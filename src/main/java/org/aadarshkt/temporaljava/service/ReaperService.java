package org.aadarshkt.temporaljava.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.TaskStatus;
import org.aadarshkt.temporaljava.queue.TaskQueue;
import org.aadarshkt.temporaljava.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReaperService {

    private final TaskRepository taskRepository;
    private final TaskQueue taskQueue;

    @Value("${reaper.timeout.seconds:120}")
    private long timeoutSeconds;

    @Scheduled(fixedDelayString = "${reaper.polling.interval.ms:60000}")
    @Transactional
    public void reapDeadTasks() {
        log.debug("Reaper starting check for dead tasks...");
        Instant threshold = Instant.now().minusSeconds(timeoutSeconds);
        List<Task> deadTasks = taskRepository.findDeadTasks(threshold);

        if (deadTasks.isEmpty()) {
            return;
        }

        log.info("Reaper found {} potentially dead tasks", deadTasks.size());

        for (Task task : deadTasks) {
            try {
                String errorMsg = "Reaped due to timeout. Worker might have died or timed out.";
                int updatedRows = taskRepository.safeReapTask(task.getId(), errorMsg);

                if (updatedRows == 0) {
                    log.info("Task {} was updated concurrently (likely completed), skipping reap.", task.getId());
                    continue;
                }

                // Push to the task queue
                taskQueue.push(task.getId());
                log.info("Successfully reaped and requeued task: {}", task.getId());

            } catch (Exception e) {
                log.error("Failed to reap task {}", task.getId(), e);
            }
        }
    }
}
