package com.github.vitaliibaranetskyi.library.dao;

import com.github.vitaliibaranetskyi.library.entity.Entity;
import com.github.vitaliibaranetskyi.library.exception.DaoException;

/**
 * Common function for all entities
 * @param <E> sub-class of Entity
 */
public interface AbstractEntityDao<E extends Entity> {
    void create(E entity) throws DaoException;
    E read(long id) throws DaoException;
    void update(E entity) throws DaoException;
    void delete(long id) throws DaoException;
}