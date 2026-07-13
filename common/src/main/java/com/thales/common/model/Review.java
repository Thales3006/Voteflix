package com.thales.common.model;

import java.time.LocalDate;

import org.json.JSONObject;

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

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (id != null) json.put("id", id);
        if (movieId != null) json.put("movie_id", movieId);
        if (username != null) json.put("username", username);
        if (rating != null) json.put("rating", rating);
        if (title != null) json.put("title", title);
        if (description != null) json.put("description", description);
        if (edited != null) json.put("edited", edited);
        if (date != null) json.put("date", date.toString());
        return json;
    }

    public static Review fromJson(JSONObject json) {
        Integer id = json.has("id") ? json.getInt("id") : null;
        Integer movieId = json.has("movie_id") ? json.getInt("movie_id") : null;
        Integer userId = null;
        String username = json.has("username") ? json.getString("username") : null;
        Integer rating = json.has("rating") ? json.getInt("rating") : null;
        String title = json.has("title") ? json.getString("title") : null;
        String description = json.has("description") ? json.getString("description") : null;
        Boolean edited = json.has("edited") ? json.getBoolean("edited") : null;
        LocalDate date = json.has("date") ? LocalDate.parse(json.getString("date")) : null;

        Review review = new Review(id, movieId, userId, rating, title, description, edited, date);
        review.setUsername(username);
        return review;
    }
}
