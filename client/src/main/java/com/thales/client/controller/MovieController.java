package com.thales.client.controller;

import java.util.ArrayList;

import com.thales.common.model.Movie;

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

    private final SimpleObjectProperty<Movie> currentMovie = new SimpleObjectProperty<>();

    @FXML private void initialize(){
        yearField.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() > 1) {
                yearField.setText(newValue.substring(0, 4));
            }
        });

        reviewScoreField.textProperty().addListener((_, _, newValue) -> {
            if (!newValue.matches("[1-5]")) {
                reviewScoreField.setText(newValue.replaceAll("[^1-5]", ""));
            }
            if (newValue.length() > 1) {
                reviewScoreField.setText(newValue.substring(0, 1));
            }
        });

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
        handle(() -> { 
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
        handle(() -> { 
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
        handle(() -> { 
            String request = clientService.requestDeleteMovie(currentMovie.get().getID()); 
            feedback(request);
            loadMovies();
        });
    }

    @FXML private void HandleCreateReviewButton(ActionEvent event){
        handle(() -> { throw new Exception("review creation not implemented yet"); });
    }

    @FXML private void HandleRefreshMovieButton(ActionEvent event){
        handle(() -> loadMovies());
    }
}
