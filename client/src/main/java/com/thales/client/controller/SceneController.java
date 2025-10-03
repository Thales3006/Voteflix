package com.thales.client.controller;

import java.io.IOException;

import com.thales.client.model.StatusException;
import com.thales.client.service.ClientService;
import com.thales.common.utils.ErrorTable;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import lombok.Data;

@Data
public abstract class SceneController {

    protected static final ClientService clientService = ClientService.getInstance();

    protected void showPopup(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===================================
    //  PopUps
    // ===================================

    protected void showStatusError(String status){
        String errorMessage = ErrorTable.getInstance().get(status);
        if(errorMessage == null){
            showPopup("Status Error", "Unknown Status return:" + status);
            return;
        }
        showPopup("Status Error", errorMessage);
    }

    // ===================================
    //  General Handlers
    // ===================================

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

    protected void handle(ThrowingRunnable action, ThrowingRunnable finallyAction) {
        handle(action);
        handle(finallyAction);
    }

    // ===================================
    //  Static methods
    // ===================================

    protected static void switchPage(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneController.class.getResource(fxmlPath));
        Parent root = loader.load();

        Node node = (Node) event.getSource();
        while (node != null) {
            if (node instanceof BorderPane && "currentPage".equals(node.getId())) {
                ((BorderPane) node).setCenter(root);
                return;
            }
            node = node.getParent();
        }
    }

    protected static void switchContent(BorderPane parent, String fxmlPath) throws IOException {
        BorderPane pane = FXMLLoader.load(SceneController.class.getResource(fxmlPath));
        parent.setCenter(pane);
    }
}
