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

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLoginButton(ActionEvent event){
        handle(event, () -> {
            String request = clientService.requestLogin(new User(usernameField.getText(), passwordField.getText()));
            feedback(request);
            SceneController.switchPage(event, "/menu_page.fxml");
        });
    }

    @FXML private void HandleRegisterButton(ActionEvent event){
        handle(event, () -> {
            String request = clientService.requestRegister(new User(usernameField.getText(), passwordField.getText()));
            feedback(request);
        });
    }

}
