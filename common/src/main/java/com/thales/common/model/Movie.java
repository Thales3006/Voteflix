package com.thales.common.model;

import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Data;

@Data
public class Movie {
    private Integer id;
    private String title;
    private String director;
    private String[] genre;
    private Integer year;
    private Float rating;
    private Integer ratingCount;
    private String synopsis;

    public Movie(Integer id, String title, String director, String[] genre, Integer year, Float rating, Integer ratingCount, String synopsis) {
        this.id = id;
        this.title = title;
        this.director = director;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.synopsis = synopsis;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (id != null) json.addProperty("id", id);
        if (title != null) json.addProperty("title", title);
        if (director != null) json.addProperty("director", director);
        if (year != null) json.addProperty("year", year);
        if (rating != null) json.addProperty("rating", rating);
        if (ratingCount != null) json.addProperty("rating_count", ratingCount);
        if (synopsis != null) json.addProperty("synopsis", synopsis);
        if (genre != null) {
            JsonArray genreArray = new JsonArray();
            for (String g : genre) {
                if (g != null) genreArray.add(g);
            }
            if (genreArray.size() > 0) json.add("genre", genreArray);
        }
        return json;
    }

    public static Movie fromJson(JsonObject json) {
        Integer id = Optional.ofNullable(json.get("id"))
            .map(e -> e.getAsInt()).orElse(null);
        String title = Optional.ofNullable(json.get("title"))
            .map(e -> e.getAsString()).orElse(null);
        String director = Optional.ofNullable(json.get("director"))
            .map(e -> e.getAsString()).orElse(null);
        Integer year = Optional.ofNullable(json.get("year"))
            .map(e -> e.getAsInt()).orElse(null);
        Float rating = Optional.ofNullable(json.get("rating"))
            .map(e -> e.getAsFloat()).orElse(null);
        Integer ratingCount = Optional.ofNullable(json.get("rating_count"))
            .map(e -> e.getAsInt()).orElse(null);
        String synopsis = Optional.ofNullable(json.get("synopsis"))
            .map(e -> e.getAsString()).orElse(null);
        String[] genre = Optional.ofNullable(json.getAsJsonArray("genre"))
            .map(array -> {
                String[] g = new String[array.size()];
                for (int i = 0; i < array.size(); i++) g[i] = array.get(i).getAsString();
                return g;
            }).orElse(null);

        return new Movie(id, title, director, genre, year, rating, ratingCount, synopsis);
    }
}
