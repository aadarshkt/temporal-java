package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.TaskStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("UPDATE Task t SET t.retryCount = t.retryCount + 1, t.status = :status, t.version = t.version + 1 WHERE t.id = :taskId AND t.version = :currentVersion")
    int incrementRetryCountInternal(@Param("taskId") UUID taskId, @Param("currentVersion") int currentVersion, @Param("status") TaskStatus status);

    default int incrementRetryCount(UUID taskId, int currentVersion) {
        return incrementRetryCountInternal(taskId, currentVersion, TaskStatus.PENDING);
    }

    @Query("SELECT COUNT(t) = 0 FROM Task t WHERE t.executionId = :executionId AND t.status <> :completedStatus")
    boolean areAllTasksCompletedInternal(@Param("executionId") UUID executionId, @Param("completedStatus") TaskStatus completedStatus);

    default boolean areAllTasksCompleted(UUID executionId) {
        return areAllTasksCompletedInternal(executionId, TaskStatus.COMPLETED);
    }
}
