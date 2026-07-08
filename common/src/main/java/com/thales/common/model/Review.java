package com.thales.common.model;

import java.time.LocalDate;

import com.google.gson.JsonObject;

import lombok.Data;

@Data
public class Review {
    private Integer id;
    private Integer movieId;
    private Integer userId;
    private String username;
    private Integer rating;
    private String title;
    private String description;
    private Boolean edited;
    private LocalDate date;

    public Review(Integer id, Integer movieId, Integer userId, Integer rating, String title, String description, Boolean edited, LocalDate date) {
        this.id = id;
        this.movieId = movieId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.edited = edited;
        this.date = date;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null) json.addProperty("id", id);
        if (movieId != null) json.addProperty("movie_id", movieId);
        if (username != null) json.addProperty("username", username);
        if (rating != null) json.addProperty("rating", rating);
        if (title != null) json.addProperty("title", title);
        if (description != null) json.addProperty("description", description);
        if (edited != null) json.addProperty("edited", edited);
        if (date != null) json.addProperty("date", date.toString());
        return json;
    }

    public static Review fromJson(JsonObject json) {
        Integer id = null, movieId = null, userId = null, rating = null;
        String username = null, title = null, description = null;
        Boolean edited = null;
        LocalDate date = null;

        if (json.has("id")) id = json.get("id").getAsInt();
        if (json.has("movie_id")) movieId = json.get("movie_id").getAsInt();
        if (json.has("username")) username = json.get("username").getAsString();
        if (json.has("rating")) rating = json.get("rating").getAsInt();
        if (json.has("title")) title = json.get("title").getAsString();
        if (json.has("description")) description = json.get("description").getAsString();
        if (json.has("edited")) edited = json.get("edited").getAsBoolean();
        if (json.has("date")) date = LocalDate.parse(json.get("date").getAsString());

        Review review = new Review(id, movieId, userId, rating, title, description, edited, date);
        review.setUsername(username);
        return review;
    }
}
