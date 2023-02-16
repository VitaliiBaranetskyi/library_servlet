package com.github.vitaliibaranetskyi.library.dao.impl.mysql.func;

import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;

import java.sql.Connection;
/**
 * Interface to be used in lambda-s in {@link Transaction} class.
 * Reads something from DB, should be used in non-transaction
 */
@FunctionalInterface
public interface DaoReader<T> {
    T proceed(Connection c) throws DaoException;
}