package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TaskCompletedEvent {

    @JsonProperty("execution_id")
    private final UUID executionId;

    @JsonProperty("task_id")
    private final UUID taskId;

    @JsonProperty("ref_id")
    private final String refId;

    private TaskCompletedEvent(UUID executionId, UUID taskId, String refId) {
        this.executionId = executionId;
        this.taskId = taskId;
        this.refId = refId;
    }

    public static TaskCompletedEvent of(UUID executionId, UUID taskId, String refId) {
        return new TaskCompletedEvent(executionId, taskId, refId);
    }
}

