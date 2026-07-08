package com.thales.client.controller;

import java.util.List;

import com.thales.common.model.Review;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ReviewController extends SceneController {
    @FXML Button refreshButton;
    @FXML Button updateButton;
    @FXML Button deleteButton;
    @FXML TextField titleField;
    @FXML TextField descriptionField;
    @FXML TextField ratingField;
    @FXML Label movieLabel;
    @FXML Label usernameLabel;
    @FXML Label dateLabel;
    @FXML VBox reviewList;

    private final SimpleObjectProperty<Review> currentReview = new SimpleObjectProperty<>();

    @FXML private void initialize() {
        currentReview.addListener((_, _, review) -> {
            if (review != null) {
                titleField.setText(review.getTitle());
                descriptionField.setText(review.getDescription());
                ratingField.setText(String.valueOf(review.getRating()));
                usernameLabel.setText(review.getUsername());
                movieLabel.setText(String.valueOf(review.getMovieId()));
                dateLabel.setText(String.valueOf(review.getDate()));
            }
        });
        handle(null, () -> { loadReviews(); });
    }

    private void loadReviews() throws Exception {
        var response = clientService.requestOwnReviewList();
        List<Review> reviews = response.reviews();

        reviewList.getChildren().clear();

        if (reviews == null || reviews.isEmpty()) {
            reviewList.getChildren().add(new Label("No reviews yet."));
        } else {
            for (Review r : reviews) {
                VBox reviewBox = new VBox();
                Label title = new Label("Title: " + (r.getTitle() == null ? "" : r.getTitle()));
                Label description = new Label("Description: " + (r.getDescription() == null ? "" : r.getDescription()));
                Label name = new Label("Username: " + (r.getUsername() == null ? "" : String.valueOf(r.getUsername())));
                Label score = new Label("Rating: " + (r.getRating() == null ? "" : String.valueOf(r.getRating())));
                Label edited = new Label("Edited: " + (r.getEdited() == null ? "" : String.valueOf(r.getEdited())));
                Label date = new Label("Date: " + (r.getDate() == null ? "" : String.valueOf(r.getDate())));
                reviewBox.getChildren().addAll(title, description, name, score, date, edited);
                reviewBox.getStyleClass().add("review-view");
                reviewBox.setOnMouseClicked(_ -> currentReview.set(r));
                reviewList.getChildren().add(reviewBox);
            }
        }
    }

    @FXML private void HandleRefreshButton(ActionEvent event) {
        handle(event, () -> { loadReviews(); });
    }

    @FXML private void HandleUpdateButton(ActionEvent event) {
        handle(event, () -> {
            if (currentReview.get() == null) {
                throw new Exception("You have to select a review");
            }
            clientService.requestUpdateReview(extractReview());
            loadReviews();
        });
    }

    @FXML private void HandleDeleteButton(ActionEvent event) {
        handle(event, () -> {
            if (currentReview.get() == null) {
                throw new Exception("You have to select a review");
            }
            clientService.requestDeleteReview(currentReview.get().getId());
            loadReviews();
        });
    }

    @FXML private Review extractReview() {
        return new Review(
            currentReview.get().getId(),
            null,
            null,
            Integer.valueOf(ratingField.getText()),
            titleField.getText(),
            descriptionField.getText(),
            null,
            null
        );
    }
}
