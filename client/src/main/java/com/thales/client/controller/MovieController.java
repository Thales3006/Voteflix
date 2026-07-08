package com.thales.client.controller;

import java.util.List;

import com.thales.common.model.Movie;
import com.thales.common.model.Review;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
    @FXML private VBox movieOverlay;
    @FXML private HBox movieButtonHbox;
    @FXML private TilePane movieTilePane;
    @FXML private VBox reviewVbox;
    @FXML private Button loadReviewButton;

    private final SimpleObjectProperty<Movie> currentMovie = new SimpleObjectProperty<>();

    @FXML private void initialize() {
        movieOverlay.setOnMouseClicked((MouseEvent e) -> {
            if (e.getTarget() == movieOverlay) closeOverlay();
        });

        reviewScoreField.setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("[1-5]?") ? change : null));

        currentMovie.addListener((_, _, movie) -> {
            if (movie != null) {
                titleField.setText(movie.getTitle());
                directorField.setText(movie.getDirector());
                yearField.setText(String.valueOf(movie.getYear()));
                genresField.setText(String.join(", ", movie.getGenre()));
                synopsisField.setText(movie.getSynopsis());
                ratingLabel.setText(String.valueOf(movie.getRating()));
                reviewAmountLabel.setText(String.valueOf(movie.getRatingCount()));
                idLabel.setText(String.valueOf(movie.getId()));

                movieOverlay.setVisible(true);
                movieOverlay.setManaged(true);
                handle(null, () -> { loadReviews(); });
            }
        });

        if (clientService.isAdmin()) {
            titleField.setEditable(true);
            directorField.setEditable(true);
            yearField.setEditable(true);
            genresField.setEditable(true);
            synopsisField.setEditable(true);
            movieButtonHbox.setVisible(true);
            movieButtonHbox.setManaged(true);
        } else {
            for (TextField f : java.util.List.of(titleField, directorField, yearField, genresField, synopsisField)) {
                f.getStyleClass().add("movie-display-field");
            }
            titleField.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-background-color: transparent; -fx-border-color: transparent; -fx-background-insets: 0; -fx-padding: 2 0; -fx-effect: none; -fx-cursor: default; -fx-text-fill: #f0e6d3;");
        }

        handle(null, () -> { loadMovies(); });
    }

    @FXML private void HandleCloseOverlay() {
        closeOverlay();
    }

    private void closeOverlay() {
        currentMovie.set(null);
        movieOverlay.setVisible(false);
        movieOverlay.setManaged(false);
    }

    private void loadMovies() throws Exception {
        var response = clientService.requestMovieList();
        List<Movie> movies = response.movies();

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
    }

    @FXML private void HandleCreateMovieButton(ActionEvent event) {
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
            clientService.requestCreateMovie(movie);
            loadMovies();
        });
    }

    @FXML private void HandleUpdateMovieButton(ActionEvent event) {
        handle(event, () -> {
            if (currentMovie.get() == null) {
                throw new Exception("You have to select a movie");
            }
            Movie movie = new Movie(
                currentMovie.get().getId(),
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
            clientService.requestUpdateMovie(movie);
            loadMovies();
        });
    }

    @FXML private void HandleDeleteMovieButton(ActionEvent event) {
        handle(event, () -> {
            if (currentMovie.get() == null) {
                throw new Exception("You have to select a movie");
            }
            clientService.requestDeleteMovie(currentMovie.get().getId());
            loadMovies();
        });
    }

    private void loadReviews() throws Exception {
        if (currentMovie.get() == null) {
            throw new Exception("No movie selected");
        }
        var response = clientService.requestMovieReviewList(currentMovie.get().getId());
        List<Review> reviews = response.reviews();

        reviewVbox.getChildren().clear();

        if (reviews == null || reviews.isEmpty()) {
            reviewVbox.getChildren().add(new Label("No reviews yet."));
        } else {
            for (Review r : reviews) {
                reviewVbox.getChildren().add(ViewReview(r));
            }
        }
    }

    @FXML private void HandleCreateReviewButton(ActionEvent event) {
        handle(event, () -> {
            if (currentMovie.get() == null) {
                throw new Exception("You have to select a movie");
            }
            Review review = new Review(
                null,
                currentMovie.get().getId(),
                null,
                Integer.valueOf(reviewScoreField.getText()),
                reviewTitleField.getText(),
                reviewDescriptionField.getText(),
                null,
                null
            );
            clientService.requestCreateReview(review);
            loadReviews();
        });
    }

    @FXML private void HandleLoadReviewButton(ActionEvent event) {
        handle(event, () -> { loadReviews(); });
    }

    @FXML private void HandleRefreshMovieButton(ActionEvent event) {
        handle(event, () -> loadMovies());
    }

    private VBox ViewReview(Review r) {
        Label titleLabel = new Label(r.getTitle() == null ? "" : r.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label usernameLabel = new Label(r.getUsername() == null ? "" : r.getUsername());
        usernameLabel.getStyleClass().add("detail-label");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox row1 = new HBox(4, titleLabel, spacer1, usernameLabel);
        row1.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(r.getDescription() == null ? "" : r.getDescription());
        descLabel.getStyleClass().add("detail-label");
        descLabel.setWrapText(true);

        Label ratingLabel = new Label(r.getRating() == null ? "" : "★ " + r.getRating());
        Label dateLabel = new Label(r.getDate() == null ? "" : String.valueOf(r.getDate()));
        dateLabel.getStyleClass().add("detail-label");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox row3 = new HBox(4, ratingLabel, spacer2, dateLabel);
        row3.setAlignment(Pos.CENTER_LEFT);

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(evt -> handle(evt, () -> {
            clientService.requestDeleteReview(r.getId());
            loadReviews();
        }));

        VBox reviewBox = new VBox(4, row1, descLabel, row3);
        reviewBox.getStyleClass().add("review-view");

        if (Boolean.TRUE.equals(r.getEdited())) {
            Label editedLabel = new Label("edited");
            editedLabel.getStyleClass().add("detail-label");
            HBox row4 = new HBox(new Region(), editedLabel);
            HBox.setHgrow(row4.getChildren().get(0), Priority.ALWAYS);
            reviewBox.getChildren().add(row4);
        }

        if (clientService.isAdmin() || r.getUsername().equals(clientService.getUsername())) {
            reviewBox.getChildren().add(deleteButton);
        }
        return reviewBox;
    }
}
