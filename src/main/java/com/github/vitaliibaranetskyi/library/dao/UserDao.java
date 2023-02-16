package com.github.vitaliibaranetskyi.library.dao;

import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;

import java.util.List;
/**
 * Functions specific to User class
 */
public interface UserDao extends AbstractSuperDao<User> {
    User findByEmail(String email) throws DaoException;
    List<User> getAll() throws DaoException;
}