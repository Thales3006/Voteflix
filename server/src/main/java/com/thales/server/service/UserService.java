package com.thales.server.service;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;

public class UserService {
    private final DatabaseService database;
    private final JwtService jwtService;

    public UserService(DatabaseService database, JwtService jwtService) {
        this.database = database;
        this.jwtService = jwtService;
    }

    public AppResponse handleCreateUser(CreateUserRequest req) {
        database.createUser(req.username(), req.password());
        return new CreatedResponse("User created");
    }

    public AppResponse handleListOwnUser(ListOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        String username = database.getUsername(id);
        return new UserInfoResponse("User info", username);
    }

    public AppResponse handleListUsers(ListUsersRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        return new UserListResponse("User list", database.getUsers());
    }

    public AppResponse handleUpdateOwnUser(UpdateOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        database.updateUser(id, req.password());
        return new OkResponse("User updated");
    }

    public AppResponse handleAdminUpdateUser(AdminUpdateUserRequest req) {
        int requesterId = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(requesterId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.updateUser(req.id(), req.password());
        return new OkResponse("User updated");
    }

    public AppResponse handleDeleteOwnUser(DeleteOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        String username = database.getUsername(id);
        if ("admin".equals(username)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.deleteUser(id);
        return new OkResponse("User deleted");
    }

    public AppResponse handleAdminDeleteUser(AdminDeleteUserRequest req) {
        int requesterId = jwtService.verifyAndGetUserId(req.token());
        if (!database.isAdmin(requesterId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        String username = database.getUsername(req.id());
        if ("admin".equals(username)) throw new StatusException(ErrorStatus.FORBIDDEN);
        database.deleteUser(req.id());
        return new OkResponse("User deleted");
    }
}
