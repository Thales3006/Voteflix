package com.thales.common.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thales.common.model.*;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class Validator {

    private static Validator instance;
    private static final Gson gson = new Gson();

    private Map<Operation, JsonSchema> requestSchemas;
    private Map<ResponseKind, JsonSchema> responseSchemas;
    private JsonSchema basicRequestSchema;
    private JsonSchema basicResponseSchema;
    private boolean loaded = false;

    private Validator() {}

    public static Validator getInstance() {
        if (instance == null) instance = new Validator();
        return instance;
    }

    public void loadSchemas() {
        if (loaded) return;
        basicRequestSchema = JsonSchema.loadFromResource("/schemas/requests/basic_request.json");
        basicResponseSchema = JsonSchema.loadFromResource("/schemas/responses/basic_response.json");

        requestSchemas = new EnumMap<>(Operation.class);
        for (Operation op : Operation.values()) {
            if (op == Operation.UNKNOWN) continue;
            requestSchemas.put(op, JsonSchema.loadFromResource(
                "/schemas/requests/" + op.name().toLowerCase() + ".json"));
        }

        responseSchemas = new EnumMap<>(ResponseKind.class);
        for (ResponseKind kind : ResponseKind.values()) {
            responseSchemas.put(kind, JsonSchema.loadFromResource(
                "/schemas/responses/" + kind.name().toLowerCase() + ".json"));
        }
        loaded = true;
    }

    public Request parseRequest(String json) throws StatusException {
        try {
            basicRequestSchema.validate(json);
        } catch (Exception e) {
            throw new StatusException(ErrorStatus.BAD_REQUEST, "Malformed request");
        }

        JsonObject obj = gson.fromJson(json, JsonObject.class);
        String code = obj.get("operation").getAsString();
        Operation op = Operation.fromCode(code);

        if (op == Operation.UNKNOWN) {
            throw new StatusException(ErrorStatus.BAD_REQUEST, "Unknown operation: " + code);
        }

        try {
            requestSchemas.get(op).validate(json);
        } catch (Exception e) {
            String msg = e.getMessage();
            boolean missingField = msg != null && msg.contains("required key");
            throw new StatusException(
                missingField ? ErrorStatus.UNPROCESSABLE_ENTITY : ErrorStatus.BAD_REQUEST,
                msg
            );
        }

        return deserializeRequest(op, obj);
    }

    public Response parseResponse(String json, Operation forOperation) throws StatusException {
        try {
            basicResponseSchema.validate(json);
        } catch (Exception e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR, "Malformed response");
        }

        JsonObject obj = gson.fromJson(json, JsonObject.class);
        String statusCode = obj.get("status").getAsString();

        if (!statusCode.startsWith("2")) {
            ErrorStatus status = ErrorStatus.fromCode(statusCode);
            String message = obj.has("message") ? obj.get("message").getAsString() : status.getMessage();
            throw new StatusException(status, message);
        }

        ResponseKind kind = forOperation.getResponseKind();
        try {
            responseSchemas.get(kind).validate(json);
        } catch (Exception e) {
            throw new StatusException(ErrorStatus.INTERNAL_SERVER_ERROR, "Response validation failed: " + e.getMessage());
        }

        return deserializeResponse(kind, obj);
    }

    private Request deserializeRequest(Operation op, JsonObject obj) {
        return switch (op) {
            case LOGIN -> new LoginRequest(
                obj.get("username").getAsString(),
                obj.get("password").getAsString()
            );
            case LOGOUT -> new LogoutRequest(obj.get("token").getAsString());

            case CREATE_USER -> {
                JsonObject user = obj.get("user").getAsJsonObject();
                yield new CreateUserRequest(new User(
                    user.get("username").getAsString(),
                    user.get("password").getAsString()
                ));
            }
            case GET_USER -> new GetUserRequest(obj.get("token").getAsString());
            case LIST_USERS -> new ListUsersRequest(
                obj.get("token").getAsString(),
                obj.has("filter") ? parseUserFilter(obj.get("filter").getAsJsonObject()) : null
            );
            case UPDATE_USER -> {
                JsonObject user = obj.get("user").getAsJsonObject();
                yield new UpdateUserRequest(
                    obj.get("token").getAsString(),
                    new User(user.get("id").getAsInt(), null, user.get("password").getAsString())
                );
            }
            case DELETE_USER -> new DeleteUserRequest(
                obj.get("token").getAsString(),
                obj.get("id").getAsInt()
            );

            case CREATE_MOVIE -> new CreateMovieRequest(
                obj.get("token").getAsString(),
                Movie.fromJson(obj.get("movie").getAsJsonObject())
            );
            case LIST_MOVIES -> new ListMoviesRequest(
                obj.get("token").getAsString(),
                obj.has("filter") ? parseMovieFilter(obj.get("filter").getAsJsonObject()) : null
            );
            case UPDATE_MOVIE -> new UpdateMovieRequest(
                obj.get("token").getAsString(),
                Movie.fromJson(obj.get("movie").getAsJsonObject())
            );
            case DELETE_MOVIE -> new DeleteMovieRequest(
                obj.get("token").getAsString(),
                obj.get("id").getAsInt()
            );

            case CREATE_REVIEW -> new CreateReviewRequest(
                obj.get("token").getAsString(),
                Review.fromJson(obj.get("review").getAsJsonObject())
            );
            case LIST_REVIEWS -> new ListReviewsRequest(
                obj.get("token").getAsString(),
                obj.has("filter") ? parseReviewFilter(obj.get("filter").getAsJsonObject()) : null
            );
            case UPDATE_REVIEW -> new UpdateReviewRequest(
                obj.get("token").getAsString(),
                Review.fromJson(obj.get("review").getAsJsonObject())
            );
            case DELETE_REVIEW -> new DeleteReviewRequest(
                obj.get("token").getAsString(),
                obj.get("id").getAsInt()
            );

            default -> throw new StatusException(ErrorStatus.BAD_REQUEST);
        };
    }

    private UserFilter parseUserFilter(JsonObject f) {
        String username = f.has("username") ? f.get("username").getAsString() : null;
        return new UserFilter(username);
    }

    private MovieFilter parseMovieFilter(JsonObject f) {
        String genre = f.has("genre") ? f.get("genre").getAsString() : null;
        Integer year = f.has("year") ? f.get("year").getAsInt() : null;
        return new MovieFilter(genre, year);
    }

    private ReviewFilter parseReviewFilter(JsonObject f) {
        Integer movieId = f.has("movie_id") ? f.get("movie_id").getAsInt() : null;
        Integer userId = f.has("user_id") ? f.get("user_id").getAsInt() : null;
        return new ReviewFilter(movieId, userId);
    }

    private Response deserializeResponse(ResponseKind kind, JsonObject obj) {
        String message = obj.has("message") ? obj.get("message").getAsString() : "";
        return switch (kind) {
            case OK -> new OkResponse(message);
            case CREATED -> new CreatedResponse(message);
            case LOGIN -> new LoginResponse(
                message,
                obj.get("token").getAsString(),
                obj.get("id").getAsInt()
            );
            case MOVIE_LIST -> {
                var movies = new ArrayList<Movie>();
                for (var el : obj.getAsJsonArray("movies")) {
                    movies.add(Movie.fromJson(el.getAsJsonObject()));
                }
                yield new MovieListResponse(message, movies);
            }
            case REVIEW_LIST -> {
                var reviews = new ArrayList<Review>();
                for (var el : obj.getAsJsonArray("reviews")) {
                    reviews.add(Review.fromJson(el.getAsJsonObject()));
                }
                yield new ReviewListResponse(message, reviews);
            }
            case USER_INFO -> new UserInfoResponse(message, obj.get("username").getAsString());
            case USER_LIST -> {
                var users = new ArrayList<User>();
                for (var el : obj.getAsJsonArray("users")) {
                    JsonObject u = el.getAsJsonObject();
                    users.add(new User(u.get("id").getAsInt(), u.get("username").getAsString()));
                }
                yield new UserListResponse(message, users);
            }
        };
    }
}
