package com.github.vitaliibaranetskyi.library.dao;

import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.exception.DaoException;

import java.util.List;
/**
 * Functions specific to Booking class
 */
public interface BookingDao extends AbstractSuperDao<Booking> {
    List<Booking> findDeliveredByUserID(long id) throws DaoException;
}