package org.aadarshkt.temporaljava.service;

import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;

import java.util.List;
import java.util.UUID;

public interface WorkflowService {
    
    /**
     * Submits a new workflow and its associated tasks.
     *
     * @param workflow the workflow entity
     * @param tasks the list of task entities
     * @return the UUID of the newly created workflow
     */
    UUID submitWorkflow(Workflow workflow, List<Task> tasks);
}
