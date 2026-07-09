package com.thales.server.service;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;
import com.thales.server.database.MovieRepository;
import com.thales.server.database.UserRepository;

public class MovieService {
    private final MovieRepository movieRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public MovieService(MovieRepository movieRepo, UserRepository userRepo, JwtService jwtService) {
        this.movieRepo = movieRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public AppResponse handleCreateMovie(CreateMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.create(req.movie());
        return new CreatedResponse("Movie created");
    }

    public AppResponse handleListMovies(ListMoviesRequest req) {
        jwtService.verifyAndGetUserId(req.token());
        return new MovieListResponse("Movie list", movieRepo.findAll());
    }

    public AppResponse handleUpdateMovie(UpdateMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.update(req.movie());
        return new OkResponse("Movie updated");
    }

    public AppResponse handleDeleteMovie(DeleteMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.delete(req.id());
        return new OkResponse("Movie deleted");
    }
}
