package com.thales.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class MenuController extends SceneController {
    @FXML Button logoutButton;
    @FXML Button userButton;
    @FXML Button movieButton;
    @FXML Button reviewButton;
    @FXML BorderPane mainContent;

    @FXML private void initialize() {
        handle(null, () -> switchContent(mainContent, "/movies_page.fxml"));
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML private void HandleLogoutButton(ActionEvent event){
        handle(event, () -> {
            clientService.requestLogout(); 
        }, 
            () -> SceneController.switchPage(event, "/login_page.fxml")
        );
    }

    @FXML private void HandleUserButton(ActionEvent event){
        handle(event, () -> switchContent(mainContent, "/user_page.fxml"));
    }

    @FXML private void HandleMovieButton(ActionEvent event){
        handle(event, () -> switchContent(mainContent, "/movies_page.fxml"));
    }

    @FXML private void HandleReviewButton(ActionEvent event){
        handle(event, () -> switchContent(mainContent, "/reviews_page.fxml"));
    }

}
