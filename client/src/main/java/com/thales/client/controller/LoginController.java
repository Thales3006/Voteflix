package com.thales.client.controller;

import com.thales.common.model.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginController extends SceneController {

    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField IPField;
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;

    @FXML protected void initialize() {
        connectButton.disableProperty().bind(clientService.getSocket().getRunning());
        disconnectButton.disableProperty().bind(clientService.getSocket().getRunning().not());
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLoginButton(ActionEvent event){
        handle(() -> {
            clientService.requestLogin(new User(usernameField.getText(), passwordField.getText()));
            SceneController.switchPage(event, "/main_page.fxml");
        });
    }

    @FXML private void HandleRegisterButton(ActionEvent event){
        handle(() -> {
            clientService.requestRegister(new User(usernameField.getText(), passwordField.getText()));
        });
    }

    @FXML private void HandleConnectButton(ActionEvent event){
        handle(() -> {
            clientService.connect(IPField.getText(), Integer.parseInt(portField.getText()));
        });
    }

    @FXML private void HandleDisconnectButton(ActionEvent event){
        handle(() -> {
        clientService.close();
        });
    }

}
