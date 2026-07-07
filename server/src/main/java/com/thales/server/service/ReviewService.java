
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
import com.thales.common.model.Review;
import com.thales.common.utils.ErrorTable;
import com.thales.common.utils.JsonValidator;

import com.thales.server.network.ClientHandler;
import com.thales.server.service.DatabaseService;

public class ReviewService {
    private final DatabaseService database;
    private final Gson gson = new Gson();
    private final String secretKey = "256-bit-secret-key-placeholder";
    private JsonValidator validator = JsonValidator.getInstance();

    public ReviewService(DatabaseService database) {
        this.database = database;
    }

    private void setReviewUsername(Review review) {
        if(review.getUserID() == null) {
            return;
        }
        try{
        review.setUsername(database.getUsername(review.getUserID()));
        } catch (StatusException e) {}
    }

    public String handleCreateReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        Review review = Review.fromJson(jsonObject.get("review").getAsJsonObject());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        review.setUserID(tokenId);
        database.createReview(review);

        return createStatus(ErrorStatus.CREATED).toString();
    }

    public String handleListOwnReviews(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();

        JsonObject json = createStatus(ErrorStatus.OK);
        json.add("reviews", gson.toJsonTree(database.getUserReviews(tokenId).stream()
            .map(review -> {setReviewUsername(review); return review.toJson();})
            .toList()));
        return json.toString();

    }

    public String handleListReviews(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        int movieId = Integer.parseInt(jsonObject.get("id_filme").getAsString());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        verifier.verify(token);

        JsonObject json = createStatus(ErrorStatus.OK);
        Movie movie = database.getMovie(movieId);
        json.add("filme", movie.toJson());
        json.add("reviews", gson.toJsonTree(database.getMovieReviews(movieId).stream()
            .map(review -> {setReviewUsername(review); return review.toJson();})
            .toList()));
        return json.toString();
    }

    public String handleUpdateReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        Review review = Review.fromJson(jsonObject.get("review").getAsJsonObject());
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();

        Review oldReview = database.getReview(review.getID());
        if (!oldReview.getUserID().equals(tokenId)) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        database.updateReview(review);

        JsonObject json = createStatus(ErrorStatus.OK);
        return json.toString();
    }

    public String handleDeleteReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        int reviewId = Integer.parseInt(jsonObject.get("id").getAsString());
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();

        Review oldReview = database.getReview(reviewId);
        if ((!oldReview.getUserID().equals(tokenId)) && (!database.isAdmin(tokenId))) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        database.deleteReview(reviewId);
        
        JsonObject json = createStatus(ErrorStatus.OK);
        return json.toString();
    }

    public JsonObject createStatus(ErrorStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.getCode());
        json.addProperty("mensagem", ErrorTable.getInstance().get(status).getSecond());
        return json;
    }
 }
