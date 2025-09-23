package com.thales.client.model;

import lombok.Getter;

@Getter
public class StatusException extends Exception {
    private String status;

    public StatusException(String status){
        if(status == null){
            status = "Null";
        }
        this.status = status;
    }
}
