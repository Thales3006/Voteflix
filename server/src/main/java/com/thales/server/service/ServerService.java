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
            case CREATE_USER -> handleLogin(jsonObject, client);
            case LIST_USERS -> handleLogout(jsonObject, client);
            case UPDATE_OWN_USER -> handleLogout(jsonObject, client);
            case UPDATE_USER -> handleLogin(jsonObject, client);
            
            default -> throw new RuntimeException("Unknown Operation");
            });

            if(op == Request.LOGOUT){
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
        String username = jsonObject.has("usuario") ? jsonObject.get("usuario").getAsString() : null;
        String password = jsonObject.has("senha") ? jsonObject.get("senha").getAsString() : null;

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
        String token = jsonObject.has("token") ? jsonObject.get("token").getAsString() : null;

        JsonObject json = new JsonObject();
        String secretKey = "256-bit-secret-key-placeholder";
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
            json.addProperty("status", "401");
        }
        
        return json.toString();
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
