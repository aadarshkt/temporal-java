package org.aadarshkt.temporaljava.api.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aadarshkt.temporaljava.api.dto.CreateWorkflowRequest;
import org.aadarshkt.temporaljava.api.dto.TaskDTO;
import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowMapper {

    private final ObjectMapper objectMapper;

    public WorkflowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Workflow toWorkflow(CreateWorkflowRequest request) {
        return Workflow.newWorkflow(request.getUserId(), request.getType());
    }

    public List<Task> toTasks(CreateWorkflowRequest request, Workflow workflow) {
        List<Task> tasks = new ArrayList<>();
        
        if (request.getTasks() != null) {
            for (TaskDTO taskDTO : request.getTasks()) {
                Task task = Task.newTask(workflow.getId(), taskDTO.getRefId(), taskDTO.getAction());
                
                if (taskDTO.getDependencies() != null && !taskDTO.getDependencies().isEmpty()) {
                    task.setDependencies(objectMapper.valueToTree(taskDTO.getDependencies()));
                    task.setInDegree(taskDTO.getDependencies().size());
                } else {
                    task.setInDegree(0);
                }
                
                if (taskDTO.getInput() != null) {
                    task.setInput(objectMapper.valueToTree(taskDTO.getInput()));
                }
                
                tasks.add(task);
            }
        }
        
        return tasks;
    }
}
