package com.thales.common.model;

import java.util.Map;

public class ServerValidator {

    private static ServerValidator instance;
    private Map<Operation, JsonSchema> schemas;
    private JsonSchema defaultSchema;

    private ServerValidator(){
        schemas = Map.ofEntries(
            Map.entry(Operation.LOGIN, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.LOGOUT, new JsonSchema("/schemas/schema.json")),

            Map.entry(Operation.CREATE_USER, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_OWN_USER, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_USER, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.LIST_USERS, new JsonSchema("schemas/schema.json")),
            Map.entry(Operation.DELETE_OWN_USER, new JsonSchema("schemas/schema.json")),
            Map.entry(Operation.DELETE_USER, new JsonSchema("schemas/schema.json")),

            Map.entry(Operation.CREATE_MOVIE, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_MOVIE, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.LIST_MOVIES, new JsonSchema("schemas/schema.json")),
            Map.entry(Operation.DELETE_MOVIE, new JsonSchema("schemas/schema.json")),

            Map.entry(Operation.CREATE_REVIEW, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.UPDATE_REVIEW, new JsonSchema("/schemas/schema.json")),
            Map.entry(Operation.LIST_REVIEWS, new JsonSchema("schemas/schema.json")),
            Map.entry(Operation.DELETE_REVIEW, new JsonSchema("schemas/schema.json"))
        );
    }

    public static ServerValidator getInstance(){
        if(instance == null){
            instance = new ServerValidator();
        }
        return instance;
    }
}
