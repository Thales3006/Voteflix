package com.thales.client.controller;

import java.util.ArrayList;

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

    @FXML private void initialize(){
        currentReview.addListener((_, _, review) -> {
            if (review != null) {
                titleField.setText(review.getTitle());
                descriptionField.setText(review.getDescription());
                ratingField.setText(String.valueOf(review.getRating()));
                usernameLabel.setText(review.getUsername());
                movieLabel.setText(String.valueOf(review.getMovieID()));
                dateLabel.setText(String.valueOf(review.getDate()));
            }
        });
    }

    @FXML private void HandleRefreshButton(ActionEvent event) {
        handle(event, () -> { 
            var request = clientService.requestOwnReviewList();
            String message = request.getFirst();
            ArrayList<Review> reviews = request.getSecond();

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
                    Label date = new Label("Date: " + (r.getDate() == null ? "" : String.valueOf(r.getDate())));
                    reviewBox.getChildren().addAll(title, description, name, score, date);
                    reviewBox.getStyleClass().add("review-view");
                    reviewBox.setOnMouseClicked(_ -> currentReview.set(r));
                    reviewList.getChildren().add(reviewBox);
                }
            }

            feedback(message);
        });
    }

    @FXML private void HandleUpdateButton(ActionEvent event){
        handle(event, () -> {
            if(currentReview.get() == null){
                throw  new Exception("You have to select a review");
            }
            String message = clientService.requestUpdateReview(currentReview.get());
            feedback(message);
        });
    }

    @FXML private void HandleDeleteButton(ActionEvent event){
        handle(event, () -> {
            if(currentReview.get() == null){
                throw  new Exception("You have to select a review");
            }
            String message = clientService.requestDeleteReview(currentReview.get().getID());
            feedback(message);
        });
    }
}
