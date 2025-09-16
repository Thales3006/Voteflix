package com.thales.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.controller.AppController;
import com.thales.network.ClientHandler;

import javafx.application.Platform;

public class ServerService {

    private AppController appController;
    private final Gson gson = new Gson();

    public ServerService(AppController appController){
        this.appController = appController;
    }

    public void log(String message){
        String time = java.time.LocalTime.now().withNano(0).toString();
        Platform.runLater(() -> appController.appendToLog("[" + time + "] " + message));
    }

    public void handleMessage(String message, ClientHandler client){
        JsonObject jsonObject = gson.fromJson(message, com.google.gson.JsonObject.class);
        String operacao = jsonObject.has("operacao") ? jsonObject.get("operacao").getAsString() : null;
        if (operacao == null) {
            JsonObject response = new JsonObject();
            response.addProperty("status", "400");
            client.sendMessage(gson.toJson(response));
            return;
        }

        String response;
        switch (operacao) {
        case "LOGIN":
            response = handleLogin(jsonObject, client);
            break;
        default:
            JsonObject res = new JsonObject();
            res.addProperty("status", "400");
            response = gson.toJson(res);
            break;
        }
        client.sendMessage(response);
        
    }

    private String handleLogin(JsonObject jsonObject, ClientHandler client){
        String username = jsonObject.has("username") ? jsonObject.get("username").getAsString() : null;
        //String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;

        JsonObject json = new JsonObject();
        json.addProperty("status", "200");
        json.addProperty("token", generateToken(username));

        

        return json.toString();
    }

    private String generateToken(String username){
        String secretKey = "256-bit-secret-key-placeholder"; 
        com.auth0.jwt.algorithms.Algorithm algorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(secretKey);
        return com.auth0.jwt.JWT.create()
            .withSubject(username)
            .withClaim("username", username)
            .sign(algorithm);
    }
}
