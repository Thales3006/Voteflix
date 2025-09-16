package com.thales.common.model;

import lombok.Data;

@Data
public class Movie {
    private int ID;
    private String title;
    private String director;
    private String[] genre;
    private int year;
    private float rating;
    private int ratingAmount;
    private String synopsis;
}
