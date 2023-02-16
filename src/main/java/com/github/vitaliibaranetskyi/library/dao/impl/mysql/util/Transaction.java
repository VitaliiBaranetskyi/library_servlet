package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import com.github.vitaliibaranetskyi.library.dao.impl.mysql.func.DaoChanger;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.func.DaoReader;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class serves transaction and non-transaction logic for DAO and helps to get connection, if DAO doesn't have it
 */
public class Transaction {
    private static final Logger logger = LogManager.getLogger(Transaction.class);

    private final ConnectionPool pool;
    private boolean close;
    private Connection conn;

    /**
     * For testing purpose is possible to initiate class with your connection pool
     *
     * @param pool ConnectionPool
     * @throws DaoException in case of SQL error
     */
    public Transaction(ConnectionPool pool) throws DaoException {
        this.pool = pool;
        initConnection(null);
    }

    /**
     * Class can be initiated with real Connection, then it'll be used in all operation and won't be closes.
     * This is used for Dao within Dao initialization.
     * <p>
     * If Connection is null, then new connection will be got out of ConnectionPool. Normal Dao operation
     *
     * @param c Connection or null
     * @throws DaoException in case of SQL error
     */
    public Transaction(Connection c) throws DaoException {
        this.pool = ConnectionPool.getInstance();
        initConnection(c);
    }

    /**
     * If connection is null, then get new out of pool.
     *
     * @param c connection or null
     * @throws DaoException in case of SQLException
     */
    private void initConnection(Connection c) throws DaoException {
        if (c == null) {
            logger.trace("Init connection from connection pool...");

            try {
                conn = pool.getConnection();
            } catch (SQLException e) {
                logAndThrow(e);
            }
            logger.trace("Got connection from connection pool: {}", conn);
            close = true;
        } else {
            logger.trace("Got connection from outside: {}", c);
            conn = c;
            close = false;
        }
    }

    private Connection getConnection() {
        return conn;
    }

    private void initTransaction() throws DaoException {
        logger.trace("auto commit = false");
        if (close) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                logAndThrow(e);
            }
        }
    }

    private void close() throws DaoException {
        logger.trace("close connection: {}, close={}", conn, close);
        if (close) {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logAndThrow(e);
            }
        }
    }

    private void logAndThrow(SQLException e) throws DaoException {
        logger.error(e.getMessage());
        throw new DaoException(e.getMessage(), e);
    }

    private void commit() throws DaoException {
        logger.trace("commit connection: {}", conn);
        if (close) {
            logger.trace("close is true, committing...");
            try {
                conn.commit();
            } catch (SQLException e) {
                logAndThrow(e);
            }
        } else {
            logger.trace("close is false, no commit yet");
        }
    }

    private void rollback() throws DaoException {
        logger.trace("rollback connection: {}", conn);
        if (close) {
            logger.trace("close is true, initiate rollback...");
            try {
                conn.rollback();
            } catch (SQLException e) {
                logAndThrow(e);
            }
        } else {
            logger.trace("close is false, no rollback yet");
        }
    }

    /**
     * Set autocommit to false, proceed with changes ({@link DaoChanger}
     * ), commits/rollbacks, close connection if it was got from ConnectionPool and don't close it, if it was passed
     * from another DAO
     *
     * @param changer function which does all change logic in DB
     * @throws DaoException in case of DAO/SQL exception
     */
    public void transactionWrapper(DaoChanger changer) throws DaoException {
        this.initTransaction();
        try {
            Connection c = this.getConnection();

            changer.proceed(c);

            this.commit();
        } catch (DaoException e) {
            this.rollback();
            throw e;
        } finally {
            this.close();
        }
    }

    /**
     * Non-transaction logic: get connection, proceed with read logic, close connection (or not if it was passed from
     * another DAO)
     *
     * @param reader reads info in DB
     * @param <T> Entity type
     * @return entity of given type
     * @throws DaoException in case of SQL/DAO exception
     */
    public <T> T noTransactionWrapper(DaoReader<T> reader) throws DaoException {
        Connection c = this.getConnection();
        T t = reader.proceed(c);
        this.close();
        return t;
    }
}
