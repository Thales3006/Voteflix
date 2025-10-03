package com.thales.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class MainPageController extends SceneController {

    @FXML private BorderPane currentPage;
    @FXML private TextField IPField;
    @FXML private TextField portField;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;

    @FXML protected void initialize() {
        handle(() -> switchContent(currentPage, "/login_page.fxml"));

        connectButton.disableProperty().bind(clientService.getSocket().getRunning());
        disconnectButton.disableProperty().bind(clientService.getSocket().getRunning().not());
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleConnectButton(ActionEvent event){
        handle(() -> clientService.connect(IPField.getText(), Integer.parseInt(portField.getText())));
    }

    @FXML private void HandleDisconnectButton(ActionEvent event){
        handle(() -> clientService.close());
    }
}
