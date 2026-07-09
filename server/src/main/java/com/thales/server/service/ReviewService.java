package com.thales.server.service;

import java.util.List;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;
import com.thales.server.repository.ReviewRepository;
import com.thales.server.repository.UserRepository;

public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public ReviewService(ReviewRepository reviewRepo, UserRepository userRepo, JwtService jwtService) {
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    private void attachUsername(Review review) {
        if (review.getUserId() == null) return;
        try {
            review.setUsername(userRepo.findUsernameById(review.getUserId()));
        } catch (StatusException e) {}
    }

    public AppResponse handleCreateReview(CreateReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review review = req.review();
        review.setUserId(userId);
        reviewRepo.create(review);
        return new CreatedResponse("Review created");
    }

    public AppResponse handleListOwnReviews(ListOwnReviewsRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        List<Review> reviews = reviewRepo.findByUserId(userId);
        reviews.forEach(this::attachUsername);
        return new ReviewListResponse("Reviews", reviews);
    }

    public AppResponse handleListReviews(ListReviewsRequest req) {
        jwtService.verifyAndGetUserId(req.token());
        List<Review> reviews = reviewRepo.findByMovieId(req.movieId());
        reviews.forEach(this::attachUsername);
        return new ReviewListResponse("Reviews", reviews);
    }

    public AppResponse handleUpdateReview(UpdateReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review review = req.review();
        Review existing = reviewRepo.findById(review.getId());
        if (!existing.getUserId().equals(userId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        reviewRepo.update(review);
        return new OkResponse("Review updated");
    }

    public AppResponse handleDeleteReview(DeleteReviewRequest req) {
        int userId = jwtService.verifyAndGetUserId(req.token());
        Review existing = reviewRepo.findById(req.id());
        if (!existing.getUserId().equals(userId) && !userRepo.isAdmin(userId)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        reviewRepo.delete(req.id());
        return new OkResponse("Review deleted");
    }
}
