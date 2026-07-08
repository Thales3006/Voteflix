package com.thales.common.model;

import lombok.Getter;

@Getter
public class StatusException extends RuntimeException {
    private final ErrorStatus status;
    private final String userMessage;

    public StatusException(ErrorStatus status) {
        super(status.getMessage());
        this.status = status;
        this.userMessage = status.getMessage();
    }

    public StatusException(ErrorStatus status, String userMessage) {
        super(userMessage);
        this.status = status;
        this.userMessage = userMessage;
    }
}
