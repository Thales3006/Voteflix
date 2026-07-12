package com.thales.common.model;

public enum ErrorStatus {
    OK("200", "OK"),
    CREATED("201", "Created"),
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    ALREADY_EXISTS("409", "Conflict"),
    UNPROCESSABLE_ENTITY("422", "Unprocessable Entity"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error"),
    UNKNOWN_ERROR("000", "Unknown Error");

    private final String code;
    private final String message;

    ErrorStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }

    public static ErrorStatus fromCode(String code) {
        for (ErrorStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        return UNKNOWN_ERROR;
    }
}
