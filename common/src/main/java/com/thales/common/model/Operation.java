package com.thales.common.model;

public enum Operation {
    LOGIN("LOGIN", ResponseKind.LOGIN),
    LOGOUT("LOGOUT", ResponseKind.OK),

    CREATE_USER("CREATE_USER", ResponseKind.CREATED),
    GET_USER("GET_USER", ResponseKind.USER_INFO),
    LIST_USERS("LIST_USERS", ResponseKind.USER_LIST),
    UPDATE_USER("UPDATE_USER", ResponseKind.OK),
    DELETE_USER("DELETE_USER", ResponseKind.OK),

    CREATE_MOVIE("CREATE_MOVIE", ResponseKind.CREATED),
    LIST_MOVIES("LIST_MOVIES", ResponseKind.MOVIE_LIST),
    UPDATE_MOVIE("UPDATE_MOVIE", ResponseKind.OK),
    DELETE_MOVIE("DELETE_MOVIE", ResponseKind.OK),

    CREATE_REVIEW("CREATE_REVIEW", ResponseKind.CREATED),
    LIST_REVIEWS("LIST_REVIEWS", ResponseKind.REVIEW_LIST),
    UPDATE_REVIEW("UPDATE_REVIEW", ResponseKind.OK),
    DELETE_REVIEW("DELETE_REVIEW", ResponseKind.OK),

    UNKNOWN("", ResponseKind.OK);

    private final String code;
    private final ResponseKind responseKind;

    Operation(String code, ResponseKind responseKind) {
        this.code = code;
        this.responseKind = responseKind;
    }

    public String getCode() { return code; }
    public ResponseKind getResponseKind() { return responseKind; }

    public static Operation fromCode(String code) {
        for (Operation op : values()) {
            if (op.code.equals(code)) return op;
        }
        return UNKNOWN;
    }
}
