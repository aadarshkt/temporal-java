package org.aadarshkt.temporaljava.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TaskDTO {

    @JsonProperty("ref_id")
    @NotEmpty(message = "Ref ID is required")
    private String refId;

    @NotEmpty(message = "Action is required")
    private String action;

    private List<String> dependencies;

    @NotNull(message = "Input is required")
    private Map<String, Object> input;
}
