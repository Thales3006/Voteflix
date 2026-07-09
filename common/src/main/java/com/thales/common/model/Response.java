package com.thales.common.model;

import java.util.List;

public sealed interface Response {

    record OkResponse(String message) implements Response {}
    record CreatedResponse(String message) implements Response {}
    record LoginResponse(String message, String token, int id) implements Response {}
    record MovieListResponse(String message, List<Movie> movies) implements Response {}
    record ReviewListResponse(String message, List<Review> reviews) implements Response {}
    record UserInfoResponse(String message, String username) implements Response {}
    record UserListResponse(String message, List<User> users) implements Response {}
}
