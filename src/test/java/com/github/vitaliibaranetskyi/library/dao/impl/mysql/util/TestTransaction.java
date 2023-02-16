package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class TestTransaction {
    private Connection conn;
    private ConnectionPool pool;

    @Before
    public void initConnection() throws SQLException {
        pool = mock(ConnectionPool.class);
        conn = mock(Connection.class);

        when(pool.getConnection()).thenReturn(conn);
    }

    @Test
    public void testTransactionWrapperNormalFlowWithConnectionFromOutside() throws DaoException, SQLException {
        Transaction tr = new Transaction(conn);

        tr.transactionWrapper( c -> {});
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).commit();
        verify(conn, times(0)).close();
    }

    @Test(expected = DaoException.class)
    public void testTransactionWrapperExceptionFlowWithConnectionFromOutside() throws DaoException, SQLException {
        Transaction tr = new Transaction(conn);

        tr.transactionWrapper( c -> { throw new DaoException("test");});
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).rollback();
        verify(conn, times(0)).close();
    }

    @Test
    public void testNoTransactionWrapperNormalFlowWithConnectionFromOutside() throws DaoException, SQLException {
        Transaction tr = new Transaction(conn);

        tr.noTransactionWrapper( c -> new User.Builder().build());
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).commit();
        verify(conn, times(0)).close();
    }

    @Test(expected = DaoException.class)
    public void testNoTransactionWrapperExceptionFlowWithConnectionFromOutside() throws DaoException, SQLException {
        Transaction tr = new Transaction(conn);

        tr.noTransactionWrapper( c -> { throw new DaoException("test");});
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).rollback();
        verify(conn, times(0)).close();
    }

    @Test
    public void testTransactionWrapperNormalFlow() throws DaoException, SQLException {
        InOrder inOrder = inOrder(conn);
        Transaction tr = new Transaction(pool);

        tr.transactionWrapper( c -> {});
        inOrder.verify(conn, times(1)).setAutoCommit(false);
        inOrder.verify(conn, times(1)).commit();
        inOrder.verify(conn, times(1)).close();
        verify(conn, times(0)).rollback();
    }

    @Test(expected = DaoException.class)
    public void testTransactionWrapperExceptionFlow() throws DaoException, SQLException {
        InOrder inOrder = inOrder(conn);
        Transaction tr = new Transaction(conn);

        tr.transactionWrapper( c -> { throw new DaoException("test");});
        inOrder.verify(conn, times(1)).setAutoCommit(false);
        inOrder.verify(conn, times(1)).rollback();
        inOrder.verify(conn, times(1)).close();
        verify(conn, times(0)).commit();
    }

    @Test
    public void testNoTransactionWrapperNormalFlow() throws DaoException, SQLException {
        Transaction tr = new Transaction(pool);

        tr.noTransactionWrapper( c -> new User.Builder().build());
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).rollback();
        verify(conn, times(0)).commit();
        verify(conn, times(1)).close();
    }

    @Test(expected = DaoException.class)
    public void testNoTransactionWrapperExceptionFlow() throws DaoException, SQLException {
        Transaction tr = new Transaction(conn);

        tr.noTransactionWrapper( c -> { throw new DaoException("test");});
        verify(conn, times(0)).setAutoCommit(false);
        verify(conn, times(0)).rollback();
        verify(conn, times(0)).commit();
        verify(conn, times(1)).close();
    }
}
