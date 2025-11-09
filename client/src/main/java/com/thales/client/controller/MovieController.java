package com.thales.client.controller;

import java.util.ArrayList;

import com.thales.common.model.Movie;
import com.thales.common.model.Review;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class MovieController extends SceneController {

    @FXML private TextField titleField;
    @FXML private TextField directorField;
    @FXML private TextField yearField;
    @FXML private TextField genresField;
    @FXML private TextField synopsisField;
    @FXML private Label ratingLabel;
    @FXML private Label reviewAmountLabel;
    @FXML private TextField reviewTitleField;
    @FXML private TextField reviewDescriptionField;
    @FXML private TextField reviewScoreField;
    @FXML private Label idLabel;
    @FXML private Button createMovieButton;
    @FXML private Button updateMovieButton;
    @FXML private Button deleteMovieButton;
    @FXML private Button createReviewButton;
    @FXML private Button refreshMovieButton;
    @FXML private VBox movieInfoPane;
    @FXML private HBox movieButtonHbox;
    @FXML private TilePane movieTilePane;
    @FXML private VBox reviewVbox;
    @FXML private Button loadReviewButton;

    private final SimpleObjectProperty<Movie> currentMovie = new SimpleObjectProperty<>();

    @FXML private void initialize(){
        currentMovie.addListener((_, _, movie) -> {
            if (movie != null) {
                titleField.setText(movie.getTitle());
                directorField.setText(movie.getDirector());
                yearField.setText(String.valueOf(movie.getYear()));
                genresField.setText(String.join(", ", movie.getGenre()));
                synopsisField.setText(movie.getSynopsis());
                ratingLabel.setText(String.valueOf(movie.getRating()));
                reviewAmountLabel.setText(String.valueOf(movie.getRatingAmount()));
                idLabel.setText(String.valueOf(movie.getID()));
            }
        });

        if(clientService.isAdmin()){
            titleField.setEditable(true);
            directorField.setEditable(true);
            yearField.setEditable(true);
            genresField.setEditable(true);
            synopsisField.setEditable(true);
            movieButtonHbox.setDisable(false);
        }
    }

    private void loadMovies() throws Exception {
        var request = clientService.requestMovieList();
        ArrayList<Movie> movies = request.getSecond();
        movieTilePane.getChildren().clear();

        for (Movie movie : movies) {
            VBox movieBox = new VBox();
            movieBox.prefWidthProperty().bind(movieTilePane.widthProperty().divide(2.2));
            movieBox.getChildren().addAll(
                new Label("Title: " + movie.getTitle()),
                new Label("Director: " + movie.getDirector()),
                new Label("Year: " + movie.getYear())
            );
            movieBox.getStyleClass().add("movie-view");
            movieTilePane.getChildren().add(movieBox);

            movieBox.setOnMouseClicked(_ -> currentMovie.set(movie));
        }
        feedback(request.getFirst());
    }


    @FXML private void HandleCreateMovieButton(ActionEvent event){
        handle(event, () -> { 
            Movie movie = new Movie(
                null,
                titleField.getText().trim(), 
                directorField.getText().trim(), 
                java.util.Arrays.stream(genresField.getText().trim().split(",\\s*"))
                    .map(String::trim)
                    .toArray(String[]::new),
                Integer.parseInt(yearField.getText()), 
                null,
                null,
                synopsisField.getText().trim()
            );
            String request = clientService.requestCreateMovie(movie); 
            feedback(request);
        });
    }

    @FXML private void HandleUpdateMovieButton(ActionEvent event){
        handle(event, () -> { 
            Movie movie = new Movie(
                currentMovie.get().getID(),
                titleField.getText(), 
                directorField.getText(), 
                java.util.Arrays.stream(genresField.getText().trim().split(",\\s*"))
                    .map(String::trim)
                    .toArray(String[]::new),
                Integer.parseInt(yearField.getText()), 
                null,
                null,
                synopsisField.getText()
            );
            String request = clientService.requestUpdateMovie(movie);
            feedback(request);
        });
    }

    @FXML private void HandleDeleteMovieButton(ActionEvent event){
        handle(event, () -> { 
            String request = clientService.requestDeleteMovie(currentMovie.get().getID()); 
            feedback(request);
        });
    }

    @FXML private void HandleCreateReviewButton(ActionEvent event){
        handle(event, () -> { 
            Review review = new Review(
                null,
                currentMovie.get().getID(),
                null,
                Float.valueOf(reviewScoreField.getText()),
                reviewTitleField.getText(),
                reviewDescriptionField.getText(),
                null
            );
            var message = clientService.requestCreateReview(review);

            feedback(message);
        });
    }

    @FXML private void HandleLoadReviewButton(ActionEvent event){
        handle(event, () -> { 
            var request = clientService.requestMovieReviewList(currentMovie.get().getID());
            String message = request.getFirst();
            ArrayList<Review> reviews = request.getSecond();

            reviewVbox.getChildren().clear();

            if (reviews == null || reviews.isEmpty()) {
                reviewVbox.getChildren().add(new Label("No reviews yet."));
            } else {
                for (Review r : reviews) {
                    reviewVbox.getChildren().add(ViewReview(r));
                }
            }
            feedback(message);
        });
    }

    @FXML private void HandleRefreshMovieButton(ActionEvent event){
        handle(event, () -> loadMovies());
    }

    private VBox ViewReview(Review r){
        VBox reviewBox = new VBox();
        Label title = new Label("Title: " + (r.getTitle() == null ? "" : r.getTitle()));
        Label description = new Label("Description: " + (r.getDescription() == null ? "" : r.getDescription()));
        Label name = new Label("Username: " + (r.getUsername() == null ? "" : String.valueOf(r.getUsername())));
        Label score = new Label("Rating: " + (r.getRating() == null ? "" : String.valueOf(r.getRating())));
        Label date = new Label("Date: " + (r.getDate() == null ? "" : String.valueOf(r.getDate())));
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(evt -> handle(evt, () -> {
            String message = clientService.requestDeleteReview(r.getID());
            feedback(message);
        }));
        reviewBox.getChildren().addAll(title, description, name, score, date, deleteButton);
        return reviewBox;
    }
}
