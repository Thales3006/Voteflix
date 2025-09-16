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

}