package com.thales.client.service;

import java.io.IOException;

import com.thales.client.network.ClientSocket;
import com.thales.common.model.*;
import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse.*;
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
        isAdmin = false;
        connected.set(false);
    }

    private AppResponse send(AppRequest request) throws IOException {
        String json = serializer.serialize(request);
        String response = new ClientSocket().sendAndReceive(serverIP, serverPort, json);
        return validator.parseResponse(response, request.operation());
    }

    // ===================================
    //  User operations
    // ===================================

    public String requestCreateUser(String username, String password) throws Exception {
        CreatedResponse r = (CreatedResponse) send(new CreateUserRequest(username, password));
        return r.message();
    }

    public void requestLogin(String username, String password) throws Exception {
        LoginResponse r = (LoginResponse) send(new LoginRequest(username, password));
        this.token = r.token();
        this.username = username;
        this.isAdmin = "admin".equals(username);
    }

    public void requestLogout() throws Exception {
        send(new LogoutRequest(token));
        token = null;
        username = null;
        isAdmin = false;
    }

    public String requestUpdateOwnUser(String password) throws Exception {
        OkResponse r = (OkResponse) send(new UpdateOwnUserRequest(token, password));
        return r.message();
    }

    public String requestAdminUpdateUser(int id, String password) throws Exception {
        OkResponse r = (OkResponse) send(new AdminUpdateUserRequest(token, id, password));
        return r.message();
    }

    public UserInfoResponse requestOwnUser() throws Exception {
        return (UserInfoResponse) send(new ListOwnUserRequest(token));
    }

    public UserListResponse requestUserList() throws Exception {
        return (UserListResponse) send(new ListUsersRequest(token));
    }

    public String requestDeleteOwnUser() throws Exception {
        OkResponse r = (OkResponse) send(new DeleteOwnUserRequest(token));
        return r.message();
    }

    public String requestAdminDeleteUser(int id) throws Exception {
        OkResponse r = (OkResponse) send(new AdminDeleteUserRequest(token, id));
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
        return (MovieListResponse) send(new ListMoviesRequest(token));
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
        return (ReviewListResponse) send(new ListOwnReviewsRequest(token));
    }

    public ReviewListResponse requestMovieReviewList(int movieId) throws Exception {
        return (ReviewListResponse) send(new ListReviewsRequest(token, movieId));
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
