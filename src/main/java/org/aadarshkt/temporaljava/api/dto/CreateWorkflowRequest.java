package org.aadarshkt.temporaljava.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateWorkflowRequest {

    @NotEmpty(message = "Type is required")
    private String type;

    @JsonProperty("user_id")
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "Tasks cannot be empty")
    @Valid
    private List<TaskDTO> tasks;
}
