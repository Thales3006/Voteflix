package com.thales.common.utils;

import org.json.JSONObject;
import com.thales.common.model.Request;
import com.thales.common.model.Request.*;
import com.thales.common.model.MovieFilter;
import com.thales.common.model.ReviewFilter;
import com.thales.common.model.UserFilter;

public class RequestSerializer {

    public String serialize(Request request) {
        JSONObject json = new JSONObject();
        json.put("operation", request.operation().getCode());

        switch (request) {
            case LoginRequest r -> {
                json.put("username", r.username());
                json.put("password", r.password());
            }
            case LogoutRequest r -> json.put("token", r.token());

            case CreateUserRequest r -> {
                JSONObject user = new JSONObject();
                user.put("username", r.user().getUsername());
                user.put("password", r.user().getPassword());
                json.put("user", user);
            }
            case GetUserRequest r -> json.put("token", r.token());
            case ListUsersRequest r -> {
                json.put("token", r.token());
                if (r.filter() != null) json.put("filter", serializeUserFilter(r.filter()));
            }
            case UpdateUserRequest r -> {
                json.put("token", r.token());
                JSONObject user = new JSONObject();
                user.put("id", r.user().getId());
                user.put("password", r.user().getPassword());
                json.put("user", user);
            }
            case DeleteUserRequest r -> {
                json.put("token", r.token());
                json.put("id", r.id());
            }

            case CreateMovieRequest r -> {
                json.put("token", r.token());
                json.put("movie", r.movie().toJson());
            }
            case ListMoviesRequest r -> {
                json.put("token", r.token());
                if (r.filter() != null) json.put("filter", serializeMovieFilter(r.filter()));
            }
            case UpdateMovieRequest r -> {
                json.put("token", r.token());
                json.put("movie", r.movie().toJson());
            }
            case DeleteMovieRequest r -> {
                json.put("token", r.token());
                json.put("id", r.id());
            }

            case CreateReviewRequest r -> {
                json.put("token", r.token());
                json.put("review", r.review().toJson());
            }
            case ListReviewsRequest r -> {
                json.put("token", r.token());
                if (r.filter() != null) json.put("filter", serializeReviewFilter(r.filter()));
            }
            case UpdateReviewRequest r -> {
                json.put("token", r.token());
                json.put("review", r.review().toJson());
            }
            case DeleteReviewRequest r -> {
                json.put("token", r.token());
                json.put("id", r.id());
            }
        }
        return json.toString();
    }

    private JSONObject serializeUserFilter(UserFilter f) {
        JSONObject obj = new JSONObject();
        if (f.username() != null) obj.put("username", f.username());
        return obj;
    }

    private JSONObject serializeMovieFilter(MovieFilter f) {
        JSONObject obj = new JSONObject();
        if (f.genre() != null) obj.put("genre", f.genre());
        if (f.year() != null) obj.put("year", f.year());
        return obj;
    }

    private JSONObject serializeReviewFilter(ReviewFilter f) {
        JSONObject obj = new JSONObject();
        if (f.movieId() != null) obj.put("movie_id", f.movieId());
        if (f.userId() != null) obj.put("user_id", f.userId());
        return obj;
    }
}
