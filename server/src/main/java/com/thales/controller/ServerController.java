package com.thales.controller;

import javafx.fxml.FXML;

public class ServerController {
    private NetworkController network;

    @FXML private void initialize(){
        network = new NetworkController();
        network.create(20666, this);
    }

    public void handleMessage(String message){
        
    }
}
