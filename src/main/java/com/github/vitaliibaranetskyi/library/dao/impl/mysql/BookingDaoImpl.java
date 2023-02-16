package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.BookDao;
import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Disjoint;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.SearchSortColumn;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.github.vitaliibaranetskyi.library.constant.Common.START_MSG;

/**
 * Booking DAO. Produce/consume complete entity of {@link Booking} class
 */
public class BookingDaoImpl implements BookingDao {
    private static final Logger logger = LogManager.getLogger(BookingDaoImpl.class);
    private static final String BOOKING_STATE_COL = "state";
    private static final String BOOKING_LOCATED_COL = "located";
    private static final SearchSortColumn validColumns =
            new SearchSortColumn("email", "name", BOOKING_STATE_COL, BOOKING_LOCATED_COL);
    private Connection conn;

    /**
     * Way to instantiate class from business logic
     */
    public BookingDaoImpl() {}

    /**
     * Way to instantiate class from other DAO or test
     */
    public BookingDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void create(Booking booking) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("booking={}", booking);

        // user id we don't change
        final String query = "INSERT INTO booking VALUES(DEFAULT, ?, ?, ?, ?)";
        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);

            dao.create(booking, query, this::statementFiller);

            createBooksInBooking(new BaseDao<>(c), new BookDaoImpl(c), booking.getId(), booking.getBooks());
        });
        logger.debug("create booking request finished");
    }

    @Override
    public Booking read(long id) throws DaoException {
        logger.debug("read booking request init...");
        logger.trace("id={}", id);

        final String query = "SELECT * FROM booking WHERE id = ?";
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            Booking booking = dao.read(id, query, this::parse);

            BookDaoImpl bookDao = new BookDaoImpl(c);
            booking.setBooks(bookDao.getBooksInBooking(id));
            logger.debug("read booking request finished");
            return booking;
        });
    }

    @Override
    public void update(Booking booking) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("booking={}", booking);

        // user id we don't change
        final String query = "UPDATE booking SET user_id = ?, state = ?, located = ?, modified = ? WHERE id = ?";
        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            dao.update(booking, query, (b, ps) -> { int i = statementFiller(b, ps); ps.setLong(i++, b.getId()); return i; });

            BookDaoImpl bookDao = new BookDaoImpl(c);
            List<Book> oldList = bookDao.getBooksInBooking(booking.getId());
            updateBooksInBooking(c, booking.getId(), oldList, booking.getBooks());
        });
        logger.debug("update booking request finished");
    }

    private int statementFiller(Booking booking, PreparedStatement ps) throws SQLException {
        logger.debug("fill statement");

        int i = BaseDao.START;
        if (booking.getUser() == null) {
            throw new SQLException("user field cannot be null");
        }

        ps.setLong(i++, booking.getUser().getId());
        ps.setString(i++, booking.getState().name());
        ps.setString(i++, booking.getLocated().name());
        if (booking.getModified() == null) {
            throw new SQLException("modified field cannot be null");
        }
        ps.setTimestamp(i++, new Timestamp(booking.getModified().getTimeInMillis()));

        logger.debug("finish filling statement");
        return i;
    }

    @Override
    public void delete(long id) {
        throw new UnsupportedOperationException("not yet supported");
    }

    @Override
    public List<Booking> findDeliveredByUserID(long id) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}", id);

        final String query = "SELECT * FROM booking WHERE state = 'DELIVERED' AND user_id = ?";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            List<Booking> bookings = dao.findById(id, query, this::parse);

            BookDaoImpl bookDao = new BookDaoImpl(c);
            for (Booking b: bookings) {
                List<Book> books = bookDao.getBooksInBooking(b.getId());
                b.setBooks(books);
            }
            return bookings;
        });
    }

    private Booking parse(Connection c, ResultSet rs) throws SQLException, DaoException {
        logger.debug("result set parsing init...");
        Booking.Builder builder = new Booking.Builder();

        builder.setId(rs.getInt("id"));
        builder.setState(Booking.State.valueOf(rs.getString(BOOKING_STATE_COL)));
        builder.setLocated(Booking.Place.valueOf(rs.getString(BOOKING_LOCATED_COL)));

        Timestamp sqlTimestamp = rs.getTimestamp("modified");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sqlTimestamp);
        builder.setModified(cal);

        // get dependencies
        long userID = rs.getInt("user_id");
        UserDaoImpl userDao = new UserDaoImpl(c);
        User user = userDao.read(userID);
        builder.setUser(user);

        logger.debug("result set parsing finished");
        return builder.build();
    }

    @Override
    public List<Booking> findByPattern(String what, String searchBy, String sortBy, int num, int page) throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("what={}, searchBy={}, sortBy={}, num={}, page={}", what, searchBy, sortBy, num, page);

        final String query = patternQuery(searchBy, sortBy, false, false);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            List<Booking> bookings = dao.findByPattern(what, num, page,query, this::parse);

            BookDaoImpl bookDao = new BookDaoImpl(c);
            for (Booking b: bookings) {
                List<Book> books = bookDao.getBooksInBooking(b.getId());
                b.setBooks(books);
            }

            return bookings;
        });
    }

    @Override
    public int findByPatternCount(String what, String searchBy) throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("what={}, searchBy={}", what, searchBy);

        final String query = patternQuery(searchBy, null, true, false);
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            return dao.count(what, query);
        });
    }

    @Override
    public List<Booking> findBy(String what, String searchBy) throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("what={}, searchBy={}", what, searchBy);

        validColumns.checkSearch(searchBy);
        final String query = patternQuery(searchBy, null, false, true);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BaseDao<Booking> dao = new BaseDao<>(c);
            List<Booking> bookings = dao.findByString(what, query, this::parse);

            BookDaoImpl bookDao = new BookDaoImpl(c);
            for (Booking b: bookings) {
                List<Book> books = bookDao.getBooksInBooking(b.getId());
                b.setBooks(books);
            }

            return bookings;
        });
    }

    private String patternQuery(String searchBy, String sortBy, boolean count, boolean exactSearch) throws ServiceException {
        logger.debug(START_MSG);
        logger.trace("searchBy={}, sortBy={}, count={}, exactSearch={}",
                searchBy, sortBy, count, exactSearch);

        validColumns.checkSearch(searchBy);

        final String searchCol = searchBy.equals(BOOKING_STATE_COL) || searchBy.equals(BOOKING_LOCATED_COL)
                ? "b." + searchBy : "u." + searchBy;
        final String what = count ? "COUNT(*)" : "*";
        final String operator = exactSearch ? " = ?" : " LIKE ?";

        String query = "SELECT " + what + " FROM booking AS b\n" +
                "  JOIN user AS u\n" +
                "    ON u.id = b.user_id\n" +
                " WHERE " + searchCol + operator;

        if (sortBy != null) {
            validColumns.checkSort(sortBy);

            final String orderCol = sortBy.equals(BOOKING_STATE_COL) ? "b." + sortBy : "u." + sortBy;
            query = query + " ORDER BY " + orderCol + " LIMIT ? OFFSET ?";
        }
        return query;
    }

    private void updateBooksInBooking(Connection c, long id, List<Book> oldList, List<Book> newList) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, old book list={}, new book list={}", id, oldList, newList);


        Disjoint<Book> disjoint = new Disjoint<>(oldList, newList);

        BaseDao<Book> dao = new BaseDao<>(c);
        BookDao bookDao = new BookDaoImpl(c);
        createBooksInBooking(dao, bookDao, id, disjoint.getToAdd());
        deleteBooksInBooking(dao, bookDao, id, disjoint.getToDelete());

        // here we need to update book stat in common elements also
        List<Book> common = new ArrayList<>(newList);
        common.retainAll(oldList);
        for (Book b : common) {
            bookDao.update(b);
        }
    }

    private void deleteBooksInBooking(BaseDao<Book> dao, BookDao bookDao, long id, List<Book> books) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, books={}", id, books);

        // book_id, author_id
        final String delQuery = "DELETE FROM book_in_booking WHERE booking_id = ? AND book_id = ?";

        for (Book b: books) {
            dao.deleteBound(id, b.getId(), delQuery);
            bookDao.update(b);
        }
    }

    private void createBooksInBooking(BaseDao<Book> dao, BookDao bookDao, long id, List<Book> books) throws DaoException {
        // book_id, author_id
        logger.debug(START_MSG);
        logger.trace("id={}, books={}", id, books);

        final String addQuery = "INSERT INTO book_in_booking VALUES (?, ?)";

        for (Book b: books) {
            logger.trace("update");
            dao.update(b, addQuery, (book, ps) -> {
                int i = BaseDao.START;
                ps.setLong(i++, id);
                ps.setLong(i++, book.getId());
                return i;
            });
            bookDao.update(b);
        }
    }
}
