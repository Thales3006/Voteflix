package com.thales.common.model;

import java.time.LocalDate;

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
}