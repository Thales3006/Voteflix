package com.thales.client.service;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thales.common.model.User;

import com.thales.client.model.StatusException;
import com.thales.client.network.ClientSocket;

import lombok.Data;

@Data
public class ClientService {

    private static ClientService instance;
    private ClientSocket socket;
    private String token;
    private boolean isAdmin;
    private final Gson gson = new Gson(); 


    private ClientService(){
        this.socket = new ClientSocket();
    }

    public static ClientService getInstance(){
        if(instance == null){
            instance = new ClientService();
        }
        return instance;
    }

    public void connect(String IP, int port) throws IOException, UnknownHostException{
        socket.connect(IP, port);
    }

    public void close(){
        socket.close();
    }

    private static boolean statusOk(JsonObject json){
        String status = getStatus(json);
        if (status == null) {
            return false;
        }
        return status.equals("200") || status.equals("201");
    }

    private static String getStatus(JsonObject json){
        return json.has("status")? json.get("status").getAsString() : null;
    }

    private void verifyStatus(JsonObject json) throws StatusException {
        if(!statusOk(json)){
            throw new StatusException(getStatus(json));
        }
    }

    // ===================================
    //  Requests
    // ===================================

    public void requestRegister(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "CRIAR_USUARIO");
        JsonObject usuario = new JsonObject();
        usuario.addProperty("nome", user.getUsername());
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
    }

    public void requestLogin(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGIN");
        json.addProperty("usuario", user.getUsername());
        json.addProperty("senha", user.getPassword());
        socket.sendMessage(json.toString());
        
        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
        
        JsonElement token = response.get("token");
        this.token = token.getAsString();
    }

    public void requestLogout() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGOUT");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());
        
        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
        
        token = null;
        socket.close();
    }

    public void requestUpdateOwnUser(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EDITAR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        JsonObject usuario = new JsonObject();
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
    }

    public void requestUpdateUser(User user, Integer id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "ADMIN_EDITAR_USUARIO");
        json.addProperty("token", token);
        json.addProperty("id", id.toString());
        JsonObject usuario = new JsonObject();
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
    }

    public void requestDeleteOwnUser() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EXCLUIR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
    }

    public void requestDeleteUser(Integer id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "ADMIN_EXCLUIR_USUARIO");
        json.addProperty("token", token);
        json.addProperty("id", id.toString());
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        verifyStatus(response);
    }
}
