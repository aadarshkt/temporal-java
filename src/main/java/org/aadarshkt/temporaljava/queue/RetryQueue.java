package org.aadarshkt.temporaljava.queue;

import org.aadarshkt.temporaljava.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryQueue extends BaseRabbitQueue {

    public RetryQueue(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate, RabbitMQConfig.RETRY_QUEUE_NAME);
    }
}
