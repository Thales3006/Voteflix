package com.thales.server.service;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;

public class MovieService {
    private final DatabaseService database;
    private final JwtService jwtService;

    public MovieService(DatabaseService database, JwtService jwtService) {
        this.database = database;
        this.jwtService = jwtService;
    }

    public AppResponse handleCreateMovie(CreateMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.createMovie(req.movie());
        return new CreatedResponse("Movie created");
    }

    public AppResponse handleListMovies(ListMoviesRequest req) {
        jwtService.verifyAndGetUserId(req.token());
        return new MovieListResponse("Movie list", database.getMovies());
    }

    public AppResponse handleUpdateMovie(UpdateMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.updateMovie(req.movie());
        return new OkResponse("Movie updated");
    }

    public AppResponse handleDeleteMovie(DeleteMovieRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.deleteMovie(req.id());
        return new OkResponse("Movie deleted");
    }
}
