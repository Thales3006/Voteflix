package com.thales.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class VoteController extends FXMLController {
    @FXML Button logoutButton;

    protected void onInitialize(){ }

    @FXML private void HandleLogoutButton(ActionEvent event){
        runWithPopup(() -> {
            clientService.requestLogout();
            SceneController.switchTo(event, "/login.fxml");
        });
    }

}
