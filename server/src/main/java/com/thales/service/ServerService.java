package com.thales.service;

import com.thales.controller.AppController;
import com.thales.network.ClientHandler;

import javafx.application.Platform;

public class ServerService {

    private AppController appController;

    public ServerService(AppController appController){
        this.appController = appController;
    }

    public void log(String message){
        String time = java.time.LocalTime.now().withNano(0).toString();
        Platform.runLater(() -> appController.appendToLog("[" + time + "] " + message));
    }

    public void handleMessage(String message, ClientHandler client){
        client.sendMessage(message.toUpperCase());
    }
}
