package com.github.vitaliibaranetskyi.library.dao.impl.mysql.func;

import com.github.vitaliibaranetskyi.library.entity.Entity;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
/**
 * Interface to be used in lambda-s in {@link BaseDao} class.
 * Fills Prepared statement with data from Entity of given type
 */
@FunctionalInterface
public interface StatementFiller<T extends Entity> {
    int accept(T entity, PreparedStatement ps) throws SQLException;
}