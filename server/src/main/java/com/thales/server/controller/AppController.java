package com.thales.server.controller;

import com.thales.common.model.User;
import com.thales.server.network.ServerListener;
import com.thales.server.service.ServerService;

import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {

    private ServerService serverService;
    private ServerListener serverListener;

    @FXML private Accordion userList;
    @FXML private TextArea log_output;
    @FXML private Button onButton;
    @FXML private TextField portInput;

    @FXML public void initialize() {
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            } else {
                return null;
            }
        });
        portInput.setTextFormatter(textFormatter);
    }

    @FXML public void appendToLog(String message){
        log_output.appendText(message + "\n");
    }

    @FXML public void updateUserList(ObservableMap<String, User> users) {
        userList.getPanes().clear();
        for (String username : users.keySet()) {
            User user = users.get(username);
            TitledPane pane = new TitledPane(username, new Label(
                "Password: " + user.getPassword()
                ));
            userList.getPanes().add(pane);
        }
    }

    @FXML void HandleOnButton(ActionEvent event){
        this.serverService = new ServerService(this);
        this.serverListener = new ServerListener(serverService, Integer.parseInt(portInput.getText()));

        onButton.setDisable(true);
        portInput.setDisable(true);


    }
}
