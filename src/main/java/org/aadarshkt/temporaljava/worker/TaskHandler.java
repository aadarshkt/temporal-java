package org.aadarshkt.temporaljava.worker;

/**
 * Blueprint for any function that does work.
 * Equivalent to: type TaskHandler func(ctx context.Context, input []byte) ([]byte, error)
 *
 * <p>Implementations should check {@link Thread#interrupted()} periodically
 * to honor cancellation (analogous to {@code ctx.Done()} in Go).
 */
@FunctionalInterface
public interface TaskHandler {

    /**
     * Execute the task with the given raw JSON input payload.
     *
     * @param input raw JSON bytes passed to the task
     * @return raw JSON bytes produced by the task
     * @throws InterruptedException if the current thread is interrupted (timeout / cancellation)
     * @throws Exception            for any other task-level failure
     */
    byte[] execute(byte[] input) throws Exception;
}

