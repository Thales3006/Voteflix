package com.thales.client.controller;

import com.thales.client.util.Validate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
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

        usernameField.setTextFormatter(new TextFormatter<>((Change c) ->
            c.getControlNewText().matches("[A-Za-z0-9_]{0,20}") ? c : null));

        portField.setTextFormatter(new TextFormatter<>((Change c) ->
            c.getControlNewText().matches("\\d{0,5}") ? c : null));
    }

    // ===================================
    // UI interaction handlers
    // ===================================

    @FXML private void HandleConnectButton(ActionEvent event) {
        handle(event, () -> {
            Validate.intRange(portField.getText(), "Port", 1, 65535);
            clientService.connect(IPField.getText(), Integer.parseInt(portField.getText()));
        });
    }

    @FXML private void HandleDisconnectButton(ActionEvent event) {
        handle(event, () -> clientService.close());
    }

    @FXML private void HandleLoginButton(ActionEvent event) {
        handle(event, () -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            Validate.length(username, "Username", 3, 20);
            Validate.alphanumeric(username, "Username");
            Validate.length(password, "Password", 3, 20);
            Validate.alphanumeric(password, "Password");
            clientService.requestLogin(username, password);
            SceneController.switchPage(event, "/menu_page.fxml");
        });
    }

    @FXML private void HandleRegisterButton(ActionEvent event) {
        handle(event, () -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            Validate.length(username, "Username", 3, 20);
            Validate.alphanumeric(username, "Username");
            Validate.length(password, "Password", 3, 20);
            Validate.alphanumeric(password, "Password");
            String message = clientService.requestCreateUser(username, password);
            feedback(message);
        });
    }
}
