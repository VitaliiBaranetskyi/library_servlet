package com.github.vitaliibaranetskyi.library.dao.factory.impl.db;

import com.github.vitaliibaranetskyi.library.dao.*;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.*;

/**
 * DBFactory implementation for MySQL
 */
public class MySQLDaoFactory implements DaoFactoryImpl {

    @Override
    public UserDao getUserDao() {
        return new UserDaoImpl();
    }

    @Override
    public BookingDao getBookingDao() {
        return new BookingDaoImpl();
    }

    @Override
    public BookDao getBookDao() {
        return new BookDaoImpl();
    }

    @Override
    public AuthorDao getAuthorDao() {
        return new AuthorDaoImpl();
    }

    @Override
    public LangDao getLangDao() {
        return new LangDaoImpl();
    }

}