package org.aadarshkt.temporaljava.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aadarshkt.temporaljava.config.RabbitMQConfig;
import org.aadarshkt.temporaljava.queue.RetryQueue;
import org.aadarshkt.temporaljava.repository.TaskRepository;
import org.aadarshkt.temporaljava.repository.WorkflowRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.aadarshkt.temporaljava.service.EventBus;

@Component
public class RetryTaskWorker extends AbstractTaskWorker {

    public RetryTaskWorker(TaskRepository taskRepository, ObjectMapper objectMapper, WorkflowRepository workflowRepo, RetryQueue retryQueue, EventBus eventBus) {
        super(taskRepository, objectMapper, workflowRepo, retryQueue, eventBus);
    }

    @RabbitListener(
            queues = RabbitMQConfig.RETRY_QUEUE_NAME,
            containerFactory = "rabbitListenerContainerFactory",
            concurrency = "1" // As per the Go code 9:1 ratio
    )
    @Transactional
    @Override
    public void processTask(String message) {
        super.processTask(message);
    }
}
