package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class TaskEvent {

    @JsonProperty("execution_id")
    private final UUID executionId;

    @JsonProperty("task_id")
    private final UUID taskId;

    // refId is used by the Coordinator to identify the workflow step
    @JsonProperty("ref_id")
    private final String refId;
}
