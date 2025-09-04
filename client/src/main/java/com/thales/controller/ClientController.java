package com.thales.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClientController {
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;

    private NetworkController network;

    @FXML
    public void initialize() {
        network = new NetworkController();
        network.connect("localhost", 20666, this);
    }

    @FXML private void HandleLogin(){
        network.sendMessage(usernameField.getText());
    }

    @FXML private void HandleRegister(){

    }

    public void handleMessage(String message){
        
    }

}
