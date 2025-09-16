package com.thales.controller;

import java.io.IOException;

import com.thales.network.ClientSocket;
import com.thales.service.ClientService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {

    private ClientSocket clientSocket;
    private ClientService clientService;

    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField IPField;
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;

    @FXML private void initialize() {
        clientSocket = new ClientSocket();
        clientService = new ClientService(clientSocket, this); 

        connectButton.disableProperty().bind(clientSocket.getRunning());
        disconnectButton.disableProperty().bind(clientSocket.getRunning().not());
    }
    
    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLoginButton(){
        try{
            clientSocket.sendMessage(usernameField.getText());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @FXML private void HandleRegisterButton(){

    }

    @FXML private void HandleConnectButton(){
        try{
            clientSocket.connect(IPField.getText(), Integer.parseInt(portField.getText()));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @FXML private void HandleDisconnectButton(){
        clientSocket.close();
    }

}
