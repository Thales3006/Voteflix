package com.thales.common.utils;

import com.google.gson.JsonObject;
import com.thales.common.model.AppRequest;
import com.thales.common.model.AppRequest.*;

public class RequestSerializer {

    public String serialize(AppRequest request) {
        JsonObject json = new JsonObject();
        json.addProperty("operation", request.operation().getCode());

        switch (request) {
            case LoginRequest r -> {
                json.addProperty("username", r.username());
                json.addProperty("password", r.password());
            }
            case LogoutRequest r -> json.addProperty("token", r.token());

            case CreateUserRequest r -> {
                JsonObject user = new JsonObject();
                user.addProperty("username", r.username());
                user.addProperty("password", r.password());
                json.add("user", user);
            }
            case UpdateOwnUserRequest r -> {
                json.addProperty("token", r.token());
                JsonObject user = new JsonObject();
                user.addProperty("password", r.password());
                json.add("user", user);
            }
            case AdminUpdateUserRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
                JsonObject user = new JsonObject();
                user.addProperty("password", r.password());
                json.add("user", user);
            }
            case ListOwnUserRequest r -> json.addProperty("token", r.token());
            case ListUsersRequest r -> json.addProperty("token", r.token());
            case DeleteOwnUserRequest r -> json.addProperty("token", r.token());
            case AdminDeleteUserRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }

            case CreateMovieRequest r -> {
                json.addProperty("token", r.token());
                json.add("movie", r.movie().toJson());
            }
            case UpdateMovieRequest r -> {
                json.addProperty("token", r.token());
                json.add("movie", r.movie().toJson());
            }
            case ListMoviesRequest r -> json.addProperty("token", r.token());
            case DeleteMovieRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }

            case CreateReviewRequest r -> {
                json.addProperty("token", r.token());
                json.add("review", r.review().toJson());
            }
            case UpdateReviewRequest r -> {
                json.addProperty("token", r.token());
                json.add("review", r.review().toJson());
            }
            case ListOwnReviewsRequest r -> json.addProperty("token", r.token());
            case ListReviewsRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("movie_id", r.movieId());
            }
            case DeleteReviewRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }
        }
        return json.toString();
    }
}
