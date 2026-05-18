package org.aadarshkt.temporaljava.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE_NAME = "task_queue";
    public static final String RETRY_QUEUE_NAME = "retry_queue";

    @Bean
    public Queue taskQueue() {
        // durable = true
        return new Queue(TASK_QUEUE_NAME, true);
    }

    @Bean
    public Queue retryQueue() {
        // durable = true
        return new Queue(RETRY_QUEUE_NAME, true);
    }
}
