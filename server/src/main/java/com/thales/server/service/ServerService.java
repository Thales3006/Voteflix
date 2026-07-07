package com.thales.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Movie;
import com.thales.common.model.Request;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.common.utils.ErrorTable;
import com.thales.common.utils.JsonValidator;
import com.thales.server.controller.AppController;
import com.thales.server.network.ClientHandler;
import com.thales.server.service.UserService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ServerService {

    private AppController appController;
    private final Gson gson = new Gson();
    private final Object usersLock = new Object();
    private final String secretKey = "256-bit-secret-key-placeholder";
    private ObservableMap<String, User> users = FXCollections.observableHashMap();
    private JsonValidator validator = JsonValidator.getInstance();
    private DatabaseService database = new DatabaseService();
    private final UserService userService;
    private final MovieService movieService;
    private final ReviewService reviewService;

    public ServerService(AppController appController){
        this.appController = appController;

        users.addListener((MapChangeListener<String, User>) _ -> {
            Platform.runLater(() -> appController.updateUserList(users));
        });
        try{
            validator.loadSchemas();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        this.userService = new UserService(database);
        this.movieService = new MovieService(database);
        this.reviewService = new ReviewService(database);
    }

    public void log(String message){
        String time = java.time.LocalTime.now().withNano(0).toString();
        System.out.println("[" + time + "] " + message);
        Platform.runLater(() -> appController.appendToLog("[" + time + "] " + message));
    }

    private String generateToken(int id, String username, String password){
        String secretKey = "256-bit-secret-key-placeholder"; 
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return com.auth0.jwt.JWT.create()
            .withSubject(username)
            .withClaim("id", id)
            .withClaim("username", username)
            .withClaim("password", password)
            .sign(algorithm);
    }

    private JsonObject createStatus(ErrorStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.getCode());
        json.addProperty("mensagem", ErrorTable.getInstance().get(status).getSecond());
        return json;
    }

    // ===================================
    //  Main Handler
    // ===================================

    public void handleMessage(String message, ClientHandler client){
        try{
            Request op = validator.getRequest(message);
            
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            validator.validateRequest(jsonObject, op);

            client.sendMessage(switch (op) {
            case LOGIN -> handleLogin(jsonObject, client);
            case LOGOUT -> handleLogout(jsonObject, client);

            case CREATE_USER -> userService.handleRegister(jsonObject, client);
            case LIST_OWN_USER -> userService.handleListOwnUser(jsonObject, client);
            case LIST_USERS -> userService.handleListUsers(jsonObject, client);
            case UPDATE_OWN_USER -> userService.handleUpdateOwnUser(jsonObject, client);
            case UPDATE_USER -> userService.handleUpdateUser(jsonObject, client);
            case DELETE_OWN_USER -> userService.handleDeleteOwnUser(jsonObject, client);
            case DELETE_USER -> userService.handleDeleteUser(jsonObject, client);

            case CREATE_MOVIE -> movieService.handleCreateMovie(jsonObject, client);
            case LIST_MOVIES -> movieService.handleListMovies(jsonObject, client);
            case UPDATE_MOVIE -> movieService.handleUpdateMovie(jsonObject, client);
            case DELETE_MOVIE -> movieService.handleDeleteMovie(jsonObject, client);

            case CREATE_REVIEW -> reviewService.handleCreateReview(jsonObject, client);
            case LIST_OWN_REVIEWS -> reviewService.handleListOwnReviews(jsonObject, client);
            case LIST_REVIEWS -> reviewService.handleListReviews(jsonObject, client);
            case UPDATE_REVIEW -> reviewService.handleUpdateReview(jsonObject, client);
            case DELETE_REVIEW -> reviewService.handleDeleteReview(jsonObject, client);
            
            default -> throw new StatusException(ErrorStatus.BAD_REQUEST);
            });

            if(op == Request.LOGOUT || op == Request.DELETE_OWN_USER){
                client.close();
            }
        } catch (JWTVerificationException e){
            System.err.println(e.toString());
            client.sendMessage(createStatus(ErrorStatus.UNAUTHORIZED).toString());
        } catch (StatusException e){
            System.err.println(e.toString());
            client.sendMessage(createStatus(e.getStatus()).toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
            client.sendMessage(createStatus(ErrorStatus.INTERNAL_SERVER_ERROR).toString());
        }
    }

    // ===================================
    //  Client Interaction Handlers
    // ===================================

    private String handleLogin(JsonObject jsonObject, ClientHandler client) throws StatusException {
        String username = jsonObject.get("usuario").getAsString();
        String password = jsonObject.get("senha").getAsString();

        
        String token = null;
        if(!database.checkUser(username, password)){
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        int id = database.getUserId(username);
        token = generateToken(id,username, password);

        JsonObject json = createStatus(ErrorStatus.OK);  
        json.addProperty("token", token);

        synchronized (usersLock) {
            if(!users.containsKey(username)){
                users.put(username, new User(username, password));
            }
        }
        
        return json.toString();
    }

    private String handleLogout(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();

        String username = database.getUsername(id);
        JsonObject json = createStatus(ErrorStatus.OK);

        synchronized (usersLock) {
            users.remove(username);
        }

        return json.toString();
    }
}
