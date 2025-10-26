package com.thales.common.model;

public enum ErrorStatus {
    OK("200"),
    CREATED("201"),
    BAD_REQUEST("400"),
    UNAUTHORIZED("401"),
    FORBIDDEN("403"),
    NOT_FOUND("404"),
    INVALID_INPUT("405"),
    ALREADY_EXISTS("409"),
    UNPROCESSABLE_ENTITY("422"),
    INTERNAL_SERVER_ERROR("500"),
    UNKNOWN_ERROR("");

    private final String code;

    private ErrorStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ErrorStatus fromCode(String code) {
        for (ErrorStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return UNKNOWN_ERROR;
    }
}
