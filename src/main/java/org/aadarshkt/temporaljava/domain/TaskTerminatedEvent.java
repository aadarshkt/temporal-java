package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TaskTerminatedEvent {

    @JsonProperty("execution_id")
    private final UUID executionId;

    @JsonProperty("task_id")
    private final UUID taskId;

    //TODO: Check what is the purpose of this field. if it is not required let's remove it.
    @JsonProperty("ref_id")
    private final String refId;

    @JsonProperty("type")
    private final TaskTerminationType type;

    @JsonProperty("error")
    private final String error;

    private TaskTerminatedEvent(UUID executionId, UUID taskId, String refId, TaskTerminationType type, String error) {
        this.executionId = executionId;
        this.taskId = taskId;
        this.refId = refId;
        this.type = type;
        this.error = error;
    }

    public static TaskTerminatedEvent of(UUID executionId, UUID taskId, String refId,
                                         TaskTerminationType type, String error) {
        return new TaskTerminatedEvent(executionId, taskId, refId, type, error);
    }
}

