package com.thales.server.service;

import com.thales.common.model.*;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response.*;
import com.thales.common.utils.ResponseBuilder;
import com.thales.common.utils.Validator;
import com.thales.server.repository.MovieRepository;
import com.thales.server.repository.ReviewRepository;
import com.thales.server.database.SQLiteDatabase;
import com.thales.server.repository.UserRepository;
import com.thales.server.network.ClientHandler;

public class ServerService {

    private final Validator validator = Validator.getInstance();
    private final ResponseBuilder responseBuilder = new ResponseBuilder();
    private final UserRepository userRepo;
    private final MovieRepository movieRepo;
    private final ReviewRepository reviewRepo;
    private final JwtService jwtService;
    private final UserService userService;
    private final MovieService movieService;
    private final ReviewService reviewService;

    public ServerService() {
        String basedir = System.getProperty("server.basedir", ".");
        SQLiteDatabase database = new SQLiteDatabase(basedir + "/data/voteflix.db");
        this.userRepo = new UserRepository(database);
        this.movieRepo = new MovieRepository(database);
        this.reviewRepo = new ReviewRepository(database);

        String secretKey = System.getenv().getOrDefault("JWT_SECRET", "256-bit-secret-key-placeholder");
        this.jwtService = new JwtService(secretKey);
        this.userService = new UserService(userRepo, jwtService);
        this.movieService = new MovieService(movieRepo, userRepo, jwtService);
        this.reviewService = new ReviewService(reviewRepo, userRepo, jwtService);

        try {
            validator.loadSchemas();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void log(String message) {
        String time = java.time.LocalTime.now().withNano(0).toString();
        System.out.println("[" + time + "] " + message);
    }

    public void handleMessage(String message, ClientHandler client) {
        Request request = null;
        try {
            request = validator.parseRequest(message);
            Response response = dispatch(request);
            client.sendMessage(responseBuilder.serialize(response));
        } catch (StatusException e) {
            System.err.println(e.toString());
            client.sendMessage(responseBuilder.serializeError(e));
        } catch (Exception e) {
            e.printStackTrace();
            client.sendMessage(responseBuilder.serializeError(
                new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR)));
        } finally {
            if (request instanceof LogoutRequest || request instanceof DeleteUserRequest) {
                client.close();
            }
        }
    }

    private Response dispatch(Request request) {
        return switch (request) {
            case LoginRequest r -> handleLogin(r);
            case LogoutRequest r -> handleLogout(r);

            case CreateUserRequest r -> userService.create(r);
            case GetUserRequest r -> userService.get(r);
            case ListUsersRequest r -> userService.list(r);
            case UpdateUserRequest r -> userService.update(r);
            case DeleteUserRequest r -> userService.delete(r);

            case CreateMovieRequest r -> movieService.create(r);
            case ListMoviesRequest r -> movieService.list(r);
            case UpdateMovieRequest r -> movieService.update(r);
            case DeleteMovieRequest r -> movieService.delete(r);

            case CreateReviewRequest r -> reviewService.create(r);
            case ListReviewsRequest r -> reviewService.list(r);
            case UpdateReviewRequest r -> reviewService.update(r);
            case DeleteReviewRequest r -> reviewService.delete(r);
        };
    }

    private Response handleLogin(LoginRequest req) {
        if (!userRepo.checkCredentials(req.username(), req.password())) {
            throw new StatusException(ErrorStatus.UNAUTHORIZED, "Invalid credentials");
        }
        int id = userRepo.findIdByUsername(req.username());
        String token = jwtService.generateToken(id, req.username());
        return new LoginResponse("OK", token, id);
    }

    private Response handleLogout(LogoutRequest req) {
        jwtService.verifyAndGetUserId(req.token());
        return new OkResponse("OK");
    }
}
