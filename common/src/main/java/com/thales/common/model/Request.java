package com.thales.common.model;

public enum Request {
    LOGIN("LOGIN"),
    LOGOUT("LOGOUT"),

    CREATE_USER("CRIAR_USUARIO"),
    UPDATE_OWN_USER("EDITAR_PROPRIO_USUARIO"),
    UPDATE_USER("ADMIN_EDITAR_USUARIO"),
    LIST_OWN_USER("LISTAR_PROPRIO_USUARIO"),
    LIST_USERS("LISTAR_USUARIOS"),
    DELETE_OWN_USER("EXCLUIR_PROPRIO_USUARIO"),
    DELETE_USER("ADMIN_EXCLUIR_USUARIO"),

    CREATE_MOVIE("CRIAR_FILME"),
    UPDATE_MOVIE("EDITAR_FILME"),
    LIST_MOVIES("LISTAR_FILMES"),
    DELETE_MOVIE("EXCLUIR_FILME"),

    CREATE_REVIEW("CRIAR_REVIEW"),
    UPDATE_REVIEW("EDITAR_REVIEW"),
    LIST_OWN_REVIEWS("LISTAR_REVIEWS_USUARIO"),
    LIST_REVIEWS("BUSCAR_FILME_ID"),
    DELETE_REVIEW("EXCLUIR_REVIEW"),

    UNKNOWN("");

    private final String operation;

    private Request(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public static Request fromCode(String op) {
        for (Request request : values()) {
            if (request.operation.equals(op)) {
                return request;
            }
        }
        return UNKNOWN;
    }

}
