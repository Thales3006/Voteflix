package com.thales.common.model;

import java.util.List;

public sealed interface AppResponse {

    record OkResponse(String message) implements AppResponse {}
    record CreatedResponse(String message) implements AppResponse {}
    record LoginResponse(String message, String token) implements AppResponse {}
    record MovieListResponse(String message, List<Movie> movies) implements AppResponse {}
    record ReviewListResponse(String message, List<Review> reviews) implements AppResponse {}
    record UserInfoResponse(String message, String username) implements AppResponse {}
    record UserListResponse(String message, List<User> users) implements AppResponse {}
}
