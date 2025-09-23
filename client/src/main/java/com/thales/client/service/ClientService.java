package com.thales.client.service;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.User;

import javafx.application.Platform;

import com.thales.client.controller.Controller;
import com.thales.client.network.ClientSocket;

import lombok.Data;

@Data
public class ClientService {

    private static ClientService instance;
    private ClientSocket socket;
    private Controller activeController;


    private ClientService(){
        this.socket = new ClientSocket();
    }

    public static ClientService getInstance(){
        if(instance == null){
            instance = new ClientService();
        }
        return instance;
    }

    public void handleMessage(String message){
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(message, com.google.gson.JsonObject.class);
        if (activeController != null) {
            Platform.runLater(() -> activeController.handleMessage(json));
        } else {
            System.err.println("Nenhum controller ativo para receber a mensagem: " + json);
        }
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
    }
}
