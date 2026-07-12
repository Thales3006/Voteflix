package com.thales.client.controller;

import java.util.List;

import com.thales.common.model.Review;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ReviewController extends SceneController {
    @FXML Button refreshButton;
    @FXML Button updateButton;
    @FXML Button deleteButton;
    @FXML TextField titleField;
    @FXML TextArea descriptionField;
    @FXML TextField ratingField;
    @FXML Label movieLabel;
    @FXML Label usernameLabel;
    @FXML Label dateLabel;
    @FXML VBox reviewList;
    @FXML VBox reviewOverlay;

    private final SimpleObjectProperty<Review> currentReview = new SimpleObjectProperty<>();

    @FXML private void initialize() {
        reviewOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == reviewOverlay) HandleCloseReviewOverlay();
        });

        currentReview.addListener((_, _, review) -> {
            if (review != null) {
                titleField.setText(review.getTitle());
                descriptionField.setText(review.getDescription());
                ratingField.setText(String.valueOf(review.getRating()));
                usernameLabel.setText(review.getUsername());
                movieLabel.setText(String.valueOf(review.getMovieId()));
                dateLabel.setText(String.valueOf(review.getDate()));
                showReviewOverlay();
            }
        });

        handle(null, () -> { loadReviews(); });
    }

    private void showReviewOverlay() {
        reviewOverlay.setVisible(true);
        reviewOverlay.setManaged(true);
        reviewOverlay.getStyleClass().add("open");
    }

    @FXML private void HandleCloseReviewOverlay() {
        currentReview.set(null);
        reviewOverlay.getStyleClass().remove("open");
        reviewOverlay.setVisible(false);
        reviewOverlay.setManaged(false);
    }

    private void loadReviews() throws Exception {
        var response = clientService.requestOwnReviewList();
        List<Review> reviews = response.reviews();

        reviewList.getChildren().clear();

        if (reviews == null || reviews.isEmpty()) {
            reviewList.getChildren().add(new Label("No reviews yet."));
        } else {
            for (Review r : reviews) {
                VBox reviewBox = new VBox(6);

                Label titleLabel = new Label(r.getTitle() != null ? r.getTitle() : "Untitled");
                titleLabel.getStyleClass().add("review-card-title");
                Label ratingLabel = new Label(r.getRating() != null ? "★ " + r.getRating() + " / 5" : "");
                ratingLabel.getStyleClass().add("review-card-rating");
                Region headerSpacer = new Region();
                HBox.setHgrow(headerSpacer, Priority.ALWAYS);
                HBox headerRow = new HBox(4, titleLabel, headerSpacer, ratingLabel);
                headerRow.setAlignment(Pos.CENTER_LEFT);

                Label descLabel = new Label(r.getDescription() != null ? r.getDescription() : "");
                descLabel.getStyleClass().add("review-card-desc");
                descLabel.setWrapText(true);

                Label cardDateLabel = new Label(r.getDate() != null ? r.getDate().toString() : "");
                cardDateLabel.getStyleClass().add("review-card-meta");
                HBox metaRow = new HBox(6, cardDateLabel);
                metaRow.setAlignment(Pos.CENTER_LEFT);
                if (Boolean.TRUE.equals(r.getEdited())) {
                    Label editedLabel = new Label("• edited");
                    editedLabel.getStyleClass().add("review-card-meta");
                    metaRow.getChildren().add(editedLabel);
                }

                reviewBox.getChildren().addAll(headerRow, descLabel, metaRow);
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
            HandleCloseReviewOverlay();
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
