package com.thales.common.model;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonSchemaValidator {
    private final Schema schema;

    public JsonSchemaValidator(String schemaJson) {
        JSONObject rawSchema = new JSONObject(new JSONTokener(schemaJson));
        this.schema = SchemaLoader.load(rawSchema);
    }

    public JsonSchemaValidator loadFromResource(String resourcePath) {
        try (java.io.InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            JSONObject rawSchema = new JSONObject(new JSONTokener(is));
            return new JsonSchemaValidator(rawSchema.toString());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load schema from resource: " + resourcePath, e);
        }
    }

    public void validate(String jsonData) throws org.everit.json.schema.ValidationException {
        JSONObject jsonObject = new JSONObject(new JSONTokener(jsonData));
        schema.validate(jsonObject);
    }
}
