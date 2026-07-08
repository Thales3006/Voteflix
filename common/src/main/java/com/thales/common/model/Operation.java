package com.thales.common.model;

public enum Operation {
    LOGIN("LOGIN", ResponseKind.LOGIN),
    LOGOUT("LOGOUT", ResponseKind.OK),

    CREATE_USER("CREATE_USER", ResponseKind.CREATED),
    UPDATE_OWN_USER("UPDATE_OWN_USER", ResponseKind.OK),
    ADMIN_UPDATE_USER("ADMIN_UPDATE_USER", ResponseKind.OK),
    LIST_OWN_USER("LIST_OWN_USER", ResponseKind.USER_INFO),
    LIST_USERS("LIST_USERS", ResponseKind.USER_LIST),
    DELETE_OWN_USER("DELETE_OWN_USER", ResponseKind.OK),
    ADMIN_DELETE_USER("ADMIN_DELETE_USER", ResponseKind.OK),

    CREATE_MOVIE("CREATE_MOVIE", ResponseKind.CREATED),
    UPDATE_MOVIE("UPDATE_MOVIE", ResponseKind.OK),
    LIST_MOVIES("LIST_MOVIES", ResponseKind.MOVIE_LIST),
    DELETE_MOVIE("DELETE_MOVIE", ResponseKind.OK),

    CREATE_REVIEW("CREATE_REVIEW", ResponseKind.CREATED),
    UPDATE_REVIEW("UPDATE_REVIEW", ResponseKind.OK),
    LIST_OWN_REVIEWS("LIST_OWN_REVIEWS", ResponseKind.REVIEW_LIST),
    LIST_REVIEWS("LIST_REVIEWS", ResponseKind.REVIEW_LIST),
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
