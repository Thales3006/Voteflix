package com.thales.client.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainPageController extends SceneController {

    @FXML private BorderPane currentPage;

    @FXML protected void initialize() {
        handle(null, () -> switchContent(currentPage, "/login_page.fxml"));
    }
}
