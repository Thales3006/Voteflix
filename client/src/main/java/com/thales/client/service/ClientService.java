package com.thales.client.service;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.thales.common.model.Movie;
import com.thales.common.model.Response;
import com.thales.common.model.Review;
import com.thales.common.model.User;
import com.thales.common.utils.JsonValidator;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

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

    public String requestRegister(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "CRIAR_USUARIO");
        JsonObject usuario = new JsonObject();
        usuario.addProperty("nome", user.getUsername());
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.CREATED);

        return response.get("mensagem").getAsString();
    }

    public String requestLogin(User user) throws Exception {
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
        
        return response.get("mensagem").getAsString();
    }

    public String requestLogout() throws Exception {
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
        
        return response.get("mensagem").getAsString();
    }

    public String requestUpdateOwnUser(User user) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EDITAR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        JsonObject usuario = new JsonObject();
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public String requestUpdateUser(User user, int id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "ADMIN_EDITAR_USUARIO");
        json.addProperty("token", token);
        json.addProperty("id", Integer.toString(id));
        JsonObject usuario = new JsonObject();
        usuario.addProperty("senha", user.getPassword());
        json.add("usuario", usuario);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public Pair<String,User> requestOwnUser() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LISTAR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.USER_INFO);

        String username = response.get("usuario").getAsString();
        return new Pair<>(response.get("mensagem").getAsString(), new User(null, username, null));
    }

    public Pair<String,ArrayList<User>> requestUserList() throws Exception{
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
            return new Pair<>(response.get("mensagem").getAsString(),users);
    }

    public String requestDeleteOwnUser() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EXCLUIR_PROPRIO_USUARIO");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public String requestDeleteUser(int id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "ADMIN_EXCLUIR_USUARIO");
        json.addProperty("token", token);
        json.addProperty("id", Integer.toString(id));
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public String requestCreateMovie(Movie movie) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "CRIAR_FILME");
        json.addProperty("token", token);
        JsonObject filme = movie.toJson();
        json.add("filme", filme);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.CREATED);

        return response.get("mensagem").getAsString();
    }

    public Pair<String,ArrayList<Movie>> requestMovieList() throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "LISTAR_FILMES");
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.MOVIE_LIST);

        ArrayList<Movie> movies = new ArrayList<>();
        JsonElement filmesElement = response.get("filmes");
        for (JsonElement element : filmesElement.getAsJsonArray()) {
            movies.add(Movie.fromJson(element.getAsJsonObject()));
        }
        return new Pair<>(response.get("mensagem").getAsString(),movies);
    }

    public String requestUpdateMovie(Movie movie) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EDITAR_FILME");
        json.addProperty("token", token);
        JsonObject filme = movie.toJson();
        json.add("filme", filme);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public String requestDeleteMovie(int id) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "EXCLUIR_FILME");
        json.addProperty("token", token);
        json.addProperty("id", Integer.toString(id));
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.OK);

        return response.get("mensagem").getAsString();
    }

    public String requestCreateReview(Review review) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "CRIAR_REVIEW");
        json.addProperty("token", token);
        json.add("review", review.toJson());
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.CREATED);

        return response.get("mensagem").getAsString();
    }

    public Pair<String, ArrayList<Review>> requestMovieReviewList(int movieId) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("operacao", "BUSCAR_FILME_ID");
        json.addProperty("id_filme", Integer.toString(movieId));
        json.addProperty("token", token);
        socket.sendMessage(json.toString());

        JsonObject response = gson.fromJson(socket.waitMessage(), JsonObject.class);
        validator.validateResponce(response, Response.REVIEW_LIST);

        ArrayList<Review> reviews = new ArrayList<>();
        JsonElement filmesElement = response.get("reviews");
        for (JsonElement element : filmesElement.getAsJsonArray()) {
            reviews.add(Review.fromJson(element.getAsJsonObject()));
        }
        return new Pair<>(response.get("mensagem").getAsString(),reviews);
    }
}
