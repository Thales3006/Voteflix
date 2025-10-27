package com.thales.common.model;

import com.thales.common.utils.ErrorTable;

import lombok.Getter;

@Getter
public class StatusException extends Exception {
    private ErrorStatus status;
    private String message = null;

    public StatusException(ErrorStatus status){
        this.status = status;
        this.message = ErrorTable.getInstance().get(status).getSecond();
    }

    public StatusException(ErrorStatus status, String message){
        this.status = status;
        this.message = message;
    }
}
