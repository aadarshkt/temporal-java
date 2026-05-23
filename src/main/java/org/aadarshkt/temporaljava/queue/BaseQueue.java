package org.aadarshkt.temporaljava.queue;

import java.util.UUID;

public interface BaseQueue {
    
    /**
     * Pushes a task ID to the end of the queue.
     * @param taskId the UUID of the task
     */
    void push(UUID taskId);
}
