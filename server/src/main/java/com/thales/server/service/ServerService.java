package com.thales.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Movie;
import com.thales.common.model.Request;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.common.utils.ErrorTable;
import com.thales.common.utils.JsonValidator;
import com.thales.server.controller.AppController;
import com.thales.server.network.ClientHandler;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ServerService {

    private AppController appController;
    private final Gson gson = new Gson();
    private final Object usersLock = new Object();
    private final String secretKey = "256-bit-secret-key-placeholder";
    private ObservableMap<String, User> users = FXCollections.observableHashMap();
    private JsonValidator validator = JsonValidator.getInstance();
    private DatabaseService database = new DatabaseService();

    public ServerService(AppController appController){
        this.appController = appController;

        users.addListener((MapChangeListener<String, User>) _ -> {
            Platform.runLater(() -> appController.updateUserList(users));
        });
        try{
            validator.loadSchemas();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void log(String message){
        String time = java.time.LocalTime.now().withNano(0).toString();
        System.out.println("[" + time + "] " + message);
        Platform.runLater(() -> appController.appendToLog("[" + time + "] " + message));
    }

    private String generateToken(int id, String username, String password){
        String secretKey = "256-bit-secret-key-placeholder"; 
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return com.auth0.jwt.JWT.create()
            .withSubject(username)
            .withClaim("id", id)
            .withClaim("username", username)
            .withClaim("password", password)
            .sign(algorithm);
    }

    private JsonObject createStatus(ErrorStatus status) {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.getCode());
        json.addProperty("mensagem", ErrorTable.getInstance().get(status).getSecond());
        return json;
    }

    private void setReviewUsername(Review review) {
        if(review.getUserID() == null) {
            return;
        }
        try{
        review.setUsername(database.getUsername(review.getUserID()));
        } catch (StatusException e) {}
    }

    // ===================================
    //  Main Handler
    // ===================================

    public void handleMessage(String message, ClientHandler client){
        try{
            Request op = validator.getRequest(message);
            
            JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
            validator.validateRequest(jsonObject, op);

            client.sendMessage(switch (op) {
            case LOGIN -> handleLogin(jsonObject, client);
            case LOGOUT -> handleLogout(jsonObject, client);

            case CREATE_USER -> handleRegister(jsonObject, client);
            case LIST_OWN_USER -> handleListOwnUser(jsonObject, client);
            case LIST_USERS -> handleListUsers(jsonObject, client);
            case UPDATE_OWN_USER -> handleUpdateOwnUser(jsonObject, client);
            case UPDATE_USER -> handleUpdateUser(jsonObject, client);
            case DELETE_OWN_USER -> handleDeleteOwnUser(jsonObject, client);
            case DELETE_USER -> handleDeleteUser(jsonObject, client);

            case CREATE_MOVIE -> handleCreateMovie(jsonObject, client);
            case LIST_MOVIES -> handleListMovies(jsonObject, client);
            case UPDATE_MOVIE -> handleUpdateMovie(jsonObject, client);
            case DELETE_MOVIE -> handleDeleteMovie(jsonObject, client);

            case CREATE_REVIEW -> handleCreateReview(jsonObject, client);
            case LIST_OWN_REVIEWS -> handleListOwnReviews(jsonObject, client);
            case LIST_REVIEWS -> handleListReviews(jsonObject, client);
            case UPDATE_REVIEW -> handleUpdateReview(jsonObject, client);
            case DELETE_REVIEW -> handleDeleteReview(jsonObject, client);
            
            default -> throw new StatusException(ErrorStatus.BAD_REQUEST);
            });

            if(op == Request.LOGOUT || op == Request.DELETE_OWN_USER){
                client.close();
            }
        } catch (JWTVerificationException e){
            System.err.println(e.toString());
            client.sendMessage(createStatus(ErrorStatus.UNAUTHORIZED).toString());
        } catch (StatusException e){
            System.err.println(e.toString());
            client.sendMessage(createStatus(e.getStatus()).toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
            client.sendMessage(createStatus(ErrorStatus.INTERNAL_SERVER_ERROR).toString());
        }
    }

    // ===================================
    //  Client Interaction Handlers
    // ===================================

    private String handleLogin(JsonObject jsonObject, ClientHandler client) throws StatusException {
        String username = jsonObject.get("usuario").getAsString();
        String password = jsonObject.get("senha").getAsString();

        
        client.setUsername(username);

        String token = null;
        if(!database.checkUser(username, password)){
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        int id = database.getUserId(username);
        token = generateToken(id,username, password);

        JsonObject json = createStatus(ErrorStatus.OK);  
        json.addProperty("token", token);

        synchronized (usersLock) {
            if(!users.containsKey(username)){
                users.put(username, new User(username, password));
            }
        }
        
        return json.toString();
    }

    private String handleLogout(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();

        database.getUsername(id);
        JsonObject json = createStatus(ErrorStatus.OK);  

        handleClosed(client);
    
        return json.toString();
    }

    private String handleRegister(JsonObject jsonObject, ClientHandler client) throws StatusException {
        JsonObject user = jsonObject.get("usuario").getAsJsonObject();
        String username = user.get("nome").getAsString();
        String password = user.get("senha").getAsString();

        database.createUser(username, password);
        return createStatus(ErrorStatus.CREATED).toString();

    }

    private String handleListOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int id = jwt.getClaim("id").asInt();

        String username = database.getUsername(id);
        JsonObject json = createStatus(ErrorStatus.OK);
        json.addProperty("usuario", username);
        return json.toString();
    }

    private String handleListUsers(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleUpdateOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JsonObject usuario = jsonObject.get("usuario").getAsJsonObject();
        String newPassword = usuario.get("senha").getAsString();

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        
        int id = jwt.getClaim("id").asInt();

        database.updateUser(id, newPassword);
        return createStatus(ErrorStatus.OK).toString();
    }

    private String handleUpdateUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleDeleteOwnUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        
        int id = jwt.getClaim("id").asInt();
        String username = database.getUsername(id);
        if ("admin".equals(username)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        database.deleteUser(id);

        handleClosed(client);
        return createStatus(ErrorStatus.OK).toString();
    }

    private String handleDeleteUser(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleCreateMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleListMovies(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        verifier.verify(token);
        
        JsonObject json = createStatus(ErrorStatus.OK);
        json.add("filmes", gson.toJsonTree(database.getMovies().stream()
            .map(movie -> movie.toJson())
            .toList()));
        return json.toString();

    }

    private String handleUpdateMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleDeleteMovie(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleCreateReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        Review review = Review.fromJson(jsonObject.get("review").getAsJsonObject());

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();
        review.setUserID(tokenId);
        database.createReview(review);

        Movie movie = database.getMovie(review.getMovieID());
        Float newRating = (movie.getRating()*movie.getRatingAmount() + review.getRating()) / (movie.getRatingAmount()+1) ;
        movie.setRating(newRating);
        movie.setRatingAmount(movie.getRatingAmount()+1);
        database.updateMovie(movie);

        return createStatus(ErrorStatus.CREATED).toString();
    }

    private String handleListOwnReviews(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleListReviews(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

    private String handleUpdateReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
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

        Movie movie = database.getMovie(review.getMovieID());
        Float newRating = (movie.getRating()*movie.getRatingAmount() - oldReview.getRating() + review.getRating()) / (movie.getRatingAmount()) ;
        movie.setRating(newRating);
        database.updateMovie(movie);

        JsonObject json = createStatus(ErrorStatus.OK);
        return json.toString();
    }

    private String handleDeleteReview(JsonObject jsonObject, ClientHandler client) throws StatusException, JWTVerificationException {
        String token = jsonObject.get("token").getAsString();
        int reviewId = Integer.parseInt(jsonObject.get("id").getAsString());
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey)).build();
        DecodedJWT jwt = verifier.verify(token);
        int tokenId = jwt.getClaim("id").asInt();

        Review oldReview = database.getReview(reviewId);
        if ((!oldReview.getUserID().equals(tokenId)) || (!database.isAdmin(tokenId))) {
            return createStatus(ErrorStatus.FORBIDDEN).toString();
        }
        database.deleteReview(reviewId);

        Movie movie = database.getMovie(oldReview.getMovieID());
        Float newRating = (movie.getRating()*movie.getRatingAmount() - oldReview.getRating()) / (movie.getRatingAmount()-1) ;
        movie.setRating(newRating);
        movie.setRatingAmount(movie.getRatingAmount()-1);
        database.updateMovie(movie);
        
        JsonObject json = createStatus(ErrorStatus.OK);
        return json.toString();
    }
 
    public void handleClosed(ClientHandler client) {
        String username = client.getUsername();
        if (username != null) {
            synchronized (usersLock){
                users.remove(username);
            }
        }
    }

}
