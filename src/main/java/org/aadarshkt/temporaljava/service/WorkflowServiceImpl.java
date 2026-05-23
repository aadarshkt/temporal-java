package org.aadarshkt.temporaljava.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;
import org.aadarshkt.temporaljava.queue.TaskQueue;
import org.aadarshkt.temporaljava.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final TaskQueue taskQueue;

    @Override
    @Transactional
    public UUID submitWorkflow(Workflow workflow, List<Task> tasks) {
        
        // Persist workflow and tasks atomically
        persistWorkflow(workflow, tasks);
        
        // Identify root tasks for enqueueing
        List<Task> rootTasks = getRootTasks(tasks);
        
        // Enqueue root tasks for immediate processing
        enqueueRootTasks(rootTasks);
        
        return workflow.getId();
    }
    
    private void persistWorkflow(Workflow workflow, List<Task> tasks) {
        workflowRepository.createExecution(workflow, tasks);
    }

    private void enqueueRootTasks(List<Task> rootTasks) {
        for (Task task : rootTasks) {
            taskQueue.push(task.getId());
        }
    }

    private List<Task> getRootTasks(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getInDegree() == 0)
                .collect(Collectors.toList());
    }
}
