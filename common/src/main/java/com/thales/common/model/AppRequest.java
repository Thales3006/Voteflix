package com.thales.common.model;

public sealed interface AppRequest {

    Operation operation();

    record LoginRequest(String username, String password) implements AppRequest {
        public Operation operation() { return Operation.LOGIN; }
    }
    record LogoutRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.LOGOUT; }
    }

    record CreateUserRequest(String username, String password) implements AppRequest {
        public Operation operation() { return Operation.CREATE_USER; }
    }
    record UpdateOwnUserRequest(String token, String password) implements AppRequest {
        public Operation operation() { return Operation.UPDATE_OWN_USER; }
    }
    record AdminUpdateUserRequest(String token, int id, String password) implements AppRequest {
        public Operation operation() { return Operation.ADMIN_UPDATE_USER; }
    }
    record ListOwnUserRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.LIST_OWN_USER; }
    }
    record ListUsersRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.LIST_USERS; }
    }
    record DeleteOwnUserRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.DELETE_OWN_USER; }
    }
    record AdminDeleteUserRequest(String token, int id) implements AppRequest {
        public Operation operation() { return Operation.ADMIN_DELETE_USER; }
    }

    record CreateMovieRequest(String token, Movie movie) implements AppRequest {
        public Operation operation() { return Operation.CREATE_MOVIE; }
    }
    record UpdateMovieRequest(String token, Movie movie) implements AppRequest {
        public Operation operation() { return Operation.UPDATE_MOVIE; }
    }
    record ListMoviesRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.LIST_MOVIES; }
    }
    record DeleteMovieRequest(String token, int id) implements AppRequest {
        public Operation operation() { return Operation.DELETE_MOVIE; }
    }

    record CreateReviewRequest(String token, Review review) implements AppRequest {
        public Operation operation() { return Operation.CREATE_REVIEW; }
    }
    record UpdateReviewRequest(String token, Review review) implements AppRequest {
        public Operation operation() { return Operation.UPDATE_REVIEW; }
    }
    record ListOwnReviewsRequest(String token) implements AppRequest {
        public Operation operation() { return Operation.LIST_OWN_REVIEWS; }
    }
    record ListReviewsRequest(String token, int movieId) implements AppRequest {
        public Operation operation() { return Operation.LIST_REVIEWS; }
    }
    record DeleteReviewRequest(String token, int id) implements AppRequest {
        public Operation operation() { return Operation.DELETE_REVIEW; }
    }
}
