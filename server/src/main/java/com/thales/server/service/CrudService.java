package com.thales.server.service;

import com.thales.common.model.Request;
import com.thales.common.model.Response;

public interface CrudService<R extends Request> {
    Response create(R req);
    Response list(R req);
    Response update(R req);
    Response delete(R req);
}
