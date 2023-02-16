package com.github.vitaliibaranetskyi.library.testutil;

import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;

import java.sql.Connection;
import java.sql.SQLException;

public interface TestBody {
    void accept(Connection c) throws ServiceException, DaoException, SQLException;
}
