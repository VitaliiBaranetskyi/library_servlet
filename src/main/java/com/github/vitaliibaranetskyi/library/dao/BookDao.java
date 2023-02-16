package com.github.vitaliibaranetskyi.library.dao;

import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.exception.DaoException;

import java.util.List;

/**
 * Functions specific to Book class
 */
public interface BookDao extends AbstractSuperDao<Book> {
    List<Book> getBooksInBooking(long id) throws DaoException;
}