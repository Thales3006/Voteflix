package com.thales.controller;

import javafx.fxml.FXML;

public class AppController {
    private static AppController instance;

    @FXML private void initialize(){
        AppController.getInstance();
        ServerController.getInstance();
    }

    public static AppController getInstance(){
        if(instance == null){
            instance = new AppController();
        }
        return instance;
    }

    public void handleMessage(String message){
        
    }
}
