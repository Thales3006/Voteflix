package com.thales.common.model;

import lombok.Data;

@Data
public class User {

    private Integer id;
    private String username;
    private String password;

    public User(Integer id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password){
        this(null, username, password);
    }

    public User(Integer id, String username){
        this(id, username, null);
    }
}
