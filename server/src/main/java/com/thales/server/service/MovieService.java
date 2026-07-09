package com.thales.server.service;

import com.thales.common.model.Request.MovieRequest;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response;
import com.thales.common.model.Response.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;
import com.thales.server.repository.MovieRepository;
import com.thales.server.repository.UserRepository;

public class MovieService implements CrudService<MovieRequest> {
    private final MovieRepository movieRepo;
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public MovieService(MovieRepository movieRepo, UserRepository userRepo, JwtService jwtService) {
        this.movieRepo = movieRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Override
    public Response create(MovieRequest req) {
        CreateMovieRequest r = (CreateMovieRequest) req;
        int id = jwtService.verifyAndGetUserId(r.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.create(r.movie());
        return new CreatedResponse("Movie created");
    }

    @Override
    public Response list(MovieRequest req) {
        ListMoviesRequest r = (ListMoviesRequest) req;
        jwtService.verifyAndGetUserId(r.token());
        return new MovieListResponse("Movie list", movieRepo.findAll(r.filter()));
    }

    @Override
    public Response update(MovieRequest req) {
        UpdateMovieRequest r = (UpdateMovieRequest) req;
        int id = jwtService.verifyAndGetUserId(r.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.update(r.movie());
        return new OkResponse("Movie updated");
    }

    @Override
    public Response delete(MovieRequest req) {
        DeleteMovieRequest r = (DeleteMovieRequest) req;
        int id = jwtService.verifyAndGetUserId(r.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        movieRepo.delete(r.id());
        return new OkResponse("Movie deleted");
    }
}
