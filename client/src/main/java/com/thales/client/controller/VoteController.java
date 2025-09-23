package com.thales.client.controller;

import com.thales.client.model.StatusException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class VoteController extends FXMLController {
    @FXML Button logoutButton;

    protected void onInitialize(){ }

    @FXML private void HandleLogoutButton(ActionEvent event){
        try {
            clientService.requestLogout();
            SceneController.switchTo(event, "/login.fxml");
        }  catch (StatusException e) {
            showStatusError(e.getStatus());
        } catch (Exception e) {
            showPopup("Request Error", e.toString());
        }
    }

}
