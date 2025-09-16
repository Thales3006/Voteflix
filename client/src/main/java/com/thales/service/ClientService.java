package com.thales.service;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.thales.controller.AppController;
import com.thales.model.User;
import com.thales.network.ClientSocket;

import javafx.application.Platform;

public class ClientService {
    private AppController appController;
    private ClientSocket clientSocket;

    public ClientService(ClientSocket clientSocket, AppController appController){
        this.clientSocket = clientSocket;
        this.appController = appController;
    }

    public void handleMessage(String message){
        Platform.runLater(() -> appController.getRegisterButton().setText(message));
    }

    public void requestLogin(User user) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGIN");
        json.addProperty("usuario", user.getUsername());
        json.addProperty("senha", user.getPassword());

        clientSocket.sendMessage(json.toString());
    }
}
