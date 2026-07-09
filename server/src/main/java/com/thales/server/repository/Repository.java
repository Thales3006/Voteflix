package com.thales.server.repository;

import java.util.List;

import com.thales.common.model.StatusException;

public interface Repository<T, ID> {
    void create(T entity) throws StatusException;
    void update(T entity) throws StatusException;
    T findById(ID id) throws StatusException;
    List<T> findAll() throws StatusException;
    void delete(ID id) throws StatusException;
}
