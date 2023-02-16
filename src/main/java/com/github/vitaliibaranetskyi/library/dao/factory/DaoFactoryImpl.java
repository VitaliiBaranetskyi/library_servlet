package com.github.vitaliibaranetskyi.library.dao.factory;

import com.github.vitaliibaranetskyi.library.dao.*;

/**
 * Concrete factory interface
 */
public interface DaoFactoryImpl {
    UserDao getUserDao();
    BookingDao getBookingDao();
    BookDao getBookDao();
    AuthorDao getAuthorDao();
    LangDao getLangDao();
}
