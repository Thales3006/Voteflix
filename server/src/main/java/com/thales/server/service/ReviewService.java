package com.thales.server.service;

import java.util.List;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;

public class ReviewService {
    private final DatabaseService database;
    private final JwtService jwtService;

    public ReviewService(DatabaseService database, JwtService jwtService) {
        this.database = database;
        this.jwtService = jwtService;
    }

    private void attachUsername(Review review) {
        if (review.getUserId() == null) return;
        try {
            review.setUsername(database.getUsername(review.getUserId()));
        } catch (StatusException e) {}
    }

    public AppResponse handleCreateReview(CreateReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review review = req.review();
        review.setUserId(userId);
        database.createReview(review);
        return new CreatedResponse("Review created");
    }

    public AppResponse handleListOwnReviews(ListOwnReviewsRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        List<Review> reviews = database.getUserReviews(userId);
        reviews.forEach(this::attachUsername);
        return new ReviewListResponse("Reviews", reviews);
    }

    public AppResponse handleListReviews(ListReviewsRequest req) {
        jwtService.verifyAndGetUserId(req.token());
        List<Review> reviews = database.getMovieReviews(req.movieId());
        reviews.forEach(this::attachUsername);
        return new ReviewListResponse("Reviews", reviews);
    }

    public AppResponse handleUpdateReview(UpdateReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review review = req.review();
        Review existing = database.getReview(review.getId());
        if (!existing.getUserId().equals(userId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.updateReview(review);
        return new OkResponse("Review updated");
    }

    public AppResponse handleDeleteReview(DeleteReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review existing = database.getReview(req.id());
        if (!existing.getUserId().equals(userId) && !database.isAdmin(userId)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        database.deleteReview(req.id());
        return new OkResponse("Review deleted");
    }
}
