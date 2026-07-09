package com.thales.server.service;

import com.thales.common.model.Request.UserRequest;
import com.thales.common.model.Request.*;
import com.thales.common.model.Response;
import com.thales.common.model.Response.*;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;
import com.thales.common.model.User;
import com.thales.server.repository.UserRepository;

public class UserService implements CrudService<UserRequest> {
    private final UserRepository userRepo;
    private final JwtService jwtService;

    public UserService(UserRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Override
    public Response create(UserRequest req) {
        CreateUserRequest r = (CreateUserRequest) req;
        userRepo.create(r.user());
        return new CreatedResponse("User created");
    }

    public Response get(GetUserRequest req) {
        int id = jwtService.verifyAndGetUserId(req.token());
        String username = userRepo.findUsernameById(id);
        return new UserInfoResponse("User info", username);
    }

    @Override
    public Response list(UserRequest req) {
        ListUsersRequest r = (ListUsersRequest) req;
        int id = jwtService.verifyAndGetUserId(r.token());
        if (!userRepo.isAdmin(id)) throw new StatusException(ErrorStatus.FORBIDDEN);
        return new UserListResponse("User list", userRepo.findAll(r.filter()));
    }

    @Override
    public Response update(UserRequest req) {
        UpdateUserRequest r = (UpdateUserRequest) req;
        int requesterId = jwtService.verifyAndGetUserId(r.token());
        if (r.user().getId() != requesterId && !userRepo.isAdmin(requesterId)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        userRepo.update(r.user());
        return new OkResponse("User updated");
    }

    @Override
    public Response delete(UserRequest req) {
        DeleteUserRequest r = (DeleteUserRequest) req;
        int requesterId = jwtService.verifyAndGetUserId(r.token());
        if (r.id() != requesterId && !userRepo.isAdmin(requesterId)) {
            throw new StatusException(ErrorStatus.FORBIDDEN);
        }
        String username = userRepo.findUsernameById(r.id());
        if ("admin".equals(username)) throw new StatusException(ErrorStatus.FORBIDDEN);
        userRepo.delete(r.id());
        return new OkResponse("User deleted");
    }
}
