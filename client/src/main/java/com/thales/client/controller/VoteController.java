package com.thales.client.controller;

import com.thales.client.service.ClientService;

public class VoteController extends FXMLController {
    protected void onInitialize(){
        System.out.println("Token:" + ClientService.getInstance().getToken());
    }

}
