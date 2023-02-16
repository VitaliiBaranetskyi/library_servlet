package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.*;

public class TestBookingExpireTask {
    private DaoFactoryImpl daoFactory;
    private ServletContext context;
    private Booking booking;

    @Before
    public void mockObjects() throws ServiceException, DaoException {
        daoFactory = mock(DaoFactoryImpl.class);
        BookingDao dao = mock(BookingDao.class);
        List<Booking> bookings = new ArrayList<>();
        booking = mock(Booking.class);
        context = mock(ServletContext.class);
        Calendar now = Calendar.getInstance();

        bookings.add(booking);

        when(booking.getModified()).thenReturn(now);
        when(context.getInitParameter(BookingExpireTask.INIT_PARAM_PERIOD)).thenReturn("1");
        when(dao.findBy("BOOKED", "state")).thenReturn(bookings);
        when(daoFactory.getBookingDao()).thenReturn(dao);
    }

    @Test
    public void testInitWasNotCalled() {
        BookingExpireTask task = new BookingExpireTask(daoFactory);
        task.run();

        verifyNoInteractions(daoFactory);
    }

    @Test
    public void testInitCalledWith0ShouldAllowRun() throws ServiceException {
        BookingExpireTask task = new BookingExpireTask(daoFactory);
        when(context.getInitParameter(BookingExpireTask.INIT_PARAM_PERIOD)).thenReturn("0");
        task.init(context);

        task.run();

        verify(daoFactory).getBookingDao();
    }

    @Test
    public void testRunShouldCancelBooking() throws ServiceException {
        BookingExpireTask task = new BookingExpireTask(daoFactory);
        Calendar twoDaysAgo = Calendar.getInstance();

        twoDaysAgo.add(Calendar.DATE, -2);
        when(booking.getModified()).thenReturn(twoDaysAgo);

        task.init(context);
        task.run();

        verify(booking).setState(Booking.State.CANCELED);
    }

    @Test
    public void testRunShouldNotCancelBooking() throws ServiceException {
        BookingExpireTask task = new BookingExpireTask(daoFactory);
        task.init(context);

        task.run();

        verify(booking, times(0)).setState(Booking.State.CANCELED);
    }
}
