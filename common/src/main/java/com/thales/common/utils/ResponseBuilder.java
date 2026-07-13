package com.thales.common.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import com.thales.common.model.*;
import com.thales.common.model.Response.*;

public class ResponseBuilder {

    public String serialize(Response response) {
        JSONObject json = new JSONObject();
        switch (response) {
            case OkResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
            }
            case CreatedResponse r -> {
                json.put("status", ErrorStatus.CREATED.getCode());
                json.put("message", r.message());
            }
            case LoginResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
                json.put("token", r.token());
                json.put("id", r.id());
            }
            case MovieListResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
                JSONArray movies = new JSONArray();
                r.movies().stream().map(Movie::toJson).forEach(movies::put);
                json.put("movies", movies);
            }
            case ReviewListResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
                JSONArray reviews = new JSONArray();
                r.reviews().stream().map(Review::toJson).forEach(reviews::put);
                json.put("reviews", reviews);
            }
            case UserInfoResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
                json.put("username", r.username());
            }
            case UserListResponse r -> {
                json.put("status", ErrorStatus.OK.getCode());
                json.put("message", r.message());
                JSONArray users = new JSONArray();
                r.users().stream().map(u -> {
                    JSONObject o = new JSONObject();
                    o.put("id", u.getId());
                    o.put("username", u.getUsername());
                    return o;
                }).forEach(users::put);
                json.put("users", users);
            }
        }
        return json.toString();
    }

    public String serializeError(StatusException e) {
        JSONObject json = new JSONObject();
        json.put("status", e.getStatus().getCode());
        json.put("message", e.getUserMessage());
        return json.toString();
    }
}
