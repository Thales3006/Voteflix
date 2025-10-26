package com.thales.common.model;

import lombok.Getter;

@Getter
public class StatusException extends Exception {
    private ErrorStatus status;

    public StatusException(ErrorStatus status){
        this.status = status;
    }
}
