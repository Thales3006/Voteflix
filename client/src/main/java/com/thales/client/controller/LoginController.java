package com.thales.client.controller;

import java.io.IOException;

import com.thales.common.model.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginController extends FXMLController {

    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField IPField;
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;

    @Override protected void onInitialize() {
        connectButton.disableProperty().bind(clientService.getSocket().getRunning());
        disconnectButton.disableProperty().bind(clientService.getSocket().getRunning().not());
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

    @FXML private void HandleLoginButton(ActionEvent event){
        try {
            clientService.requestLogin(new User(usernameField.getText(), passwordField.getText()));
            SceneController.switchTo(event, "/voting.fxml");
        } catch (IOException e) {
            showPopup("Request Error", e.toString());
        }
    }

    @FXML private void HandleRegisterButton(ActionEvent event){
        try{
        SceneController.switchTo(event, "/voting.fxml");
        } catch (IOException e) {
            showPopup("Error", e.toString());
            System.err.println(e);
        }
    }

    @FXML private void HandleConnectButton(ActionEvent event){
        try{
            clientService.connect(IPField.getText(), Integer.parseInt(portField.getText()));
        } catch (IOException e) {
            showPopup("Connection Error", e.toString());
            System.err.println(e);
        } catch (Exception e) {
            showPopup("Error", e.toString());
            System.err.println(e);
        }
    }

    @FXML private void HandleDisconnectButton(ActionEvent event){
        clientService.close();
    }

}
