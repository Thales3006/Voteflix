package com.thales.common.model;

import java.time.LocalDate;

import com.google.gson.JsonObject;

import lombok.Data;

@Data
public class Review {

    private Integer ID;
    private Integer movieID;
    private Integer userID;
    private Integer rating;
    private String title;
    private String description;
    private LocalDate date;

    public Review(Integer ID, Integer movieID, Integer userID, Integer rating, String title, String description, LocalDate date) {
        this.ID = ID;
        this.movieID = movieID;
        this.userID = userID;
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (ID != null) json.addProperty("ID", ID.toString());
        if (movieID != null) json.addProperty("movieID", movieID.toString());
        if (userID != null) json.addProperty("userID", userID.toString());
        if (rating != null) json.addProperty("rating", rating.toString());
        if (title != null) json.addProperty("title", title);
        if (description != null) json.addProperty("description", description);
        if (date != null) json.addProperty("date", date.toString());
        return json;
    }
    public static Review fromJson(JsonObject json) {
        Integer ID = null;
        Integer movieID = null;
        Integer userID = null;
        Integer rating = null;
        String title = null;
        String description = null;
        LocalDate date = null;

        if (json.has("ID")) {
            ID = Integer.valueOf(json.get("ID").getAsString());
        }
        if (json.has("movieID")) {
            movieID = Integer.valueOf(json.get("movieID").getAsString());
        }
        if (json.has("userID")) {
            userID = Integer.valueOf(json.get("userID").getAsString());
        }
        if (json.has("rating")) {
            rating = Integer.valueOf(json.get("rating").getAsString()); 
        }
        if (json.has("title")) {
            title = json.get("title").getAsString();
        }
        if (json.has("description")) {
            description = json.get("description").getAsString();
        }
        if (json.has("date")) {
            date = LocalDate.parse(json.get("date").getAsString());
        }

        return new Review(ID, movieID, userID, rating, title, description, date);
    }
}