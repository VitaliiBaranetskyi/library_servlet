package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.BookStat;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.testutil.DBManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class TestBookingDao {
    private static final DBManager dbManager = DBManager.getInstance();
    private static final long ID = 1;

    private static final String TABLE = "booking";
    private static final String COLUMN = "id";
    private static final String BOOK_IN_BOOKING = "book_in_booking";
    private static final String BOOK_ID = "book_id";
    private static final int NUM = 5;
    private static final int PAGE = 1;


    @Before
    public void initTest() throws ServiceException, IOException, InterruptedException {
        dbManager.executeScript();
    }

    @Test
    public void testRead() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            Booking booking = dao.read(ID);
            Assert.assertNotNull(booking);
            Assert.assertNotNull(booking.getUser());
            Assert.assertNotNull(booking.getBooks());
        });
    }

    @Test
    public void testCreateWithNotEmptyBookList() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            List<Book> books = new ArrayList<>();
            Booking booking = createBooking();

            books.add(new Book.Builder()
                    .setId(ID)
                    .setTitle("test")
                    .setYear(2005)
                    .setIsbn("29302835")
                    .setLangCode("en")
                    .setModified(Calendar.getInstance())
                    .setBookStat(new BookStat.Builder().setTotal(5).build())
                    .build());
            booking.setBooks(books);

            dao.create(booking);
            Assert.assertTrue(dbManager.read(BOOK_IN_BOOKING, BOOK_ID, ID));
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateWithEmptyFieldsShouldThrowException() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            Booking booking = new Booking.Builder().build();

            dao.create(booking);
        });
    }

    @Test
    public void testCreateWithEmptyBookList() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            Booking booking = createBooking();
            dao.create(booking);
            Assert.assertTrue(booking.getId() > 0);
            Assert.assertTrue(dbManager.read(TABLE, COLUMN, booking.getId()));
        });
    }

    private Booking createBooking() {
        return new Booking.Builder()
                .setUser(new User.Builder()
                        .setId(100)
                        .setModified(Calendar.getInstance())
                        .setFineLastChecked(Calendar.getInstance())
                        .build())
                .setBooks(new ArrayList<>())
                .setModified(Calendar.getInstance())
                .build();
    }

    @Test
    public void testUpdateShouldAddOneBookAndPreserveOneBook() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            Booking booking = dao.read(ID);
            long idToDelete = 3;
            long idToAdd = 16;
            Assert.assertNotNull(booking);

            List<Book> books = booking.getBooks();
            Assert.assertEquals(idToDelete, books.get(1).getId());
            books.remove(1);
            books.add(
                    new Book.Builder()
                            .setId(idToAdd)
                            .setTitle("Дураки умирают")
                            .setLangCode("uk")
                            .setIsbn("978-5-699-46418-0")
                            .setYear(2010)
                            .setModified(Calendar.getInstance())
                            .setBookStat(new BookStat.Builder().setTotal(5).build())
                            .build()
            );

            dao.update(booking);
            Assert.assertTrue(dbManager.read(BOOK_IN_BOOKING, BOOK_ID, idToAdd));
            Assert.assertFalse(dbManager.read(BOOK_IN_BOOKING, BOOK_ID, idToDelete));
        });
    }

    @Test
    public void testFindBy() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            List<Booking> booking = dao.findBy("booked", "state");
            Assert.assertNotNull(booking);
            Assert.assertTrue(booking.size() > 0);
        });
    }

    @Test
    public void testFindByPattern() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            List<Booking> booking = dao.findByPattern("user@gmail.com", "email", "email", NUM, PAGE);
            Assert.assertNotNull(booking);
            Assert.assertEquals(1, booking.size());
        });
    }

    @Test
    public void testFindByPatternCount() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            int count = dao.findByPatternCount("user@gmail.com", "email");
            Assert.assertEquals(1, count);
        });
    }

    @Test
    public void testFindDeliveredByUser() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookingDao dao = new BookingDaoImpl(c);
            List<Booking> bookings = dao.findDeliveredByUserID(100);
            Assert.assertNotNull(bookings);
            Assert.assertEquals(1, bookings.size());
            Assert.assertNotNull(bookings.get(0).getBooks());
            Assert.assertEquals(2, bookings.get(0).getBooks().size());
        });
    }
}
