package com.thales.common.model;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (id != null) json.put("id", id);
        if (title != null) json.put("title", title);
        if (director != null) json.put("director", director);
        if (year != null) json.put("year", year);
        if (rating != null) json.put("rating", rating);
        if (ratingCount != null) json.put("rating_count", ratingCount);
        if (synopsis != null) json.put("synopsis", synopsis);
        if (genre != null) {
            JSONArray genreArray = new JSONArray();
            for (String g : genre) {
                if (g != null) genreArray.put(g);
            }
            if (genreArray.length() > 0) json.put("genre", genreArray);
        }
        return json;
    }

    public static Movie fromJson(JSONObject json) {
        Integer id = json.has("id") ? json.getInt("id") : null;
        String title = json.has("title") ? json.getString("title") : null;
        String director = json.has("director") ? json.getString("director") : null;
        Integer year = json.has("year") ? json.getInt("year") : null;
        Float rating = json.has("rating") ? (float) json.getDouble("rating") : null;
        Integer ratingCount = json.has("rating_count") ? json.getInt("rating_count") : null;
        String synopsis = json.has("synopsis") ? json.getString("synopsis") : null;
        String[] genre = null;
        if (json.has("genre")) {
            JSONArray arr = json.getJSONArray("genre");
            genre = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) genre[i] = arr.getString(i);
        }
        return new Movie(id, title, director, genre, year, rating, ratingCount, synopsis);
    }
}
