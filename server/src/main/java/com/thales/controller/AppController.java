package com.thales.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Data;

@Data
public class AppController {
    @FXML private TextArea log_output;

    private static AppController instance;

    public AppController(){
        if(instance == null){
            instance = this;
        }
    }

    @FXML public void initialize() {
        ServerController.getInstance();
    }

    public static AppController getInstance() {
        return instance;
    }

    public void handleMessage(String message){

    }

    public void appendToLog(String message){
        Platform.runLater(()->{
            log_output.appendText(message + "\n");
        });
    }
}
