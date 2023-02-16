package com.github.vitaliibaranetskyi.library.dao.impl.mysql.func;

import com.github.vitaliibaranetskyi.library.entity.Entity;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * Interface to be used in lambda-s in {@link BaseDao} class.
 * Parses Result Set to Entity of given type
 */
@FunctionalInterface
public interface EntityParser<T extends Entity> {
    T accept(Connection c, ResultSet rs) throws SQLException, DaoException;
}