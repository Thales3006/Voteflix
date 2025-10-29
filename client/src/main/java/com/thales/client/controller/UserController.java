package com.thales.client.controller;

import java.util.ArrayList;

import com.thales.common.model.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class UserController extends SceneController {
    @FXML private Button updateUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button listUserButton;
    @FXML private TextField passwordField;
    @FXML private Label usernameLabel;
    @FXML private ListView<User> userListView;
    @FXML private VBox usersVbox;

    private User selectedUser = null;

    @FXML protected void initialize(){
        
        
        if(!clientService.isAdmin()){
            return;
        }
        usersVbox.setDisable(false);

        userListView.setCellFactory(_ -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            if (empty || user == null) {
                setGraphic(null);
            } else {
                VBox vbox = new VBox(
                new Label("Username: " + user.getUsername()),
                new Label("ID: " + user.getId())
                );
                setGraphic(vbox);
            }
            }
        });
    }
    
    private void refreshUsers() throws Exception {
        var request = clientService.requestUserList();
        ArrayList<User> users = request.getSecond();
        userListView.getItems().clear();
        for(User user : users){
            userListView.getItems().add(user);
        }
        feedback(request.getFirst());
    }

    private void listOwnUser() throws Exception {
        var request = clientService.requestOwnUser();
        selectedUser = request.getSecond();
        usernameLabel.setText(selectedUser.getUsername());
        feedback(request.getFirst());
        
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML void HandleListUserButton(ActionEvent event){
        handle(event, () -> listOwnUser());
    }

    @FXML void HandleUpdateUserButton(ActionEvent event){
        handle(event, () -> {
            if (selectedUser == null){
                showPopup("Error", "You should select someone to update");
                return;
            }
            String request = clientService.getUsername().equals(selectedUser.getUsername())?
                clientService.requestUpdateOwnUser(
                    new User("", passwordField.getText())):
                clientService.requestUpdateUser(
                    new User("", passwordField.getText()), selectedUser.getId());
            feedback(request);
        });
    }

    @FXML void HandleDeleteUserButton(ActionEvent event){
        handle(event, () -> {
            if (selectedUser == null){
                showPopup("Error", "You should select someone to delete");
                return;
            }
            String request = clientService.getUsername().equals(selectedUser.getUsername())?
                clientService.requestDeleteOwnUser() :
                clientService.requestDeleteUser(selectedUser.getId());
            switchPage(event, "/login_page.fxml");
            feedback(request);
        });
    }

    @FXML void HandleSelectUser(MouseEvent event){
        handle(null, () -> {
            selectedUser = userListView.getSelectionModel().getSelectedItem();
            usernameLabel.setText(selectedUser.getUsername());
        });
    }

    @FXML void HandleRefreshUsersButton(ActionEvent event){
        handle(event, () -> refreshUsers());
    }
}
