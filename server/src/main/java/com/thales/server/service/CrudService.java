package com.thales.server.service;

import com.thales.common.model.AppRequest;
import com.thales.common.model.AppResponse;

public interface CrudService<R extends AppRequest> {
    AppResponse create(R req);
    AppResponse list(R req);
    AppResponse update(R req);
    AppResponse delete(R req);
}
