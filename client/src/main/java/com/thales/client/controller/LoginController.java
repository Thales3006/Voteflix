package com.thales.client.controller;

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

    @FXML private void initialize() {
        connectButton.disableProperty().bind(clientService.getConnected());
        disconnectButton.disableProperty().bind(clientService.getConnected().not());
    }

    // ===================================
    // UI interaction handlers
    // ===================================

    @FXML private void HandleConnectButton(ActionEvent event) {
        handle(event, () -> clientService.connect(IPField.getText(), Integer.parseInt(portField.getText())));
    }

    @FXML private void HandleDisconnectButton(ActionEvent event) {
        handle(event, () -> clientService.close());
    }

    @FXML private void HandleLoginButton(ActionEvent event) {
        handle(event, () -> {
            clientService.requestLogin(usernameField.getText(), passwordField.getText());
            SceneController.switchPage(event, "/menu_page.fxml");
        });
    }

    @FXML private void HandleRegisterButton(ActionEvent event) {
        handle(event, () -> {
            String message = clientService.requestCreateUser(usernameField.getText(), passwordField.getText());
            feedback(message);
        });
    }

}
