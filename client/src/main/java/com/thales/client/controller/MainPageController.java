package com.thales.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class MainPageController extends SceneController {
    @FXML Button logoutButton;
    @FXML Button userButton;
    @FXML Button movieButton;
    @FXML Button reviewButton;
    @FXML AnchorPane mainContent;

    @FXML protected void initialize(){ }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLogoutButton(ActionEvent event){
        handle(
            () -> clientService.requestLogout(), 
            () -> SceneController.switchPage(event, "/login_page.fxml")
        );
    }

    @FXML private void HandleUserButton(ActionEvent event){
        handle(() -> switchContent(mainContent, "/user_page.fxml"));
    }

    @FXML private void HandleMovieButton(ActionEvent event){
        handle(() -> switchContent(mainContent, "/movies_page.fxml"));
    }

    @FXML private void HandleReviewButton(ActionEvent event){
        handle(() -> switchContent(mainContent, "/reviews_page.fxml"));
    }

}
