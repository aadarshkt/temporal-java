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

}
