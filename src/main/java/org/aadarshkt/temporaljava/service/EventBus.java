package org.aadarshkt.temporaljava.service;

import org.aadarshkt.temporaljava.domain.TaskCompletedEvent;
import org.aadarshkt.temporaljava.domain.TaskTerminatedEvent;

public interface EventBus {
    
    void publishTaskCompleted(TaskCompletedEvent event);
    
    void publishTaskTerminated(TaskTerminatedEvent event);
}
