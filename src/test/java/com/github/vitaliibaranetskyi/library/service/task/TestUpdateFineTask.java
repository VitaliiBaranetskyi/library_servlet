package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.dao.UserDao;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.github.vitaliibaranetskyi.library.service.task.UpdateFineTask.INIT_PARAM_FINE_PER_DAY;
import static org.mockito.Mockito.*;


public class TestUpdateFineTask {
    private DaoFactoryImpl daoFactory;
    private Booking booking;
    private User user;

    @Before
    public void mockDaoFactory() throws DaoException {
        long id = 1;
        daoFactory = mock(DaoFactoryImpl.class);
        user = mock(User.class);
        booking = mock(Booking.class);

        Book book = mock(Book.class);
        BookingDao bookingDao = mock(BookingDao.class);
        UserDao userDao = mock(UserDao.class);
        List<User> users = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();
        List<Book> books = new ArrayList<>();

        Calendar twoDaysBefore = Calendar.getInstance();
        twoDaysBefore.add(Calendar.DATE, -2);
        when(booking.getModified()).thenReturn(twoDaysBefore);

        books.add(book);
        bookings.add(booking);
        users.add(user);

        when(user.getId()).thenReturn(id);
        when(user.getFine()).thenReturn(0.0);
        when(user.getFineLastChecked()).thenReturn(twoDaysBefore);
        when(user.getModified()).thenReturn(twoDaysBefore);

        when(book.getKeepPeriod()).thenReturn(1);
        when(booking.getBooks()).thenReturn(books);
        when(userDao.getAll()).thenReturn(users);
        when(bookingDao.findDeliveredByUserID(id)).thenReturn(bookings);

        when(daoFactory.getBookingDao()).thenReturn(bookingDao);
        when(daoFactory.getUserDao()).thenReturn(userDao);
    }

    @Test
    public void testInitWasNotCalled() {
        UpdateFineTask task = new UpdateFineTask(daoFactory);
        task.run();
        verifyNoInteractions(daoFactory);
    }

    @Test
    public void testSetUpUserFine() throws ServiceException {
        UpdateFineTask task = new UpdateFineTask(daoFactory);
        ServletContext context = mock(ServletContext.class);
        when(context.getInitParameter(INIT_PARAM_FINE_PER_DAY)).thenReturn("1");
        task.init(context);

        task.run();

        verify(user).setFine(1.0);
    }

    @Test
    public void testNotSetUpUserFineOnNewBooking() throws ServiceException {
        UpdateFineTask task = new UpdateFineTask(daoFactory);
        ServletContext context = mock(ServletContext.class);
        when(context.getInitParameter(INIT_PARAM_FINE_PER_DAY)).thenReturn("1");
        task.init(context);

        when(booking.getModified()).thenReturn(Calendar.getInstance());
        task.run();

        verify(user, times(0)).setFine(1.0);
    }

    @Test
    public void testNotSetUpUserFineMoreThanOneTimeADay() throws ServiceException {
        UpdateFineTask task = new UpdateFineTask(daoFactory);
        ServletContext context = mock(ServletContext.class);
        when(context.getInitParameter(INIT_PARAM_FINE_PER_DAY)).thenReturn("1");
        when(user.getFineLastChecked()).thenReturn(Calendar.getInstance());

        task.init(context);
        task.run();

        verify(user, times(0)).setFine(1.0);
    }
}
