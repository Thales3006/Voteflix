package com.thales.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AppController {
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;

    private static AppController instance;

    @FXML
    public void initialize() {
        AppController.getInstance();
        ClientController.getInstance();
    }

    public static AppController getInstance(){
        if(instance == null){
            instance = new AppController();
        }
        return instance;
    }

    @FXML private void HandleLogin(){
        ClientController.getInstance().sendMessage(usernameField.getText());
    }

    @FXML private void HandleRegister(){

    }

    public void handleMessage(String message){
        
    }

}
