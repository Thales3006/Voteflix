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
import com.thales.common.model.Movie;
import com.thales.common.utils.ErrorTable;
import com.thales.common.utils.JsonValidator;

import com.thales.server.network.ClientHandler;
import com.thales.server.service.DatabaseService;

public class MovieService {
    private final DatabaseService database;
    private final Gson gson = new Gson();
    private final String secretKey = "256-bit-secret-key-placeholder";
    private JsonValidator validator = JsonValidator.getInstance();

    public MovieService(DatabaseService database) {
        this.database = database;
    }
    
    public String handleCreateMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        Movie movie = Movie.fromJson(jsonObject.get("filme").getAsJsonObject());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        
        if (!database.isAdmin(tokenId)) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }

        database.createMovie(movie);
        return createStatus(ErrorStatus.CREATED).toString();
    }

    public String handleListMovies(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        verifier.verify(token);
        
        JsonObject json = createStatus(ErrorStatus.OK);
        json.add("filmes", gson.toJsonTree(database.getMovies().stream()
            .map(movie -> movie.toJson())
            .toList()));
        return json.toString();

    }

    public String handleUpdateMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        Movie movie = Movie.fromJson(jsonObject.get("filme").getAsJsonObject());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        
        if (!database.isAdmin(tokenId)) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }

        database.updateMovie(movie);
        return createStatus(ErrorStatus.OK).toString();

    }

    public String handleDeleteMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        int movieId = Integer.parseInt(jsonObject.get("id").getAsString());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        
        if (!database.isAdmin(tokenId)) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }

        database.deleteMovie(movieId);
        return createStatus(ErrorStatus.OK).toString();
    }

    public JsonObject createStatus(ErrorStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.getCode());
        json.addProperty("mensagem", ErrorTable.getInstance().get(status).getSecond());
        return json;
    }
}
