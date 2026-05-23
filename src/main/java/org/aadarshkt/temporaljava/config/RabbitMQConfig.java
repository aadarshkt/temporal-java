package org.aadarshkt.temporaljava.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE_NAME = "task_queue";
    public static final String RETRY_QUEUE_NAME = "retry_queue";
    public static final String CONCURRENT_LISTENERS = "3-10"; // Base concurrency config
    public static final String EVENTS_EXCHANGE_NAME = "workflow.events.exchange";

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

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE_NAME, true, false);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1); // Standard for fair dispatch, ensuring one task per worker
        return factory;
    }
}
