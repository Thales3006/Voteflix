package com.thales.server.service;

import java.util.List;

import com.thales.common.model.Request.ReviewRequest;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response;
import com.thales.common.model.Response.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.Review;
import com.thales.common.model.StatusException;
import com.thales.server.repository.ReviewRepository;
import com.thales.server.repository.UserRepository;

public class ReviewService implements CrudService<ReviewRequest> {
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

    @Override
    public Response create(ReviewRequest req) {
        CreateReviewRequest r = (CreateReviewRequest) req;
        int userId = jwtService.verifyAndGetUserId(r.token());
        Review review = r.review();
        review.setUserId(userId);
        reviewRepo.create(review);
        return new CreatedResponse("Review posted.");
    }

    @Override
    public Response list(ReviewRequest req) {
        ListReviewsRequest r = (ListReviewsRequest) req;
        jwtService.verifyAndGetUserId(r.token());
        List<Review> reviews = reviewRepo.findAll(r.filter());
        reviews.forEach(this::attachUsername);
        return new ReviewListResponse("OK", reviews);
    }

    @Override
    public Response update(ReviewRequest req) {
        UpdateReviewRequest r = (UpdateReviewRequest) req;
        int userId = jwtService.verifyAndGetUserId(r.token());
        Review review = r.review();
        Review existing = reviewRepo.findById(review.getId());
        if (!existing.getUserId().equals(userId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        reviewRepo.update(review);
        return new OkResponse("Review updated.");
    }

    @Override
    public Response delete(ReviewRequest req) {
        DeleteReviewRequest r = (DeleteReviewRequest) req;
        int userId = jwtService.verifyAndGetUserId(r.token());
        Review existing = reviewRepo.findById(r.id());
        if (!existing.getUserId().equals(userId) && !userRepo.isAdmin(userId)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        reviewRepo.delete(r.id());
        return new OkResponse("Review removed.");
    }
}
