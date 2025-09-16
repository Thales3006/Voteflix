package com.thales.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.thales.controller.AppController;
import com.thales.model.User;
import com.thales.network.ClientSocket;

import javafx.application.Platform;

public class ClientService {
    private AppController appController;
    private ClientSocket clientSocket;
    private final Gson gson = new Gson();

    public ClientService(ClientSocket clientSocket, AppController appController){
        this.appController = appController;
    }

    public void handleMessage(String message){
        String parsedJson = gson.fromJson(message,String.class);

        Platform.runLater(() -> appController.getRegisterButton().setText(parsedJson));
    }

    public void requestLogin(User user){
        try {
            clientSocket.sendMessage(gson.toJson(user));
        } catch (IOException e){
            System.err.println(e);
        }
    }
}
