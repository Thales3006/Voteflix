package com.thales.common.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import com.thales.common.model.*;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class Validator {

    private static Validator instance;

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

        JSONObject obj = new JSONObject(json);
        String code = obj.getString("operation");
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

        JSONObject obj = new JSONObject(json);
        String statusCode = obj.getString("status");

        if (!statusCode.startsWith("2")) {
            ErrorStatus status = ErrorStatus.fromCode(statusCode);
            String message = obj.has("message") ? obj.getString("message") : status.getMessage();
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

    private Request deserializeRequest(Operation op, JSONObject obj) {
        return switch (op) {
            case LOGIN -> new LoginRequest(
                obj.getString("username"),
                obj.getString("password")
            );
            case LOGOUT -> new LogoutRequest(obj.getString("token"));

            case CREATE_USER -> {
                JSONObject user = obj.getJSONObject("user");
                yield new CreateUserRequest(new User(
                    user.getString("username"),
                    user.getString("password")
                ));
            }
            case GET_USER -> new GetUserRequest(obj.getString("token"));
            case LIST_USERS -> new ListUsersRequest(
                obj.getString("token"),
                obj.has("filter") ? parseUserFilter(obj.getJSONObject("filter")) : null
            );
            case UPDATE_USER -> {
                JSONObject user = obj.getJSONObject("user");
                yield new UpdateUserRequest(
                    obj.getString("token"),
                    new User(user.getInt("id"), null, user.getString("password"))
                );
            }
            case DELETE_USER -> new DeleteUserRequest(
                obj.getString("token"),
                obj.getInt("id")
            );

            case CREATE_MOVIE -> new CreateMovieRequest(
                obj.getString("token"),
                Movie.fromJson(obj.getJSONObject("movie"))
            );
            case LIST_MOVIES -> new ListMoviesRequest(
                obj.getString("token"),
                obj.has("filter") ? parseMovieFilter(obj.getJSONObject("filter")) : null
            );
            case UPDATE_MOVIE -> new UpdateMovieRequest(
                obj.getString("token"),
                Movie.fromJson(obj.getJSONObject("movie"))
            );
            case DELETE_MOVIE -> new DeleteMovieRequest(
                obj.getString("token"),
                obj.getInt("id")
            );

            case CREATE_REVIEW -> new CreateReviewRequest(
                obj.getString("token"),
                Review.fromJson(obj.getJSONObject("review"))
            );
            case LIST_REVIEWS -> new ListReviewsRequest(
                obj.getString("token"),
                obj.has("filter") ? parseReviewFilter(obj.getJSONObject("filter")) : null
            );
            case UPDATE_REVIEW -> new UpdateReviewRequest(
                obj.getString("token"),
                Review.fromJson(obj.getJSONObject("review"))
            );
            case DELETE_REVIEW -> new DeleteReviewRequest(
                obj.getString("token"),
                obj.getInt("id")
            );

            default -> throw new StatusException(ErrorStatus.BAD_REQUEST);
        };
    }

    private UserFilter parseUserFilter(JSONObject f) {
        String username = f.has("username") ? f.getString("username") : null;
        return new UserFilter(username);
    }

    private MovieFilter parseMovieFilter(JSONObject f) {
        String genre = f.has("genre") ? f.getString("genre") : null;
        Integer year = f.has("year") ? f.getInt("year") : null;
        return new MovieFilter(genre, year);
    }

    private ReviewFilter parseReviewFilter(JSONObject f) {
        Integer movieId = f.has("movie_id") ? f.getInt("movie_id") : null;
        Integer userId = f.has("user_id") ? f.getInt("user_id") : null;
        return new ReviewFilter(movieId, userId);
    }

    private Response deserializeResponse(ResponseKind kind, JSONObject obj) {
        String message = obj.has("message") ? obj.getString("message") : "";
        return switch (kind) {
            case OK -> new OkResponse(message);
            case CREATED -> new CreatedResponse(message);
            case LOGIN -> new LoginResponse(
                message,
                obj.getString("token"),
                obj.getInt("id")
            );
            case MOVIE_LIST -> {
                var movies = new ArrayList<Movie>();
                JSONArray arr = obj.getJSONArray("movies");
                for (int i = 0; i < arr.length(); i++) {
                    movies.add(Movie.fromJson(arr.getJSONObject(i)));
                }
                yield new MovieListResponse(message, movies);
            }
            case REVIEW_LIST -> {
                var reviews = new ArrayList<Review>();
                JSONArray arr = obj.getJSONArray("reviews");
                for (int i = 0; i < arr.length(); i++) {
                    reviews.add(Review.fromJson(arr.getJSONObject(i)));
                }
                yield new ReviewListResponse(message, reviews);
            }
            case USER_INFO -> new UserInfoResponse(message, obj.getString("username"));
            case USER_LIST -> {
                var users = new ArrayList<User>();
                JSONArray arr = obj.getJSONArray("users");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject u = arr.getJSONObject(i);
                    users.add(new User(u.getInt("id"), u.getString("username")));
                }
                yield new UserListResponse(message, users);
            }
        };
    }
}
