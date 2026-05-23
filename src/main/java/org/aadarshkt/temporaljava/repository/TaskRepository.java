package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.TaskStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, TaskRepositoryCustom {

    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.output = :output WHERE t.id = :taskId")
    void markCompletedInternal(@Param("taskId") UUID taskId, @Param("output") JsonNode output, @Param("status") TaskStatus status);

    default void markCompleted(UUID taskId, JsonNode output) {
        markCompletedInternal(taskId, output, TaskStatus.COMPLETED);
    }

    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.lastError = :errMessage, t.output = :output WHERE t.id = :taskId")
    void markFailedInternal(@Param("taskId") UUID taskId, @Param("errMessage") String errMessage, @Param("output") JsonNode output, @Param("status") TaskStatus status);

    default void markFailed(UUID taskId, String errMessage, JsonNode output) {
        markFailedInternal(taskId, errMessage, output, TaskStatus.FAILED);
    }

    @Modifying
    @Query("UPDATE Task t SET t.status = :status, t.output = :output WHERE t.id = :taskId")
    void markSkippedInternal(@Param("taskId") UUID taskId, @Param("output") JsonNode output, @Param("status") TaskStatus status);

    default void markSkipped(UUID taskId, JsonNode output) {
        markSkippedInternal(taskId, output, TaskStatus.SKIPPED);
    }

    @Modifying
    @Query("UPDATE Task t SET t.attemptCount = t.attemptCount + 1, t.status = :status, t.version = t.version + 1 WHERE t.id = :taskId AND t.version = :currentVersion")
    int incrementAttemptCountInternal(@Param("taskId") UUID taskId, @Param("currentVersion") int currentVersion, @Param("status") TaskStatus status);

    default int incrementAttemptCount(UUID taskId, int currentVersion) {
        return incrementAttemptCountInternal(taskId, currentVersion, TaskStatus.PENDING);
    }

    @Query("SELECT COUNT(t) = 0 FROM Task t WHERE t.executionId = :executionId AND t.status <> :completedStatus")
    boolean areAllTasksCompletedInternal(@Param("executionId") UUID executionId, @Param("completedStatus") TaskStatus completedStatus);

    default boolean areAllTasksCompleted(UUID executionId) {
        return areAllTasksCompletedInternal(executionId, TaskStatus.COMPLETED);
    }

    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.updatedAt < :timeoutThreshold")
    List<Task> findDeadTasksInternal(@Param("status") TaskStatus status, @Param("timeoutThreshold") Instant timeoutThreshold);

    default List<Task> findDeadTasks(Instant timeoutThreshold) {
        return findDeadTasksInternal(TaskStatus.RUNNING, timeoutThreshold);
    }

    @Modifying
    @Query("UPDATE Task t SET t.status = :targetStatus, t.attemptCount = t.attemptCount + 1, t.lastError = :error, t.version = t.version + 1 WHERE t.id = :taskId AND t.status = :expectedStatus")
    int safeReapTaskInternal(@Param("taskId") UUID taskId, @Param("error") String error, @Param("expectedStatus") TaskStatus expectedStatus, @Param("targetStatus") TaskStatus targetStatus);

    default int safeReapTask(UUID taskId, String error) {
        return safeReapTaskInternal(taskId, error, TaskStatus.RUNNING, TaskStatus.QUEUED);
    }
}
