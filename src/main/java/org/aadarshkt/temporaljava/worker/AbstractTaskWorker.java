package org.aadarshkt.temporaljava.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.WorkflowStatus;
import org.aadarshkt.temporaljava.queue.RetryQueue;
import org.aadarshkt.temporaljava.repository.TaskRepository;
import org.aadarshkt.temporaljava.domain.TaskCompletedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminatedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminationType;
import org.aadarshkt.temporaljava.repository.WorkflowRepository;
import org.aadarshkt.temporaljava.service.EventBus;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTaskWorker {

    protected final TaskRepository taskRepository;
    protected final ObjectMapper objectMapper;
    protected final WorkflowRepository workflowRepo;
    protected final RetryQueue retryQueue;
    
    protected final String workerId = UUID.randomUUID().toString();
    
    // We instantiate the registry here; ideally it could be a Spring Bean.
    protected final TaskRegistry taskRegistry = TaskRegistry.init();

    // Placeholder for EventBus
    protected final EventBus eventBus;

    /**
     * Shared processing logic for both main and retry workers.
     * The @RabbitListener and @Transactional annotations will be on the child class methods.
     */
    public void processTask(String message) {
        log.debug("Received task message: {}", message);
        try {
            UUID taskId = UUID.fromString(message);
            
            // 1. Fetch task
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                log.warn("Task with ID {} not found in database. Discarding message.", taskId);
                return;
            }

            Task task = taskOpt.get();

            // 2. Check if task should be skipped
            if (task.isSkipHint()) {
                handleSkippedTask(task);
                return;
            }

            // 3. Claim the task
            if (!claimTask(task)) {
                return; // Failed to claim (already claimed by another worker)
            }

            // 4. Execute the task
            byte[] output;
            try {
                output = executeTaskAction(task);
            } catch (Exception err) {
                handleTaskFailure(task, err);
                return;
            }

            // 5. Handle successful completion
            handleTaskSuccess(task, output);

        } catch (IllegalArgumentException e) {
            log.error("Invalid task message format: {}", message, e);
        }
    }

    private void handleSkippedTask(Task task) {
        log.info("Worker {} skipping task {} (parent task failed)", workerId, task.getRefId());
        
        taskRepository.markSkipped(task.getId(), null);
        
        // Publish termination event so coordinator can propagate to children
        eventBus.publishTaskTerminated(TaskTerminatedEvent.of(
            task.getExecutionId(),
            task.getId(),
            task.getRefId(),
            TaskTerminationType.SKIPPED,
            "skipped due to parent task failure"
        ));
        
        log.info("Worker successfully skipped task {}", task.getRefId());
    }

    private boolean claimTask(Task task) {
        int updated = taskRepository.claimTask(task.getId(), workerId, task.getVersion());
        if (updated == 0) {
            log.warn("Worker {} failed to claim task {} (already claimed by another worker)", workerId, task.getRefId());
            return false;
        }

        // Update in-memory version to match DB after claim (version was incremented in DB)
        task.setVersion(task.getVersion() + 1);
        log.info("Worker {} claimed task {}", workerId, task.getRefId());
        return true;
    }

    private byte[] executeTaskAction(Task task) throws Exception {
        Optional<TaskHandler> handlerOpt = taskRegistry.get(task.getAction());
        if (handlerOpt.isEmpty()) {
            log.error("Worker unknown action: {}", task.getAction());
            taskRepository.markFailed(task.getId(), "unknown action", null);
            
            // Publish termination event
            eventBus.publishTaskTerminated(TaskTerminatedEvent.of(
                task.getExecutionId(),
                task.getId(),
                task.getRefId(),
                TaskTerminationType.FAILED,
                "unknown action"
            ));
            
            // Update workflow status to failed
            workflowRepo.updateStatus(task.getExecutionId(), WorkflowStatus.FAILED);
            
            throw new Exception("unknown action");
        }

        byte[] inputBytes = task.getInput() != null ? objectMapper.writeValueAsBytes(task.getInput()) : new byte[0];
        return handlerOpt.get().execute(inputBytes);
    }

    private void handleTaskFailure(Task task, Exception execErr) {
        log.error("Worker task {} failed: {}", task.getRefId(), execErr.getMessage());

        if (task.canRetry(task.getMaxRetries())) {
            retryTask(task);
            return;
        }

        markTaskFailedPermanently(task, execErr);
    }

    private void retryTask(Task task) {
        log.info("Worker retrying task {} (retry {}/{})", task.getRefId(), task.getAttemptCount() + 1, task.getMaxRetries());

        taskRepository.incrementAttemptCount(task.getId(), task.getVersion());

        // Push task to retry queue
        retryQueue.push(task.getId());
        log.info("Task {} pushed to retry queue", task.getRefId());
    }

    private void markTaskFailedPermanently(Task task, Exception execErr) {
        log.info("Worker task {} exhausted all retries, marking as failed", task.getRefId());

        taskRepository.markFailed(task.getId(), execErr.getMessage(), null);

        // Update workflow status to failed
        workflowRepo.updateStatus(task.getExecutionId(), WorkflowStatus.FAILED);

        // Publish termination event to propagate skip hint to children
        eventBus.publishTaskTerminated(TaskTerminatedEvent.of(
            task.getExecutionId(),
            task.getId(),
            task.getRefId(),
            TaskTerminationType.FAILED,
            execErr.getMessage()
        ));
    }

    private void handleTaskSuccess(Task task, byte[] output) {
        try {
            JsonNode outputJson = (output != null && output.length > 0) ? objectMapper.readTree(output) : null;
            taskRepository.markCompleted(task.getId(), outputJson);

            // Publish completion event
            eventBus.publishTaskCompleted(TaskCompletedEvent.of(
                task.getExecutionId(),
                task.getId(),
                task.getRefId()
            ));

            log.info("Worker successfully finished {}", task.getRefId());
        } catch (Exception e) {
            log.error("Failed to parse output JSON or mark completed for task {}", task.getRefId(), e);
        }
    }
}
