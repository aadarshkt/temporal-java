package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TaskTerminatedEvent extends TaskEvent {

    @JsonProperty("type")
    private final TaskTerminationType type;

    @JsonProperty("error")
    private final String error;

    private TaskTerminatedEvent(UUID executionId, UUID taskId, String refId, TaskTerminationType type, String error) {
        super(executionId, taskId, refId);
        this.type = type;
        this.error = error;
    }

    public static TaskTerminatedEvent of(UUID executionId, UUID taskId, String refId,
                                         TaskTerminationType type, String error) {
        return new TaskTerminatedEvent(executionId, taskId, refId, type, error);
    }
}

