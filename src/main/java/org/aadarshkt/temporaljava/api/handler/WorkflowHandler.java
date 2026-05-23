package org.aadarshkt.temporaljava.api.handler;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aadarshkt.temporaljava.api.dto.CreateWorkflowRequest;
import org.aadarshkt.temporaljava.api.dto.CreateWorkflowResponse;
import org.aadarshkt.temporaljava.api.mapper.WorkflowMapper;
import org.aadarshkt.temporaljava.domain.Task;
import org.aadarshkt.temporaljava.domain.Workflow;
import org.aadarshkt.temporaljava.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowHandler {

    private final WorkflowService workflowService;
    private final WorkflowMapper workflowMapper;

    @PostMapping
    public ResponseEntity<CreateWorkflowResponse> submitWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        
        // Convert DTO to domain entities using mapper
        Workflow execution = workflowMapper.toWorkflow(request);
        List<Task> tasks = workflowMapper.toTasks(request, execution);

        // Submit workflow and get execution ID
        UUID executionId = workflowService.submitWorkflow(execution, tasks);

        // Return the response
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateWorkflowResponse(executionId));
    }
}
