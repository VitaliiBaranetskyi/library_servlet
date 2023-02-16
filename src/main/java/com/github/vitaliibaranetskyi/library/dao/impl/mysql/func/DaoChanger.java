package com.github.vitaliibaranetskyi.library.dao.impl.mysql.func;

import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;

import java.sql.Connection;

/**
 * Interface to be used in lambda-s in {@link Transaction} class.
 * Changes something in DB, should be used only in transaction
 */
@FunctionalInterface
public interface DaoChanger {
    void proceed(Connection c) throws DaoException;
}