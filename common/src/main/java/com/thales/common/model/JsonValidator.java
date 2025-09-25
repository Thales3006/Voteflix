package com.thales.common.model;

import java.util.Map;

import org.everit.json.schema.ValidationException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JsonValidator {

    private static JsonValidator instance;
    private Map<Operation, JsonSchema> clientSchemas;
    //private Map<Return, JsonSchema> serverSchemas;
    private JsonSchema operationSchema;
    private final Gson gson = new Gson();

    private JsonValidator() { }

    public static JsonValidator getInstance() {
        if(instance == null){
            instance = new JsonValidator();
        }
        return instance;
    }

    public void loadSchemas() throws RuntimeException {
        operationSchema = JsonSchema.loadFromResource("/schemas/operacao-field.json");
        clientSchemas = Map.ofEntries(
            Map.entry(Operation.LOGIN, JsonSchema.loadFromResource("/schemas/login.json")),
            Map.entry(Operation.LOGOUT, JsonSchema.loadFromResource("/schemas/logout.json")),

            Map.entry(Operation.CREATE_USER, JsonSchema.loadFromResource("/schemas/create-user.json")),
            Map.entry(Operation.UPDATE_OWN_USER, JsonSchema.loadFromResource("/schemas/update-own-user.json")),
            Map.entry(Operation.UPDATE_USER, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.LIST_USERS, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.DELETE_OWN_USER, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.DELETE_USER, JsonSchema.loadFromResource("/schemas/schema.json")),

            Map.entry(Operation.CREATE_MOVIE, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_MOVIE, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.LIST_MOVIES, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.DELETE_MOVIE, JsonSchema.loadFromResource("/schemas/schema.json")),

            Map.entry(Operation.CREATE_REVIEW, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_REVIEW, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.LIST_REVIEWS, JsonSchema.loadFromResource("/schemas/schema.json")),
            Map.entry(Operation.DELETE_REVIEW, JsonSchema.loadFromResource("/schemas/schema.json"))
        );
    }

    public void validate(final String message, Operation operation) throws ValidationException {
        clientSchemas.get(operation).validate(message);
    }

    public Operation getOperation(final String message) throws ValidationException {
        operationSchema.validate(message);
        JsonObject json = gson.fromJson(message, JsonObject.class);

        switch (json.get("operacao").getAsString()) {
            case "LOGIN":
                return Operation.LOGIN;
            case "LOGOUT":
                return Operation.LOGOUT;

            case "CRIAR_USUARIO":
                return Operation.CREATE_USER;
            case "LISTAR_USUARIOS":
                return Operation.LIST_USERS;
            case "EDITAR_PROPRIO_USUARIO":
                return Operation.UPDATE_OWN_USER;
            case "ADMIN_EDITAR_USUARIO":
                return Operation.UPDATE_USER;
            case "EXCLUIR_PROPRIO_USUARIO":
                return Operation.DELETE_OWN_USER;
            case "ADMIN_EXCLUIR_USUARIO":
                return Operation.DELETE_USER;

            case "CRIAR_FILME":
                return Operation.CREATE_MOVIE;
            case "LISTAR_FILMES":
                return Operation.LIST_MOVIES;
            case "EDITAR_FILME":
                return Operation.UPDATE_MOVIE;
            case "EXCLUIR_FILME":
                return Operation.DELETE_MOVIE;

            case "CRIAR_REVIEW":
                return Operation.CREATE_REVIEW;
            case "BUSCAR_FILME_ID":
                return Operation.LIST_REVIEWS;
            case "EDITAR_REVIEW":
                return Operation.UPDATE_REVIEW;
            case "EXCLUIR_REVIEW":
                return Operation.DELETE_REVIEW;

            default:
                throw new ValidationException(
                    operationSchema.getSchema(),
                    "Invalid operation",
                    "operacao", 
                    operationSchema.getSchema().getSchemaLocation()
                );
        }
    }

}
