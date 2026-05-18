package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Workflow;
import org.aadarshkt.temporaljava.domain.WorkflowStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID>, WorkflowRepositoryCustom {

    /**
     * Updates the workflow execution status.
     * The status check in the WHERE clause prevents duplicate updates when multiple terminal tasks
     * complete simultaneously. Additionally, once a workflow is FAILED, it cannot be overwritten.
     */
    @Modifying
    @Query("UPDATE Workflow w SET w.status = :status " +
           "WHERE w.id = :id AND w.status <> :status AND w.status <> :failedStatus")
    int updateStatusInternal(@Param("id") UUID id, @Param("status") WorkflowStatus status, @Param("failedStatus") WorkflowStatus failedStatus);

    default int updateStatus(UUID id, WorkflowStatus status) {
        return updateStatusInternal(id, status, WorkflowStatus.FAILED);
    }
}
