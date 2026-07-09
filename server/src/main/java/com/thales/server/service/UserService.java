package com.thales.server.service;

import com.thales.common.model.AppRequest.*;
import com.thales.common.model.AppResponse;
import com.thales.common.model.AppResponse.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.server.database.UserRepository;

public class UserService {
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public UserService(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public AppResponse handleCreateUser(CreateUserRequest req) {
        userRepo.create(new User(req.username(), req.password()));
        return new CreatedResponse("User created");
    }

    public AppResponse handleListOwnUser(ListOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        String username = userRepo.findUsernameById(id);
        return new UserInfoResponse("User info", username);
    }

    public AppResponse handleListUsers(ListUsersRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        return new UserListResponse("User list", userRepo.findAll());
    }

    public AppResponse handleUpdateOwnUser(UpdateOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        userRepo.update(new User(id, null, req.password()));
        return new OkResponse("User updated");
    }

    public AppResponse handleAdminUpdateUser(AdminUpdateUserRequest req) {
        int requesterId = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(requesterId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        userRepo.update(new User(req.id(), null, req.password()));
        return new OkResponse("User updated");
    }

    public AppResponse handleDeleteOwnUser(DeleteOwnUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        String username = userRepo.findUsernameById(id);
        if ("admin".equals(username)) throw new StatusException(ErrorStatus.FORBIDDEN);
        userRepo.delete(id);
        return new OkResponse("User deleted");
    }

    public AppResponse handleAdminDeleteUser(AdminDeleteUserRequest req) {
        int requesterId = jwtService.verifyAndGetUserId(req.token());
        if (!userRepo.isAdmin(requesterId)) throw new StatusException(ErrorStatus.FORBIDDEN);
        String username = userRepo.findUsernameById(req.id());
        if ("admin".equals(username)) throw new StatusException(ErrorStatus.FORBIDDEN);
        userRepo.delete(req.id());
        return new OkResponse("User deleted");
    }
}
