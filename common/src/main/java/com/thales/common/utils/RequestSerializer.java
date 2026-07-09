package com.thales.common.utils;

import com.google.gson.JsonObject;
import com.thales.common.model.AppRequest;
import com.thales.common.model.AppRequest.*;
import com.thales.common.model.MovieFilter;
import com.thales.common.model.ReviewFilter;
import com.thales.common.model.UserFilter;

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
                user.addProperty("username", r.user().getUsername());
                user.addProperty("password", r.user().getPassword());
                json.add("user", user);
            }
            case GetUserRequest r -> json.addProperty("token", r.token());
            case ListUsersRequest r -> {
                json.addProperty("token", r.token());
                if (r.filter() != null) json.add("filter", serializeUserFilter(r.filter()));
            }
            case UpdateUserRequest r -> {
                json.addProperty("token", r.token());
                JsonObject user = new JsonObject();
                user.addProperty("id", r.user().getId());
                user.addProperty("password", r.user().getPassword());
                json.add("user", user);
            }
            case DeleteUserRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }

            case CreateMovieRequest r -> {
                json.addProperty("token", r.token());
                json.add("movie", r.movie().toJson());
            }
            case ListMoviesRequest r -> {
                json.addProperty("token", r.token());
                if (r.filter() != null) json.add("filter", serializeMovieFilter(r.filter()));
            }
            case UpdateMovieRequest r -> {
                json.addProperty("token", r.token());
                json.add("movie", r.movie().toJson());
            }
            case DeleteMovieRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }

            case CreateReviewRequest r -> {
                json.addProperty("token", r.token());
                json.add("review", r.review().toJson());
            }
            case ListReviewsRequest r -> {
                json.addProperty("token", r.token());
                if (r.filter() != null) json.add("filter", serializeReviewFilter(r.filter()));
            }
            case UpdateReviewRequest r -> {
                json.addProperty("token", r.token());
                json.add("review", r.review().toJson());
            }
            case DeleteReviewRequest r -> {
                json.addProperty("token", r.token());
                json.addProperty("id", r.id());
            }
        }
        return json.toString();
    }

    private JsonObject serializeUserFilter(UserFilter f) {
        JsonObject obj = new JsonObject();
        if (f.username() != null) obj.addProperty("username", f.username());
        return obj;
    }

    private JsonObject serializeMovieFilter(MovieFilter f) {
        JsonObject obj = new JsonObject();
        if (f.genre() != null) obj.addProperty("genre", f.genre());
        if (f.year() != null) obj.addProperty("year", f.year());
        return obj;
    }

    private JsonObject serializeReviewFilter(ReviewFilter f) {
        JsonObject obj = new JsonObject();
        if (f.movieId() != null) obj.addProperty("movie_id", f.movieId());
        if (f.userId() != null) obj.addProperty("user_id", f.userId());
        return obj;
    }
}
