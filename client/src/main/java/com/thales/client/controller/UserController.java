package com.thales.client.controller;

import java.util.List;

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

    @FXML protected void initialize() {

        if (clientService.isAdmin()) {
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
            handle(null, () -> { refreshUsers(); });
        }
        handle(null, () -> { listOwnUser(); });
    }

    private void refreshUsers() throws Exception {
        var response = clientService.requestUserList();
        List<User> users = response.users();
        userListView.getItems().clear();
        for (User user : users) {
            userListView.getItems().add(user);
        }
    }

    private void listOwnUser() throws Exception {
        var response = clientService.requestGetUser();
        selectedUser = new User((Integer) null, response.username());
        usernameLabel.setText(response.username());
    }

    // ===================================
    //  UI interaction handlers
    // ===================================

    @FXML void HandleListUserButton(ActionEvent event) {
        handle(event, () -> listOwnUser());
    }

    @FXML void HandleUpdateUserButton(ActionEvent event) {
        handle(event, () -> {
            if (selectedUser == null) {
                showPopup("Error", "You should select someone to update");
                return;
            }
            String request = clientService.getUsername().equals(selectedUser.getUsername())
                ? clientService.requestUpdateUser(passwordField.getText())
                : clientService.requestAdminUpdateUser(selectedUser.getId(), passwordField.getText());
            if (clientService.isAdmin()) {
                refreshUsers();
            }
        });
    }

    @FXML void HandleDeleteUserButton(ActionEvent event) {
        handle(event, () -> {
            if (selectedUser == null) {
                showPopup("Error", "You should select someone to delete");
                return;
            }
            String request = clientService.getUsername().equals(selectedUser.getUsername())
                ? clientService.requestDeleteUser()
                : clientService.requestAdminDeleteUser(selectedUser.getId());
            if (clientService.getUsername().equals(selectedUser.getUsername())) {
                switchPage(event, "/login_page.fxml");
            }
            if (clientService.isAdmin()) {
                refreshUsers();
            }
        });
    }

    @FXML void HandleSelectUser(MouseEvent event) {
        handle(null, () -> {
            selectedUser = userListView.getSelectionModel().getSelectedItem();
            usernameLabel.setText(selectedUser.getUsername());
        });
    }

    @FXML void HandleRefreshUsersButton(ActionEvent event) {
        handle(event, () -> refreshUsers());
    }
}
