package com.thales.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.*;
import com.thales.common.model.AppResponse.*;

public class ResponseBuilder {

    private static final Gson gson = new Gson();

    public String serialize(AppResponse response) {
        JsonObject json = new JsonObject();
        switch (response) {
            case OkResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
            }
            case CreatedResponse r -> {
                json.addProperty("status", ErrorStatus.CREATED.getCode());
                json.addProperty("message", r.message());
            }
            case LoginResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
                json.addProperty("token", r.token());
            }
            case MovieListResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
                json.add("movies", gson.toJsonTree(r.movies().stream().map(Movie::toJson).toList()));
            }
            case ReviewListResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
                json.add("reviews", gson.toJsonTree(r.reviews().stream().map(Review::toJson).toList()));
            }
            case UserInfoResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
                json.addProperty("username", r.username());
            }
            case UserListResponse r -> {
                json.addProperty("status", ErrorStatus.OK.getCode());
                json.addProperty("message", r.message());
                json.add("users", gson.toJsonTree(r.users().stream().map(u -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("id", u.getId());
                    o.addProperty("username", u.getUsername());
                    return o;
                }).toList()));
            }
        }
        return json.toString();
    }

    public String serializeError(StatusException e) {
        JsonObject json = new JsonObject();
        json.addProperty("status", e.getStatus().getCode());
        json.addProperty("message", e.getUserMessage());
        return json.toString();
    }
}
