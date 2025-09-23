package com.thales.client.controller;

import com.thales.client.model.StatusException;
import com.thales.client.service.ClientService;
import com.thales.common.model.ErrorTable;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import lombok.Data;

@Data
public abstract class FXMLController {

    protected static final ClientService clientService = ClientService.getInstance();

    @FXML private void initialize() {
        ClientService.getInstance().setActiveController(this);
        onInitialize();
    }

    protected void onInitialize() {}

    protected void showPopup(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showStatusError(String status){
        String errorMessage = ErrorTable.getInstance().get(status);
        if(errorMessage == null){
            showPopup("Status Error", "Unknown Status return:" + status);
            return;
        }
        showPopup("Status Error", errorMessage);
    }

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }

    protected void handle(ThrowingRunnable action) {
        try {
            action.run();
        } catch (StatusException e) {
            showStatusError(e.getStatus());
        } catch (Exception e) {
            showPopup("Exception Error", e.toString());
            System.err.println(e);
        }
    }

    protected void handle(ThrowingRunnable action , ThrowingRunnable finallyAction) {
        handle(action);
        handle(finallyAction);
    }
}
