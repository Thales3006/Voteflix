package com.thales.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Request;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.common.utils.ErrorTable;
import com.thales.common.utils.JsonValidator;

import com.thales.server.network.ClientHandler;
import com.thales.server.service.DatabaseService;

public class UserService {
    private final DatabaseService database;
    private final Gson gson = new Gson();
    private final String secretKey = "256-bit-secret-key-placeholder";
    private JsonValidator validator = JsonValidator.getInstance();

    public UserService(DatabaseService database) {
        this.database = database;
    }

    public String handleRegister(JsonObject jsonObject, ClientHandler client) throws StatusException {
        JsonObject user = jsonObject.get("usuario").getAsJsonObject();
        String username = user.get("nome").getAsString();
        String password = user.get("senha").getAsString();

        database.createUser(username, password);
        return createStatus(ErrorStatus.CREATED).toString();
    }

    public String handleListOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();

        String username = database.getUsername(id);
        JsonObject json = createStatus(ErrorStatus.OK);
        json.addProperty("usuario", username);
        return json.toString();
    }

    public String handleListUsers(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        if(!database.isAdmin(tokenId)){
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        
        JsonObject json = createStatus(ErrorStatus.OK);
        json.add("usuarios", gson.toJsonTree(database.getUsers().stream()
            .map(user -> {
                JsonObject userObj = new JsonObject();
                userObj.addProperty("id", user.getId().toString());
                userObj.addProperty("nome", user.getUsername());
                return userObj;
            })
            .toList()));
        return json.toString();
    }

    public String handleUpdateOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JsonObject usuario = jsonObject.get("usuario").getAsJsonObject();
        String newPassword = usuario.get("senha").getAsString();

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        
        int id = jwt.getClaim("id").asInt();

        database.updateUser(id, newPassword);
        return createStatus(ErrorStatus.OK).toString();
    }

    public String handleUpdateUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JsonObject usuario = jsonObject.get("usuario").getAsJsonObject();
        String newPassword = usuario.get("senha").getAsString();
        int id = Integer.parseInt(jsonObject.get("id").getAsString());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        if(!database.isAdmin(tokenId)){
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }

        database.updateUser(id, newPassword);
        return createStatus(ErrorStatus.OK).toString();
    }

    public String handleDeleteOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        
        int id = jwt.getClaim("id").asInt();
        String username = database.getUsername(id);
        if ("admin".equals(username)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        database.deleteUser(id);

        return createStatus(ErrorStatus.OK).toString();
    }

    public String handleDeleteUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        int userId = Integer.parseInt(jsonObject.get("id").getAsString());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        if(!database.isAdmin(tokenId)){
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        
        String username = database.getUsername(userId);
        if ("admin".equals(username)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        
        database.deleteUser(userId);
        return createStatus(ErrorStatus.OK).toString();
    }


    public JsonObject createStatus(ErrorStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.getCode());
        json.addProperty("mensagem", ErrorTable.getInstance().get(status).getSecond());
        return json;
    }
}
