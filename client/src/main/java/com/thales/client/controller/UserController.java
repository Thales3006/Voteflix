package com.thales.client.controller;

import com.thales.common.model.User;

import java.util.ArrayList;
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
    @FXML private TextField passwordField;
    @FXML private Label usernameLabel;
    @FXML private ListView<User> userListView;
    @FXML private VBox usersVbox;

    private User selectedUser;

    @FXML protected void initialize(){
        handle(() ->{selectedUser = clientService.requestOwnUser();});
        usernameLabel.setText(selectedUser.getUsername());
        
        if(!clientService.isAdmin()){
            return;
        }
        HandleRefreshUsersButton(null);
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

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML void HandleUpdateUserButton(ActionEvent event){
        handle(
            () -> {
                if(!clientService.getUsername().equals("admin")){
                    clientService
                        .requestUpdateOwnUser(new User("", passwordField.getText()));
                    return;
                }
                clientService
                    .requestUpdateUser(new User("", passwordField.getText()), selectedUser.getId());
            });
    }

    @FXML void HandleDeleteUserButton(ActionEvent event){
        handle(
            () -> {
                if(!clientService.getUsername().equals("admin")){
                    clientService.requestDeleteOwnUser();
                    return;
                }
                clientService.requestDeleteUser(selectedUser.getId());
                HandleRefreshUsersButton(event);
            });
    }

    @FXML void HandleSelectUser(MouseEvent event){
        handle(() -> {
            selectedUser = userListView.getSelectionModel().getSelectedItem();
            usernameLabel.setText(selectedUser.getUsername());
        });
    }

    @FXML void HandleRefreshUsersButton(ActionEvent event){
        handle(
            () -> {
                ArrayList<User> users = clientService.requestUserList();
                userListView.getItems().clear();
                for(User user : users){
                    userListView.getItems().add(user);
                }
        });
    }
}
