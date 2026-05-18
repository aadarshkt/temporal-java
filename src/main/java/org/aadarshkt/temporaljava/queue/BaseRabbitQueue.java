package org.aadarshkt.temporaljava.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

@Slf4j
public abstract class BaseRabbitQueue implements BaseQueue {

    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    protected BaseRabbitQueue(RabbitTemplate rabbitTemplate, String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @Override
    public void push(UUID taskId) {
        try {
            rabbitTemplate.convertAndSend(queueName, taskId.toString());
            log.debug("Successfully pushed task {} to queue {}", taskId, queueName);
        } catch (Exception e) {
            log.error("Failed to push task {} to queue {}", taskId, queueName, e);
            throw new RuntimeException("Failed to push to RabbitMQ", e);
        }
    }

    @Override
    public UUID pop(long timeoutMillis) {
        try {
            Object message = rabbitTemplate.receiveAndConvert(queueName, timeoutMillis);
            if (message != null) {
                return UUID.fromString(message.toString());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to pop from queue {}", queueName, e);
            throw new RuntimeException("Failed to pop from RabbitMQ", e);
        }
    }

    @Override
    public UUID pop() {
        while (!Thread.currentThread().isInterrupted()) {
            // Poll the queue. Block for 2 seconds, if nothing is returned, retry.
            // This mimics the 'Wait forever' behaviour of Redis BLPop(0).
            UUID taskId = pop(2000); 
            if (taskId != null) {
                return taskId;
            }
        }
        return null;
    }
}
