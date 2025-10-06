package com.thales.client.service;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thales.common.model.Movie;
import com.thales.common.model.Response;
import com.thales.common.model.User;
import com.thales.common.utils.JsonValidator;

import java.util.ArrayList;

import com.thales.client.network.ClientSocket;

import lombok.Data;

@Data
public class ClientService {

    private static ClientService instance;
    private ClientSocket socket;
    private String token;
    private boolean isAdmin;
    private String username;
    private final Gson gson; 
    private JsonValidator validator;


    private ClientService(){
        this.socket = new ClientSocket();
        this.isAdmin = false;
        this.gson = new Gson();
        this.validator = JsonValidator.getInstance();
        try{
            validator.loadSchemas();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
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
        validator.validateResponce(response, Response.OK);
    }

    public void requestLogin(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGIN");
        json.addProperty("usuario", user.getUsername());
        json.addProperty("senha", user.getPassword());
        socket.sendMessage(json.toString());
        
        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.LOGIN);
        
        JsonElement token = response.get("token");
        this.token = token.getAsString();

        username = user.getUsername();
        isAdmin = "admin".equals(username);
        
    }

    public void requestLogout() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LOGOUT");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());
        
        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);
        
        token = null;
        username = null;
        isAdmin = false;
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
        validator.validateResponce(response, Response.OK);
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
        validator.validateResponce(response, Response.OK);
    }

    public ArrayList<User> requestUserList() throws Exception{
            JsonObject json = new JsonObject();
            json.addProperty("operacao", "LISTAR_USUARIOS");
            json.addProperty("token", token);
            socket.sendMessage(json.toString());

            JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
            validator.validateResponce(response, Response.USER_LIST);

            ArrayList<User> users = new ArrayList<>();
            JsonElement usuariosElement = response.get("usuarios");
            for (JsonElement element : usuariosElement.getAsJsonArray()) {
                JsonObject obj = element.getAsJsonObject();
                Integer id = Integer.parseInt(obj.get("id").getAsString());
                String username = obj.get("nome").getAsString();
                users.add(new User(id, username));
            }
            return users;
    }

    public void requestDeleteOwnUser() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EXCLUIR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);
    }

    public void requestDeleteUser(Integer id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "ADMIN_EXCLUIR_USUARIO");
        json.addProperty("token", token);
        json.addProperty("id", id.toString());
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);
    }

    public ArrayList<Movie> requestMovieList() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LISTAR_FILMES");
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.MOVIE_LIST);

        ArrayList<Movie> movies = new ArrayList<>();
        JsonElement filmesElement = response.get("filmes");
        for (JsonElement element : filmesElement.getAsJsonArray()) {
            movies.add(Movie.fromJson(element.getAsJsonObject()));
        }
        return movies;
    }
}
