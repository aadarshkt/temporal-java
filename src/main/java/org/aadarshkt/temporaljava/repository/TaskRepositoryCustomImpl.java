package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskRepositoryCustomImpl implements TaskRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // Optimistic locking implementation
    @Override
    @Transactional
    public int claimTask(UUID taskId, String workerId, int currentVersion) {
        return entityManager.createQuery(
                "UPDATE Task t SET t.status = :runningStatus, " +
                "t.workerId = :workerId, t.version = :nextVersion " +
                "WHERE t.id = :taskId AND t.version = :currentVersion")
                .setParameter("runningStatus", org.aadarshkt.temporaljava.domain.TaskStatus.RUNNING)
                .setParameter("workerId", workerId)
                .setParameter("nextVersion", currentVersion + 1)
                .setParameter("taskId", taskId)
                .setParameter("currentVersion", currentVersion)
                .executeUpdate();
    }

    // Decrement the in-degree of tasks and propagate failure/skipped events
    // Check : what is completedRefID
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<UUID> decrementAndGetReadyTasks(UUID executionId, String completedRefId) {
        String sql = "UPDATE tasks " +
                     "SET in_degree = in_degree - 1, " +
                     "    status = CASE WHEN in_degree - 1 = 0 THEN :queuedStatus ELSE status END " +
                     "WHERE execution_id = :executionId " +
                     "  AND dependencies @> CAST(:depParam AS jsonb) " +
                     "RETURNING id, in_degree";

        String depParam = "[\"" + completedRefId + "\"]";
        
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("executionId", executionId)
                .setParameter("depParam", depParam)
                .setParameter("queuedStatus", org.aadarshkt.temporaljava.domain.TaskStatus.QUEUED.name())
                .getResultList();

        List<UUID> readyTaskIds = new ArrayList<>();
        for (Object[] row : results) {
            UUID id = (UUID) row[0];
            int inDegree = (int) row[1];
            if (inDegree == 0) {
                readyTaskIds.add(id);
            }
        }
        return readyTaskIds;
    }

    // TODO : check what can be done for unchecked warnings removal.
    // TODO : why are doing queued status here.
    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<UUID> decrementAndSetSkipHint(UUID executionId, String failedRefId) {
        String sql = "UPDATE tasks " +
                     "SET in_degree = in_degree - 1, " +
                     "    skip_hint = true, " +
                     "    status = CASE WHEN in_degree - 1 = 0 THEN :queuedStatus ELSE status END " +
                     "WHERE execution_id = :executionId " +
                     "  AND dependencies @> CAST(:depParam AS jsonb) " +
                     "RETURNING id, in_degree";

        String depParam = "[\"" + failedRefId + "\"]";
        
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("executionId", executionId)
                .setParameter("depParam", depParam)
                .setParameter("queuedStatus", org.aadarshkt.temporaljava.domain.TaskStatus.QUEUED.name())
                .getResultList();

        List<UUID> readyTaskIds = new ArrayList<>();
        for (Object[] row : results) {
            UUID id = (UUID) row[0];
            int inDegree = (int) row[1];
            if (inDegree == 0) {
                readyTaskIds.add(id);
            }
        }
        return readyTaskIds;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Task> findChildren(UUID executionId, String parentName) {
        String sql = "SELECT * FROM tasks WHERE execution_id = :executionId AND dependencies @> CAST(:depParam AS jsonb)";
        String depParam = "[\"" + parentName + "\"]";
        
        return entityManager.createNativeQuery(sql, Task.class)
                .setParameter("executionId", executionId)
                .setParameter("depParam", depParam)
                .getResultList();
    }
}
