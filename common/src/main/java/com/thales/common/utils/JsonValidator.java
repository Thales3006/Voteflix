package com.thales.common.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.everit.json.schema.ValidationException;

import com.google.gson.JsonObject;
import com.thales.common.model.Request;
import com.thales.common.model.Response;
import com.thales.common.model.StatusException;

public class JsonValidator {

    private static JsonValidator instance;
    private Map<Request, JsonSchema> requestSchemas;
    private Map<Response, JsonSchema> responseSchemas;
    private JsonSchema basicRequestSchema;
    private JsonSchema basicResponseSchema;

    private JsonValidator() { }

    public static JsonValidator getInstance() {
        if(instance == null){
            instance = new JsonValidator();
        }
        return instance;
    }

    public void loadSchemas() throws RuntimeException {
        try{
            basicRequestSchema = JsonSchema.loadFromResource("/schemas/requests/basic_request.json");
            requestSchemas = Arrays.stream(Request.values())
                .collect(Collectors.toMap(
                Function.identity(),
                operation -> JsonSchema.loadFromResource("/schemas/requests/" + operation.name().toLowerCase() + ".json")
                ));
            basicResponseSchema = JsonSchema.loadFromResource("/schemas/responses/basic_response.json");
            responseSchemas = Arrays.stream(Response.values())
                .collect(Collectors.toMap(
                Function.identity(),
                operation -> JsonSchema.loadFromResource("/schemas/responses/" + operation.name().toLowerCase() + ".json")
                ));
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void validateResponce(JsonObject json, Response response) throws IllegalStateException, StatusException {
        try{
        responseSchemas.get(response).validate(json.toString());
        } catch ( Exception e ) {
            try{
                responseSchemas.get(Response.ERROR).validate(json.toString());
                throw new StatusException(json.get("status").getAsString());
            } catch ( Exception ee ) {
                throw e;
            }
        }
    }

    public Response getResponce(final String message) throws ValidationException {
        basicResponseSchema.validate(message);

        for (Map.Entry<Response, JsonSchema> entry : responseSchemas.entrySet()) {
            try {
                entry.getValue().validate(message);
                return entry.getKey();
            } catch (ValidationException e) { }
        }
        throw new ValidationException(
            basicResponseSchema.getSchema(),
            "No matching response schema found",
            "status",
            basicResponseSchema.getSchema().getSchemaLocation()
        );
    }

    public void validateResquest(JsonObject json, Request request) throws ValidationException {
        requestSchemas.get(request).validate(json.toString());
    }

    public Request getRequest(final String message) throws ValidationException {
        basicRequestSchema.validate(message);

        for (Map.Entry<Request, JsonSchema> entry : requestSchemas.entrySet()) {
            try {
            entry.getValue().validate(message);
            return entry.getKey();
            } catch (ValidationException e) { }
        }
        throw new ValidationException(
            basicResponseSchema.getSchema(),
            "No matching response schema found",
            "status",
            basicResponseSchema.getSchema().getSchemaLocation()
        );
    }

}
