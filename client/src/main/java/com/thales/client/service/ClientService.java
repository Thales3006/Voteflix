package com.thales.client.service;

import java.io.IOException;

import com.thales.client.network.ClientSocket;
import com.thales.common.model.*;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response.*;
import com.thales.common.utils.RequestSerializer;
import com.thales.common.utils.Validator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import lombok.Data;

@Data
public class ClientService {

    private static ClientService instance;
    private String serverIP;
    private int serverPort;
    private final BooleanProperty connected = new SimpleBooleanProperty(false);
    private String token;
    private int userId;
    private boolean isAdmin;
    private String username;
    private final Validator validator;
    private final RequestSerializer serializer;

    private ClientService() {
        this.validator = Validator.getInstance();
        this.serializer = new RequestSerializer();
        try {
            validator.loadSchemas();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static ClientService getInstance() {
        if (instance == null) instance = new ClientService();
        return instance;
    }

    public void connect(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        connected.set(true);
    }

    public void close() {
        token = null;
        username = null;
        userId = 0;
        isAdmin = false;
        connected.set(false);
    }

    private Response send(Request request) throws IOException {
        String json = serializer.serialize(request);
        String response = new ClientSocket().sendAndReceive(serverIP, serverPort, json);
        return validator.parseResponse(response, request.operation());
    }

    // ===================================
    //  User operations
    // ===================================

    public String requestCreateUser(String username, String password) throws Exception {
        CreatedResponse r = (CreatedResponse) send(new CreateUserRequest(new User(username, password)));
        return r.message();
    }

    public void requestLogin(String username, String password) throws Exception {
        LoginResponse r = (LoginResponse) send(new LoginRequest(username, password));
        this.token = r.token();
        this.userId = r.id();
        this.username = username;
        this.isAdmin = "admin".equals(username);
    }

    public void requestLogout() throws Exception {
        send(new LogoutRequest(token));
        token = null;
        username = null;
        userId = 0;
        isAdmin = false;
    }

    public String requestUpdateUser(String password) throws Exception {
        OkResponse r = (OkResponse) send(new UpdateUserRequest(token, new User(userId, null, password)));
        return r.message();
    }

    public String requestAdminUpdateUser(int id, String password) throws Exception {
        OkResponse r = (OkResponse) send(new UpdateUserRequest(token, new User(id, null, password)));
        return r.message();
    }

    public UserInfoResponse requestGetUser() throws Exception {
        return (UserInfoResponse) send(new GetUserRequest(token));
    }

    public UserListResponse requestUserList() throws Exception {
        return (UserListResponse) send(new ListUsersRequest(token, null));
    }

    public UserListResponse requestUserList(UserFilter filter) throws Exception {
        return (UserListResponse) send(new ListUsersRequest(token, filter));
    }

    public String requestDeleteUser() throws Exception {
        OkResponse r = (OkResponse) send(new DeleteUserRequest(token, userId));
        return r.message();
    }

    public String requestAdminDeleteUser(int id) throws Exception {
        OkResponse r = (OkResponse) send(new DeleteUserRequest(token, id));
        return r.message();
    }

    // ===================================
    //  Movie operations
    // ===================================

    public String requestCreateMovie(Movie movie) throws Exception {
        CreatedResponse r = (CreatedResponse) send(new CreateMovieRequest(token, movie));
        return r.message();
    }

    public MovieListResponse requestMovieList() throws Exception {
        return (MovieListResponse) send(new ListMoviesRequest(token, null));
    }

    public MovieListResponse requestMovieList(MovieFilter filter) throws Exception {
        return (MovieListResponse) send(new ListMoviesRequest(token, filter));
    }

    public String requestUpdateMovie(Movie movie) throws Exception {
        OkResponse r = (OkResponse) send(new UpdateMovieRequest(token, movie));
        return r.message();
    }

    public String requestDeleteMovie(int id) throws Exception {
        OkResponse r = (OkResponse) send(new DeleteMovieRequest(token, id));
        return r.message();
    }

    // ===================================
    //  Review operations
    // ===================================

    public String requestCreateReview(Review review) throws Exception {
        CreatedResponse r = (CreatedResponse) send(new CreateReviewRequest(token, review));
        return r.message();
    }

    public ReviewListResponse requestOwnReviewList() throws Exception {
        return (ReviewListResponse) send(new ListReviewsRequest(token, new ReviewFilter(null, userId)));
    }

    public ReviewListResponse requestMovieReviewList(int movieId) throws Exception {
        return (ReviewListResponse) send(new ListReviewsRequest(token, new ReviewFilter(movieId, null)));
    }

    public ReviewListResponse requestReviewList(ReviewFilter filter) throws Exception {
        return (ReviewListResponse) send(new ListReviewsRequest(token, filter));
    }

    public String requestUpdateReview(Review review) throws Exception {
        OkResponse r = (OkResponse) send(new UpdateReviewRequest(token, review));
        return r.message();
    }

    public String requestDeleteReview(int id) throws Exception {
        OkResponse r = (OkResponse) send(new DeleteReviewRequest(token, id));
        return r.message();
    }
}
