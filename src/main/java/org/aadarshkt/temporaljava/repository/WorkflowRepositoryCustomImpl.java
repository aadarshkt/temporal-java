package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class WorkflowRepositoryCustomImpl implements WorkflowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void createExecution(Workflow workflow, List<Task> tasks) {
        entityManager.persist(workflow);
        if (tasks != null) {
            for (Task task : tasks) {
                entityManager.persist(task);
            }
        }
    }
}
