package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;

import java.util.List;
import java.util.UUID;

public interface TaskRepositoryCustom {

    int claimTask(UUID taskId, String workerId, int currentVersion);
    
    List<UUID> decrementAndGetReadyTasks(UUID executionId, String completedRefId);
    
    List<UUID> decrementAndSetSkipHint(UUID executionId, String failedRefId);

    List<Task> findChildren(UUID executionId, String parentName);
}
