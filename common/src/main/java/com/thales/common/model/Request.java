package com.thales.common.model;

public sealed interface Request {

    Operation operation();

    sealed interface UserRequest extends Request
        permits CreateUserRequest, GetUserRequest, ListUsersRequest, UpdateUserRequest, DeleteUserRequest {}
    sealed interface MovieRequest extends Request
        permits CreateMovieRequest, ListMoviesRequest, UpdateMovieRequest, DeleteMovieRequest {}
    sealed interface ReviewRequest extends Request
        permits CreateReviewRequest, ListReviewsRequest, UpdateReviewRequest, DeleteReviewRequest {}

    record LoginRequest(String username, String password) implements Request {
        public Operation operation() { return Operation.LOGIN; }
    }
    record LogoutRequest(String token) implements Request {
        public Operation operation() { return Operation.LOGOUT; }
    }

    record CreateUserRequest(User user) implements UserRequest {
        public Operation operation() { return Operation.CREATE_USER; }
    }
    record GetUserRequest(String token) implements UserRequest {
        public Operation operation() { return Operation.GET_USER; }
    }
    record ListUsersRequest(String token, UserFilter filter) implements UserRequest {
        public Operation operation() { return Operation.LIST_USERS; }
    }
    record UpdateUserRequest(String token, User user) implements UserRequest {
        public Operation operation() { return Operation.UPDATE_USER; }
    }
    record DeleteUserRequest(String token, int id) implements UserRequest {
        public Operation operation() { return Operation.DELETE_USER; }
    }

    record CreateMovieRequest(String token, Movie movie) implements MovieRequest {
        public Operation operation() { return Operation.CREATE_MOVIE; }
    }
    record ListMoviesRequest(String token, MovieFilter filter) implements MovieRequest {
        public Operation operation() { return Operation.LIST_MOVIES; }
    }
    record UpdateMovieRequest(String token, Movie movie) implements MovieRequest {
        public Operation operation() { return Operation.UPDATE_MOVIE; }
    }
    record DeleteMovieRequest(String token, int id) implements MovieRequest {
        public Operation operation() { return Operation.DELETE_MOVIE; }
    }

    record CreateReviewRequest(String token, Review review) implements ReviewRequest {
        public Operation operation() { return Operation.CREATE_REVIEW; }
    }
    record ListReviewsRequest(String token, ReviewFilter filter) implements ReviewRequest {
        public Operation operation() { return Operation.LIST_REVIEWS; }
    }
    record UpdateReviewRequest(String token, Review review) implements ReviewRequest {
        public Operation operation() { return Operation.UPDATE_REVIEW; }
    }
    record DeleteReviewRequest(String token, int id) implements ReviewRequest {
        public Operation operation() { return Operation.DELETE_REVIEW; }
    }
}
