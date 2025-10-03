package com.thales.common.model;

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

}
