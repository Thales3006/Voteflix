package com.thales.controller;

import java.io.IOException;

import com.thales.network.ClientSocket;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField IPField;
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;

    @FXML public void initialize() {
        ClientSocket.getInstance().getLastMessage().addListener((_, _, newMsg) -> {
            handleMessage(newMsg);
        });
        connectButton.disableProperty().bind(ClientSocket.getInstance().getRunning());
        disconnectButton.disableProperty().bind(ClientSocket.getInstance().getRunning().not());
    }

    @FXML public void handleMessage(String message){
        registerButton.setText(message);
    }
    
    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLoginButton(){
        try{
            ClientSocket.getInstance().sendMessage(usernameField.getText());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @FXML private void HandleRegisterButton(){

    }

    @FXML private void HandleConnectButton(){
        try{
            ClientSocket.getInstance().connect(IPField.getText(), Integer.parseInt(portField.getText()));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @FXML private void HandleDisconnectButton(){
        ClientSocket.getInstance().close();
    }

}
