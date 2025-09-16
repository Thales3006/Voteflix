package com.thales.common.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Review {

    private int ID;
    private int movieID;
    private String username;
    private float rating;
    private String title;
    private String description;
    private LocalDate date;

    public Review(int ID, int movieID, String username, float rating, String title, String description, LocalDate date) {
        this.ID = ID;
        this.movieID = movieID;
        this.username = username;
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.date = date;
    }
}