package com.thales.common.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Review {

    private Integer ID;
    private Integer movieID;
    private String username;
    private Float rating;
    private String title;
    private String description;
    private LocalDate date;

    public Review(Integer ID, Integer movieID, String username, Float rating, String title, String description, LocalDate date) {
        this.ID = ID;
        this.movieID = movieID;
        this.username = username;
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.date = date;
    }
}