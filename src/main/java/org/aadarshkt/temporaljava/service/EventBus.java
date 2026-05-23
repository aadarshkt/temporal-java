package org.aadarshkt.temporaljava.service;

import org.aadarshkt.temporaljava.domain.TaskCompletedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminatedEvent;

import java.util.concurrent.BlockingQueue;

public interface EventBus {
    
    void publishTaskCompleted(TaskCompletedEvent event);
    
    void publishTaskTerminated(TaskTerminatedEvent event);
    
    // Equivalent to SubscribeToEvents in Go, renamed as requested
    BlockingQueue<TaskCompletedEvent> subscribeToCompletedEvents();
    
    // Equivalent to SubscribeToTerminationEvents in Go
    BlockingQueue<TaskTerminatedEvent> subscribeToTerminationEvents();
}
