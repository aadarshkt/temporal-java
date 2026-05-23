package org.aadarshkt.temporaljava.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aadarshkt.temporaljava.domain.TaskCompletedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQEventBus implements EventBus {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // We can use a TopicExchange for routing events
    private static final String EXCHANGE_NAME = "workflow.events.exchange";
    private static final String COMPLETED_ROUTING_KEY = "task.completed";
    private static final String TERMINATED_ROUTING_KEY = "task.terminated";

    @Override
    public void publishTaskCompleted(TaskCompletedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, COMPLETED_ROUTING_KEY, payload);
            log.info("Published TaskCompletedEvent for task {}", event.getTaskId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TaskCompletedEvent", e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    @Override
    public void publishTaskTerminated(TaskTerminatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, TERMINATED_ROUTING_KEY, payload);
            log.info("Published TaskTerminatedEvent for task {} with type {}", event.getTaskId(), event.getType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize TaskTerminatedEvent", e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}
