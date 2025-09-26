package com.thales.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class UserController extends SceneController {
    @FXML private Button updateUserButton;
    @FXML private Button deleteUserButton;
    @FXML private TextField passwordField;
    @FXML private ListView<String> userListView;

    @FXML protected void initialize(){ 
        userListView.getItems().addAll("User1", "User2", "User3");
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML void HandleUpdateUserButton(ActionEvent event){
        handle(
            () -> {System.out.println(passwordField.getText());}
        );
    }

    @FXML void HandleDeleteUserButton(ActionEvent event){
        handle(
            () -> {System.out.println(passwordField.getText());}
        );
    }

    @FXML void HandleSelectUser(MouseEvent event){
        handle(() -> {
            String selectedItem = userListView.getSelectionModel().getSelectedItem();
            System.out.println("Selected: " + selectedItem);
        });
    }
}
