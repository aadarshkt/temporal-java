package org.aadarshkt.temporaljava.queue;

import org.aadarshkt.temporaljava.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskQueue extends BaseRabbitQueue {

    public TaskQueue(RabbitTemplate rabbitTemplate) {
        super(rabbitTemplate, RabbitMQConfig.TASK_QUEUE_NAME);
    }
}
