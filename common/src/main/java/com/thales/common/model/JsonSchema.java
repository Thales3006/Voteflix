package com.thales.common.model;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import lombok.Data;

@Data
public class JsonSchema {
    private final Schema schema;

    private JsonSchema(Schema schema) {
        this.schema = schema;
    }

    public static JsonSchema loadFromResource(String resourcePath) throws RuntimeException {
        try (java.io.InputStream is = JsonSchema.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            JSONObject rawSchema = new JSONObject(new JSONTokener(is));
            Schema schema = SchemaLoader.load(rawSchema);
            return new JsonSchema(schema);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load schema from resource: " + resourcePath, e);
        }
    }

    public void validate(String jsonData) throws ValidationException {
        try{
            JSONObject jsonObject = new JSONObject(new JSONTokener(jsonData));
            schema.validate(jsonObject);
        } catch (JSONException e) {
            throw new ValidationException(
                schema, 
                "Bad formatting JSON", 
                "json", 
                schema.getSchemaLocation()
            );
        }
    }
}
