package com.thales.common.model;

import java.time.LocalDate;

import com.google.gson.JsonObject;

import lombok.Data;

@Data
public class Review {

    private Integer ID;
    private Integer movieID;
    private Integer userID;
    private String username;
    private Float rating;
    private String title;
    private String description;
    private LocalDate date;

    public Review(Integer ID, Integer movieID, Integer userID, Float rating, String title, String description, LocalDate date) {
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
        if (ID != null) json.addProperty("id", ID.toString());
        if (movieID != null) json.addProperty("id_filme", movieID.toString());
        if (username != null) json.addProperty("nome_usuario", username);
        if (rating != null) json.addProperty("nota", rating.toString());
        if (title != null) json.addProperty("titulo", title);
        if (description != null) json.addProperty("descricao", description);
        json.addProperty("data", date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return json;
    }
    public static Review fromJson(JsonObject json) {
        Integer ID = null;
        Integer movieID = null;
        Integer userID = null;
        String username = null;
        Float rating = null;
        String title = null;
        String description = null;
        LocalDate date = null;

        if (json.has("id")) {
            ID = Integer.valueOf(json.get("id").getAsString());
        }
        if (json.has("id_filme")) {
            movieID = Integer.valueOf(json.get("id_filme").getAsString());
        }
        if (json.has("nome_usuario")) {
            username = json.get("nome_usuario").getAsString();
        }
        if (json.has("nota")) {
            rating = Float.valueOf(json.get("nota").getAsString()); 
        }
        if (json.has("titulo")) {
            title = json.get("titulo").getAsString();
        }
        if (json.has("descricao")) {
            description = json.get("descricao").getAsString();
        }
        if (json.has("data")) {
            date = LocalDate.parse(json.get("data").getAsString(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        Review review = new Review(ID, movieID, userID, rating, title, description, date);
        review.setUsername(username);
        return review;

    }
}