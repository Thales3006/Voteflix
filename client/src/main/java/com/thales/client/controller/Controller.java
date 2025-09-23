package com.thales.client.controller;

import com.google.gson.JsonObject;
import com.thales.client.service.ClientService;

import javafx.fxml.FXML;
import lombok.Data;

@Data
public abstract class Controller {

    protected static final ClientService clientService = ClientService.getInstance();

    @FXML private void initialize() {
        ClientService.getInstance().setActiveController(this);
        onInitialize();
    }

    protected void onInitialize() {}

    public abstract void handleMessage(JsonObject json);
}
