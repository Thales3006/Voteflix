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
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
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
    @FXML private TextArea synopsisField;
    @FXML private Label ratingLabel;
    @FXML private Label reviewAmountLabel;
    @FXML private TextField reviewTitleField;
    @FXML private TextArea reviewDescriptionField;
    @FXML private TextField reviewScoreField;
    @FXML private Label idLabel;
    @FXML private Button createMovieButton;
    @FXML private Button updateMovieButton;
    @FXML private Button deleteMovieButton;
    @FXML private Button createReviewButton;
    @FXML private Button newMovieButton;
    @FXML private Button refreshMovieButton;
    @FXML private VBox movieInfoPane;
    @FXML private VBox movieOverlay;
    @FXML private HBox movieButtonHbox;
    @FXML private TilePane movieTilePane;
    @FXML private VBox reviewVbox;
    @FXML private VBox reviewPanel;
    @FXML private Separator reviewPanelSeparator;
    @FXML private Button loadReviewButton;
    @FXML private GridPane movieFormPane;
    @FXML private VBox movieDisplayPane;
    @FXML private Label displayTitleLabel;
    @FXML private Label displayDirectorLabel;
    @FXML private Label displayYearLabel;
    @FXML private Label displaySynopsisLabel;

    private final SimpleObjectProperty<Movie> currentMovie = new SimpleObjectProperty<>();

    @FXML private void initialize() {
        movieOverlay.setOnMouseClicked((MouseEvent e) -> {
            if (e.getTarget() == movieOverlay) closeOverlay();
        });

        reviewScoreField.setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("[1-5]?") ? change : null));

        currentMovie.addListener((_, _, movie) -> {
            if (movie != null) {
                boolean hasRating = movie.getRatingCount() != null && movie.getRatingCount() > 0;
                ratingLabel.setText(hasRating ? String.valueOf(movie.getRating()) : "—");
                reviewAmountLabel.setText(String.valueOf(movie.getRatingCount() != null ? movie.getRatingCount() : 0));
                idLabel.setText(String.valueOf(movie.getId()));

                if (clientService.isAdmin()) {
                    titleField.setText(movie.getTitle());
                    directorField.setText(movie.getDirector());
                    yearField.setText(String.valueOf(movie.getYear()));
                    genresField.setText(String.join(", ", movie.getGenre()));
                    synopsisField.setText(movie.getSynopsis());
                } else {
                    displayTitleLabel.setText(movie.getTitle() != null ? movie.getTitle() : "");
                    displayDirectorLabel.setText(movie.getDirector() != null ? movie.getDirector() : "");
                    displayYearLabel.setText(movie.getYear() + (movie.getGenre() != null ? "  ·  " + String.join(", ", movie.getGenre()) : ""));
                    displaySynopsisLabel.setText(movie.getSynopsis() != null ? movie.getSynopsis() : "");
                }

                updateMovieButton.setVisible(true);
                updateMovieButton.setManaged(true);
                deleteMovieButton.setVisible(true);
                deleteMovieButton.setManaged(true);
                reviewPanel.setVisible(true);
                reviewPanel.setManaged(true);
                reviewPanelSeparator.setVisible(true);
                reviewPanelSeparator.setManaged(true);
                createMovieButton.setText("Save");
                movieOverlay.setVisible(true);
                movieOverlay.setManaged(true);
                movieOverlay.getStyleClass().add("open");
                handle(null, () -> { loadReviews(); });
            }
        });

        if (clientService.isAdmin()) {
            movieButtonHbox.setVisible(true);
            movieButtonHbox.setManaged(true);
            newMovieButton.setVisible(true);
            newMovieButton.setManaged(true);
        } else {
            movieFormPane.setVisible(false);
            movieFormPane.setManaged(false);
            movieDisplayPane.setVisible(true);
            movieDisplayPane.setManaged(true);
        }

        handle(null, () -> { loadMovies(); });
    }

    @FXML private void HandleCloseOverlay() {
        closeOverlay();
    }

    private void closeOverlay() {
        currentMovie.set(null);
        movieOverlay.getStyleClass().remove("open");
        movieOverlay.setVisible(false);
        movieOverlay.setManaged(false);
    }

    private void loadMovies() throws Exception {
        var response = clientService.requestMovieList();
        List<Movie> movies = response.movies();

        movieTilePane.getChildren().clear();
        for (Movie movie : movies) {
            VBox movieBox = new VBox(6);
            movieBox.prefWidthProperty().bind(movieTilePane.widthProperty().divide(2.2));

            Label titleLabel = new Label(movie.getTitle() != null ? movie.getTitle() : "");
            titleLabel.getStyleClass().add("movie-card-title");

            Label directorLabel = new Label(movie.getDirector() != null ? movie.getDirector() : "");
            directorLabel.getStyleClass().add("movie-card-sub");

            Label yearLabel = new Label(String.valueOf(movie.getYear()));
            yearLabel.getStyleClass().add("movie-card-meta");
            boolean hasRating = movie.getRatingCount() != null && movie.getRatingCount() > 0;
            Label ratingBadge = new Label(hasRating ? "★ " + movie.getRating() : "");
            ratingBadge.getStyleClass().add("movie-card-rating");
            Region metaSpacer = new Region();
            HBox.setHgrow(metaSpacer, Priority.ALWAYS);
            HBox metaRow = new HBox(4, yearLabel, metaSpacer, ratingBadge);
            metaRow.setAlignment(Pos.CENTER_LEFT);

            movieBox.getChildren().addAll(titleLabel, directorLabel, metaRow);
            movieBox.getStyleClass().add("movie-view");
            movieTilePane.getChildren().add(movieBox);

            movieBox.setOnMouseClicked(_ -> currentMovie.set(movie));
        }
    }

    @FXML private void HandleNewMovieButton(ActionEvent event) {
        currentMovie.set(null);
        titleField.clear();
        directorField.clear();
        yearField.clear();
        genresField.clear();
        synopsisField.clear();
        ratingLabel.setText("—");
        reviewAmountLabel.setText("0");
        idLabel.setText("");
        reviewVbox.getChildren().clear();
        updateMovieButton.setVisible(false);
        updateMovieButton.setManaged(false);
        deleteMovieButton.setVisible(false);
        deleteMovieButton.setManaged(false);
        reviewPanel.setVisible(false);
        reviewPanel.setManaged(false);
        reviewPanelSeparator.setVisible(false);
        reviewPanelSeparator.setManaged(false);
        createMovieButton.setText("Create");
        movieOverlay.setVisible(true);
        movieOverlay.setManaged(true);
        movieOverlay.getStyleClass().add("open");
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
            closeOverlay();
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
