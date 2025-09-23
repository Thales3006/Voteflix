package com.thales.client.service;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thales.common.model.User;

import com.thales.client.controller.FXMLController;
import com.thales.client.network.ClientSocket;

import lombok.Data;

@Data
public class ClientService {

    private static ClientService instance;
    private ClientSocket socket;
    private FXMLController activeController;
    private String token;
    private final Gson gson = new Gson(); 


    private ClientService(){
        this.socket = new ClientSocket();
    }

    public static ClientService getInstance(){
        if(instance == null){
            instance = new ClientService();
        }
        return instance;
    }

    public void connect(String IP, int port) throws IOException, UnknownHostException{
        socket.connect(IP, port);
    }

    public void close(){
        socket.close();
    }

    // ===================================
    //  Requests
    // ===================================

    public void requestLogin(User user) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGIN");
        json.addProperty("usuario", user.getUsername());
        json.addProperty("senha", user.getPassword());
        socket.sendMessage(json.toString());
        
        String response = socket.waitMessage();
        JsonObject responseJson = gson.fromJson(response, JsonObject.class);
        JsonElement token = responseJson.get("token");
        if(token == null){
            throw new IOException("No token field");
        }
        this.token = token.getAsString();
    }
}
