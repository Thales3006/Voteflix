package com.thales.controller;

import java.io.IOException;

import com.thales.model.User;
import com.thales.network.ClientSocket;
import com.thales.service.ClientService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
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
        clientSocket.setClientService(clientService);

        connectButton.disableProperty().bind(clientSocket.getRunning());
        disconnectButton.disableProperty().bind(clientSocket.getRunning().not());
    }
    
    public void showPopup(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLoginButton(){
        try {
        clientService.requestLogin(new User(usernameField.getText(), passwordField.getText()));
        } catch (IOException e) {
            showPopup("Request Error", e.toString());
        }
    }

    @FXML private void HandleRegisterButton(){

    }

    @FXML private void HandleConnectButton(){
        try{
            clientSocket.connect(IPField.getText(), Integer.parseInt(portField.getText()));
        } catch (IOException e) {
            showPopup("Connection Error", e.toString());
            System.err.println(e);
        } catch (Exception e) {
            showPopup("Error", e.toString());
            System.err.println(e);
        }
    }

    @FXML private void HandleDisconnectButton(){
        clientSocket.close();
    }

}
