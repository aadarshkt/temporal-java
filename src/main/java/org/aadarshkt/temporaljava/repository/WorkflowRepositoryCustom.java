package org.aadarshkt.temporaljava.repository;

import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;

import java.util.List;

public interface WorkflowRepositoryCustom {
    void createExecution(Workflow workflow, List<Task> tasks);
}
