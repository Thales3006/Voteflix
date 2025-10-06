package com.thales.common.model;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Data;

@Data
public class Movie {
    private Integer ID;
    private String title;
    private String director;
    private String[] genre;
    private Integer year;
    private Float rating;
    private Integer ratingAmount;
    private String synopsis;

    public Movie(Integer ID, String title, String director, String[] genre, Integer year, Float rating, Integer ratingAmount, String synopsis) {
        this.ID = ID;
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.ratingAmount = ratingAmount;
        this.synopsis = synopsis;
    }

    public JsonObject toJson() {
        JsonObject movieJson = new JsonObject();
        if (this.ID != null) movieJson.addProperty("id", this.ID.toString());
        if (this.title != null) movieJson.addProperty("titulo", this.title);
        if (this.director != null) movieJson.addProperty("diretor", this.director);
        if (this.year != null) movieJson.addProperty("ano", this.year.toString());
        if (this.rating != null) movieJson.addProperty("nota", this.rating.toString());
        if (this.ratingAmount != null) movieJson.addProperty("qtd_avaliacoes", this.ratingAmount.toString());
        if (this.synopsis != null) movieJson.addProperty("sinopse", this.synopsis);
        if (this.genre != null) {
            JsonArray genreArray = new JsonArray();
            for (String g : this.genre) {
            if (g != null) {
                genreArray.add(g);
            }
            }
            if (genreArray.size() > 0) {
            movieJson.add("genero", genreArray);
            }
        }
        return movieJson;
    }

    public static Movie fromJson(JsonObject json) {
        Integer id = Optional.ofNullable(json.get("id"))
                    .map(element -> element.getAsString())
                    .map(Integer::parseInt)
                    .orElse(null);
        String title = Optional.ofNullable(json.get("titulo"))
                    .map(element -> element.getAsString())
                    .orElse(null);
        String director = Optional.ofNullable(json.get("diretor"))
                    .map(element -> element.getAsString())
                    .orElse(null);
        Integer year = Optional.ofNullable(json.get("ano"))
                    .map(element -> element.getAsString())
                    .map(Integer::parseInt)
                    .orElse(null);
        Float rating = Optional.ofNullable(json.get("nota"))
                    .map(element -> element.getAsString())
                    .map(Float::parseFloat)
                    .orElse(null);
        Integer ratingAmount = Optional.ofNullable(json.get("qtd_avaliacoes"))
                    .map(element -> element.getAsString())
                    .map(Integer::parseInt)
                    .orElse(null);
        String synopsis = Optional.ofNullable(json.get("sinopse"))
                    .map(element -> element.getAsString())
                    .orElse(null);
        
        String[] genre = Optional.ofNullable(json.getAsJsonArray("genero"))
                    .map(array -> {
                    String[] g = new String[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        g[i] = array.get(i).getAsString();
                    }
                    return g;
                    })
                    .orElse(null);
        
        return new Movie(id, title, director, genre, year, rating, ratingAmount, synopsis);
    }

}
