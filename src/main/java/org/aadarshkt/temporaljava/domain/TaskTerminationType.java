package org.aadarshkt.temporaljava.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskTerminationType {

    FAILED("failed"),
    SKIPPED("skipped");

    private final String value;

    TaskTerminationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

