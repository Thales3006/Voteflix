package com.thales.client.util;

import java.util.Set;

public class Validate {

    private static final java.util.regex.Pattern ALPHANUMERIC = java.util.regex.Pattern.compile("^[A-Za-z0-9_]+$");

    private static final Set<String> VALID_GENRES = Set.of(
        "Action", "Adventure", "Comedy", "Drama", "Fantasy",
        "Sci-Fi", "Horror", "Romance", "Documentary", "Musical",
        "Animation", "Crime", "Thriller", "Historical"
    );

    public static void notEmpty(String value, String field) {
        if (value == null || value.isBlank())
            throw new ValidationException(field + " is required.");
    }

    public static void length(String value, String field, int min, int max) {
        notEmpty(value, field);
        int len = value.length();
        if (len < min || len > max)
            throw new ValidationException(field + " must be between " + min + " and " + max + " characters.");
    }

    public static void maxLength(String value, String field, int max) {
        if (value != null && value.length() > max)
            throw new ValidationException(field + " must be at most " + max + " characters.");
    }

    public static void alphanumeric(String value, String field) {
        if (value == null || !ALPHANUMERIC.matcher(value).matches())
            throw new ValidationException(field + " may only contain letters, digits, and underscores.");
    }

    public static void intRange(String text, String field, int min, int max) {
        notEmpty(text, field);
        int val;
        try {
            val = Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a number.");
        }
        if (val < min || val > max)
            throw new ValidationException(field + " must be between " + min + " and " + max + ".");
    }

    public static void genre(String[] genres) {
        if (genres == null || genres.length == 0)
            throw new ValidationException("At least one genre is required.");
        for (String g : genres) {
            if (!VALID_GENRES.contains(g))
                throw new ValidationException("\"" + g + "\" is not a valid genre. Valid genres: " + String.join(", ", VALID_GENRES.stream().sorted().toList()));
        }
    }
}
