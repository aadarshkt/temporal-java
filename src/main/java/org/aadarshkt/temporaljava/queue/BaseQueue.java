package org.aadarshkt.temporaljava.queue;

import java.util.UUID;

public interface BaseQueue {
    
    /**
     * Pushes a task ID to the end of the queue.
     * @param taskId the UUID of the task
     */
    void push(UUID taskId);
    
    /**
     * Pops a task ID from the queue. Blocks up to timeoutMillis if the queue is empty.
     * @param timeoutMillis the timeout in milliseconds.
     * @return the UUID, or null if no message was received within the timeout.
     */
    UUID pop(long timeoutMillis);
    
    /**
     * Pops a task ID from the queue, blocking indefinitely until an item is available.
     * @return the UUID, or null if the thread is interrupted
     */
    UUID pop();
}
