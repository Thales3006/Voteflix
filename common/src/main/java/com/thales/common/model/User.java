package com.thales.common.model;

import lombok.Data;

@Data
public class User {

    private int ID;
    private String username;
    private String password;

    public User(String username, String password, int ID){
        this.username = username;
        this.password = password;
        this.ID = ID;
    }
}
