package com.thales.server.controller;

import com.thales.server.network.ServerListener;
import com.thales.server.service.ServerService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {

    private ServerService serverService;
    private ServerListener serverListener;

    @FXML private TextArea log_output;

    @FXML public void initialize() {
        serverService = new ServerService(this);
        serverListener = new ServerListener(serverService, 20737);
    }

    @FXML public void appendToLog(String message){
        log_output.appendText(message + "\n");
    }

    
}
