package com.thales.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private static AppController instance;

    public AppController(){
        if(instance == null){
            instance = this;
        }
    }

    @FXML public void initialize() {
        ClientController.getInstance();
    }

    public static AppController getInstance() {
        return instance;
    }

    @FXML private void HandleLogin(){
        ClientController.getInstance().sendMessage(usernameField.getText());
    }

    @FXML private void HandleRegister(){

    }

    public void handleMessage(String message){
        Platform.runLater(() -> {
            registerButton.setText(message);
        });
    }

}
