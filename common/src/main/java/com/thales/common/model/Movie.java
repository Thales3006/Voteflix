package com.thales.common.model;

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

    public Movie(int ID, String title, String director, String[] genre, int year, float rating, int ratingAmount, String synopsis) {
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
        Integer id = Integer.parseInt(json.get("id").getAsString());
        String title = json.get("titulo").getAsString();
        String director = json.get("diretor").getAsString();
        Integer year = Integer.parseInt(json.get("ano").getAsString());
        Float rating = Float.parseFloat(json.get("nota").getAsString());
        Integer ratingAmount = Integer.parseInt(json.get("qtd_avaliacoes").getAsString());
        String synopsis = json.get("sinopse").getAsString();
        
        JsonArray genreArray = json.getAsJsonArray("genero");
        String[] genre = new String[genreArray.size()];
        for (int i = 0; i < genreArray.size(); i++) {
            genre[i] = genreArray.get(i).getAsString();
        }
        
        return new Movie(id, title, director, genre, year, rating, ratingAmount, synopsis);
    }

}
