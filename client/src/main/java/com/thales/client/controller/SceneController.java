package com.thales.client.controller;

import java.io.IOException;

import com.thales.client.service.ClientService;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;

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

    // ===================================
    //  PopUps
    // ===================================

    private Alert styledAlert(AlertType type) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(
            SceneController.class.getResource("/style.css").toExternalForm()
        );
        return alert;
    }

    protected void showPopup(String title, String message) {
        Alert alert = styledAlert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showStatusError(StatusException e) {
        Alert alert = styledAlert(AlertType.ERROR);
        alert.setTitle("Error " + e.getStatus().getCode());
        alert.setHeaderText(null);
        alert.setContentText(e.getUserMessage());
        alert.showAndWait();
    }

    protected void feedback(String message) {
        Alert alert = styledAlert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===================================
    //  General Handlers
    // ===================================

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }

    protected void handle(ActionEvent event, ThrowingRunnable action) {
        try {
            action.run();
        } catch (StatusException e) {
            showStatusError(e);
            if (e.getStatus().equals(ErrorStatus.UNAUTHORIZED)) {
                try {
                    switchPage(event, "/login_page.fxml");
                } catch (Exception ee) { System.err.println(ee.toString()); }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);
            showPopup("Exception Error", e.toString());
        }
    }

    protected void handle(ActionEvent event, ThrowingRunnable action, ThrowingRunnable finallyAction) {
        handle(event, action);
        handle(event, finallyAction);
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
