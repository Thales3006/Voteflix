package com.thales.server.service;

import java.sql.SQLException;

import org.everit.json.schema.ValidationException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.Request;
import com.thales.common.model.User;
import com.thales.common.utils.JsonValidator;
import com.thales.server.controller.AppController;
import com.thales.server.network.ClientHandler;

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

    private JsonObject createStatus(String status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status);
        return json;
    }

    // ===================================
    //  Main Handler
    // ===================================

    public void handleMessage(String message, ClientHandler client){
        try{
            Request op = validator.getRequest(message);
    
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);

            client.sendMessage(switch (op) {
            case LOGIN -> handleLogin(jsonObject, client);
            case LOGOUT -> handleLogout(jsonObject, client);
            case CREATE_USER -> handleRegister(jsonObject, client);
            case LIST_USERS -> handleListUsers(jsonObject, client);
            case UPDATE_OWN_USER -> handleUpdateOwnUser(jsonObject, client);
            case UPDATE_USER -> handleUpdateUser(jsonObject, client);
            case DELETE_OWN_USER -> handleDeleteOwnUser(jsonObject, client);
            case DELETE_USER -> handleDeleteUser(jsonObject, client);
            
            default -> throw new RuntimeException("Unknown Operation");
            });

            if(op == Request.LOGOUT || op == Request.DELETE_OWN_USER){
                client.close();
            }
        } catch (ValidationException e){
            System.err.println(e.toString());
            e.printStackTrace();
            client.sendMessage(createStatus("400").toString());
        }
    }

    // ===================================
    //  Client Interaction Handlers
    // ===================================

    private String handleLogin(JsonObject jsonObject, ClientHandler client){
        String username = jsonObject.get("usuario").getAsString();
        String password = jsonObject.get("senha").getAsString();

        synchronized (usersLock) {
            if(users.containsKey(username)){
                return createStatus("401").toString();
            }
            users.put(username, new User(username, password));
        }
        client.setUsername(username);

        String token = null;
        try{
            if(!database.checkUser(username, password)){
                log("asdasds");
                return createStatus("401").toString();
            }
            int id = database.getUserId(username);
            token = generateToken(id,username, password);
        } catch (SQLException e){
            log(e.toString());
            return createStatus("500").toString();
        }

        JsonObject json = new JsonObject();  
        json.addProperty("status", "200");
        json.addProperty("token", token);
        
        return json.toString();
    }

    private String handleLogout(JsonObject jsonObject, ClientHandler client){
        String token = jsonObject.get("token").getAsString();

        JsonObject json = new JsonObject();
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT jwt = verifier.verify(token);
            String username = jwt.getClaim("username").asString();

            synchronized (usersLock){
                if (users.remove(username) == null) {
                    throw new NullPointerException("User not logged");
                }
            }

            json.addProperty("status", "200");
        } catch (Exception e) {
            return createStatus("401").toString();
        }
        
        return json.toString();
    }

    private String handleRegister(JsonObject jsonObject, ClientHandler client){
        JsonObject user = jsonObject.get("usuario").getAsJsonObject();
        String username = user.get("nome").getAsString();
        String password = user.get("senha").getAsString();

        try {
            if (database.createUser(username, password)) {
                return createStatus("200").toString();
            }
            return createStatus("409").toString();
        } catch (SQLException e) {
            log(e.toString());
            return createStatus("500").toString();
        }
    }

    private String handleListUsers(JsonObject jsonObject, ClientHandler client){
        return createStatus("200").toString();
    }

    private String handleUpdateOwnUser(JsonObject jsonObject, ClientHandler client){
        String token = jsonObject.get("token").getAsString();
        JsonObject usuario = jsonObject.get("usuario").getAsJsonObject();
        String newPassword = usuario.get("senha").getAsString();

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT jwt = verifier.verify(token);
            
            int id = jwt.getClaim("id").asInt();

            if (database.updateUser(id, newPassword)) {
                return createStatus("200").toString();
            }
            return createStatus("404").toString();
        } catch (Exception e) {
            log(e.toString());
            return createStatus("401").toString();
        }
    }

    private String handleUpdateUser(JsonObject jsonObject, ClientHandler client){
        String token = jsonObject.get("token").getAsString();
        JsonObject usuario = jsonObject.get("usuario").getAsJsonObject();
        String newPassword = usuario.get("senha").getAsString();
        int id = Integer.parseInt(jsonObject.get("id").getAsString());

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT jwt = verifier.verify(token);
            int tokenId = jwt.getClaim("id").asInt();
            if(!database.isAdmin(tokenId)){
                return createStatus("401").toString();
            }

            if (database.updateUser(id, newPassword)) {
                return createStatus("200").toString();
            }
            return createStatus("404").toString();
        } catch (Exception e) {
            log(e.toString());
            return createStatus("401").toString();
        }
    }

    private String handleDeleteOwnUser(JsonObject jsonObject, ClientHandler client){
        String token = jsonObject.get("token").getAsString();

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT jwt = verifier.verify(token);
            
            int id = jwt.getClaim("id").asInt();

            if (database.deleteUser(id)) {
                return createStatus("200").toString();
            }
            return createStatus("404").toString();
        } catch (Exception e) {
            log(e.toString());
            return createStatus("401").toString();
        }
    }

    private String handleDeleteUser(JsonObject jsonObject, ClientHandler client){
        String token = jsonObject.get("token").getAsString();
        int userId = Integer.parseInt(jsonObject.get("id").getAsString());
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
            DecodedJWT jwt = verifier.verify(token);
            int tokenId = jwt.getClaim("id").asInt();
            if(!database.isAdmin(tokenId)){
                return createStatus("401").toString();
            }
            
            if (database.deleteUser(userId)) {
            return createStatus("200").toString();
            }
            return createStatus("404").toString();
        } catch (Exception e) {
            log(e.toString());
            return createStatus("401").toString();
        }
    }

    public void handleClosed(ClientHandler client) {
        String username = client.getUsername();
        if (username != null) {
            synchronized (usersLock){
            users.remove(username);
            }
        }
    }

}
