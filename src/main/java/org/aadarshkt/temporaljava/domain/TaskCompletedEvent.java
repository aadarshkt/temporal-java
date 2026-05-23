package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TaskCompletedEvent extends TaskEvent {

    private TaskCompletedEvent(UUID executionId, UUID taskId, String refId) {
        super(executionId, taskId, refId);
    }

    public static TaskCompletedEvent of(UUID executionId, UUID taskId, String refId) {
        return new TaskCompletedEvent(executionId, taskId, refId);
    }
}

