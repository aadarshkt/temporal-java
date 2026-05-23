package org.aadarshkt.temporaljava.coordinator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aadarshkt.temporaljava.config.RabbitMQConfig;
import org.aadarshkt.temporaljava.domain.TaskCompletedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminatedEvent;
import org.aadarshkt.temporaljava.domain.WorkflowStatus;
import org.aadarshkt.temporaljava.queue.TaskQueue;
import org.aadarshkt.temporaljava.repository.TaskRepository;
import org.aadarshkt.temporaljava.repository.WorkflowRepository;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Coordinator {

    private final TaskRepository taskRepository;
    private final WorkflowRepository workflowRepository;
    private final TaskQueue taskQueue;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "coordinator.task.completed.queue", durable = "true"),
            exchange = @Exchange(value = RabbitMQConfig.EVENTS_EXCHANGE_NAME, type = "topic"),
            key = "task.completed"
    ))
    public void handleTaskCompleted(String message) {
        try {
            TaskCompletedEvent event = objectMapper.readValue(message, TaskCompletedEvent.class);
            log.info("Coordinator: Task {} ({}) completed. Checking children...", event.getRefId(), event.getTaskId());

            // 1. The Atomic Update: Tell Postgres this RefID is done.
            List<UUID> readyTaskIds = taskRepository.decrementAndGetReadyTasks(event.getExecutionId(), event.getRefId());

            if (readyTaskIds == null) {
                log.error("Database error while decrementing");
                return;
            }

            // 2. The Kickoff: Push newly unblocked tasks to the queue
            for (UUID taskId : readyTaskIds) {
                log.info("Coordinator: Task {} is now unblocked! Queuing...", taskId);
                try {
                    taskQueue.push(taskId);
                } catch (Exception e) {
                    log.error("Failed to push task {} to queue: {}", taskId, e.getMessage());
                    // Note: In production, you would add a retry mechanism here
                }
            }

            // 3. Workflow Completion Check
            if (readyTaskIds.isEmpty()) {
                checkIfWorkflowFinished(event.getExecutionId());
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize TaskCompletedEvent from message: {}", message, e);
        }
    }

    private void checkIfWorkflowFinished(UUID executionId) {
        boolean allCompleted = taskRepository.areAllTasksCompleted(executionId);

        if (!allCompleted) {
            log.info("Workflow {} still has tasks in progress", executionId);
            return;
        }

        log.info("All tasks completed for workflow {}. Marking as COMPLETED...", executionId);

        try {
            workflowRepository.updateStatus(executionId, WorkflowStatus.COMPLETED);
            log.info("Workflow {} marked as COMPLETED", executionId);
        } catch (Exception e) {
            log.error("Failed to mark workflow {} as completed: {}", executionId, e.getMessage());
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "coordinator.task.terminated.queue", durable = "true"),
            exchange = @Exchange(value = RabbitMQConfig.EVENTS_EXCHANGE_NAME, type = "topic"),
            key = "task.terminated"
    ))
    public void handleTaskTerminated(String message) {
        try {
            TaskTerminatedEvent event = objectMapper.readValue(message, TaskTerminatedEvent.class);
            log.info("Coordinator: Task {} ({}) terminated with type '{}': {}. Propagating skip hint...",
                    event.getRefId(), event.getTaskId(), event.getType(), event.getError());

            // Use the specialized method that sets skip_hint=true for children
            List<UUID> readyTaskIds = taskRepository.decrementAndSetSkipHint(event.getExecutionId(), event.getRefId());

            if (readyTaskIds == null) {
                log.error("Database error while decrementing for terminated task");
                return;
            }

            // Push newly unblocked tasks to the queue (they have skip_hint=true in DB)
            for (UUID taskId : readyTaskIds) {
                log.info("Coordinator: Task {} is now unblocked (will be skipped). Queuing...", taskId);
                try {
                    taskQueue.push(taskId);
                } catch (Exception e) {
                    log.error("Failed to push task {} to queue: {}", taskId, e.getMessage());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize TaskTerminatedEvent from message: {}", message, e);
        }
    }
}
