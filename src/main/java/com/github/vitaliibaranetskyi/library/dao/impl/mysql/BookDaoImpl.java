package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.AbstractEntityDao;
import com.github.vitaliibaranetskyi.library.dao.BookDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Disjoint;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.SearchSortColumn;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;
import com.github.vitaliibaranetskyi.library.entity.impl.Author;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.BookStat;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.vitaliibaranetskyi.library.constant.Common.END_MSG;
import static com.github.vitaliibaranetskyi.library.constant.Common.START_MSG;

/**
 * Book DAO. Produce/consume complete entity of {@link Book} class
 */
public class BookDaoImpl implements BookDao {
    private static final Logger logger = LogManager.getLogger(BookDaoImpl.class);
    private static final SearchSortColumn validColumns =
            new SearchSortColumn("title", "isbn", "year", BookDaoLowLevel.AUTHOR_COL);
    private static class BookDaoLowLevel implements AbstractEntityDao<Book> {
        private static final Logger logger = LogManager.getLogger(BookDaoLowLevel.class);
        private static final String AUTHOR_COL = "author";
        private final BaseDao<Book> dao;

        public BookDaoLowLevel(Connection conn) {
            dao = new BaseDao<>(conn);
        }

        @Override
        public void create(Book book) throws DaoException {
            logger.debug(START_MSG);
            final String query = "INSERT INTO book VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

            dao.create(book, query, this::fillStatement);
            for (Author author: book.getAuthors()) {
                createBound(book, author);
            }
        }

        @Override
        public Book read(long id) throws DaoException {
            logger.debug(START_MSG);
            final String query = "SELECT * FROM book WHERE id = ?";

            return dao.read(id, query, this::parse);
        }

        @Override
        public void update(Book book) throws DaoException {
            logger.debug(START_MSG);
            final String query = "UPDATE book SET title = ?, isbn = ?, year = ?, " +
                    "lang_code = ?, keep_period = ?, modified = ? WHERE id = ?";

            dao.update(book, query,
                    (b, ps) -> {
                        int last = fillStatement(b, ps);
                        ps.setLong(last++, b.getId());
                        return last;
                    }
            );
        }

        @Override
        public void delete(long id) throws DaoException {
            logger.debug(START_MSG);
            final String query = "DELETE FROM book WHERE id = ?";

            dao.delete(id, query);
        }

        private Book parse(Connection c, ResultSet rs) throws SQLException {
            Book.Builder builder = new Book.Builder();
            builder.setId(rs.getInt("id"));
            builder.setTitle(rs.getString("title"));
            builder.setIsbn(rs.getString("ISBN"));
            builder.setKeepPeriod(rs.getInt("keep_period"));

            Timestamp sqlTime = rs.getTimestamp("modified");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sqlTime);
            builder.setModified(cal);

            builder.setYear(rs.getDate("year"));
            builder.setLangCode(rs.getString("lang_code"));

            return builder.build();
        }

        private int fillStatement(Book book, PreparedStatement ps) throws SQLException {
            int i = BaseDao.START;
            ps.setString(i++, book.getTitle());
            ps.setString(i++, book.getIsbn());
            ps.setInt(i++, book.getYear());
            ps.setString(i++, book.getLangCode());
            ps.setInt(i++, book.getKeepPeriod());
            if (book.getModified() != null) {
                ps.setTimestamp(i++, new Timestamp(book.getModified().getTimeInMillis()));
            } else {
                throw new SQLException("modified field is null");
            }
            return i;
        }

        public List<Book> findByPattern(String pattern, String searchBy, String sortBy, int num, int page)
                throws DaoException {
            logger.debug(START_MSG);
            final String query = patternQuery(searchBy, sortBy, false);
            return dao.findByPattern(pattern, num, page, query, this::parse)
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
        }

        public List<Book> findBy(String pattern, String searchBy) throws DaoException {
            logger.debug(START_MSG);
            final String query = patternQuery(searchBy, null, true);
            return dao.findByString(pattern, query, this::parse)
                    .stream()
                    .distinct()
                    .collect(Collectors.toList());
        }

        private String patternQuery(String searchBy, String sortBy, boolean exactSearch)  {
            logger.debug(START_MSG);

            final String searchCol = searchBy.equals(AUTHOR_COL) ? "a.name" : "b." + searchBy;
            final String operator = exactSearch ? " = ?" : " LIKE ?";

            String query =  "SELECT b.* FROM book AS b " +
                            "  JOIN book_author as ba " +
                                "ON ba.book_id = b.id " +
                            "  JOIN author_name_i18n AS a " +
                                "ON a.author_id = ba.author_id " +
                            " WHERE " + searchCol + operator;

            if (sortBy != null) {
                final String orderCol = sortBy.equals(AUTHOR_COL) ? "a.name" : "b." + sortBy;
                query = query + " ORDER BY " + orderCol + " LIMIT ? OFFSET ?";
            }

            logger.debug(END_MSG);
            return query;
        }

        public int findByPatternCount(String pattern, String searchBy)
                throws DaoException {
            logger.debug(START_MSG);
            final String query = patternQuery(searchBy, null, false);
            return (int) dao.findByPattern(pattern, query, this::parse)
                    .stream()
                    .distinct()
                    .count();
        }

        public List<Book> getBooksInBooking(long id) throws DaoException {
            logger.debug(START_MSG);
            final String query = "SELECT * FROM book_in_booking WHERE booking_id = ?";

            List<Book> bookGerms = dao.findById(id, query, (c, rs) -> {
                Book.Builder builder = new Book.Builder();
                builder.setId(rs.getInt("book_id"));
                return builder.build();
            });

            List<Book> books = new ArrayList<>();
            for (Book g: bookGerms) {
                Book book = read(g.getId());
                books.add(book);
            }
            logger.debug(END_MSG);
            logger.trace("books={}", books);
            return books;
        }

        public void deleteBound(Book book, Author author) throws DaoException {
            logger.debug(START_MSG);
            final String boundQuery = "DELETE FROM book_author WHERE book_id = ? and author_id = ?";

            dao.updateBound(book.getId(), author.getId(), boundQuery);
            logger.debug(END_MSG);
        }

        public void createBound(Book book, Author author) throws DaoException {
            logger.debug(START_MSG);

            final String boundQuery = "INSERT INTO book_author VALUES (?, ?)";

            dao.updateBound(book.getId(), author.getId(), boundQuery);
            logger.debug(END_MSG);
        }
    }
    private static class BookStatDao {
        private static final Logger logger = LogManager.getLogger(BookStatDao.class);
        private final BaseDao<BookStat> dao;

        public BookStatDao(Connection conn) {
            dao = new BaseDao<>(conn);
        }

        public void create(BookStat bookStat) throws DaoException {
            logger.debug(START_MSG);
            final String query = "INSERT INTO book_stat (total, book_id) VALUES (?, ?)";

            dao.createBound(bookStat.getId(), bookStat, query, this::createFiller);
        }

        private int createFiller(BookStat bookStat, PreparedStatement ps) throws SQLException {
            int i = BaseDao.START;
            ps.setLong(i++, bookStat.getTotal());
            return i;
        }

        public BookStat read(long id) throws DaoException {
            logger.debug(START_MSG);
            final String query = "SELECT * FROM book_stat WHERE book_id = ?";

            return dao.read(id, query, this::parse);
        }

        private BookStat parse(Connection c, ResultSet rs) throws SQLException {
            BookStat.Builder builder = new BookStat.Builder();
            builder.setId(rs.getInt("book_id"));
            builder.setTotal(rs.getInt("total"));
            builder.setInStock(rs.getInt("in_stock"));
            builder.setReserved(rs.getInt("reserved"));
            builder.setTimesWasBooked(rs.getInt("times_was_booked"));
            return builder.build();
        }

        public void update(BookStat entity) throws DaoException {
            logger.debug(START_MSG);

            final String query = "UPDATE book_stat SET total = ?, in_stock = ?, reserved = ?, times_was_booked = ? " +
                    "WHERE book_id = ?";

            dao.update(entity, query, (bookStat, ps) -> {
                        int i = updateFiller(bookStat, ps);
                        ps.setLong(i++, bookStat.getId());
                        return i;
                    }
            );
        }

        private int updateFiller(BookStat bookStat, PreparedStatement ps) throws SQLException {
            int i = BaseDao.START;
            ps.setLong(i++, bookStat.getTotal());
            ps.setLong(i++, bookStat.getInStock());
            ps.setLong(i++, bookStat.getReserved());
            ps.setLong(i++, bookStat.getTimesWasBooked());
            return i;
        }
    }

    private Connection conn;

    /**
     * Way to instantiate class from business logic
     */
    public BookDaoImpl() {}

    /**
     * Way to instantiate class from other DAO or test
     */
    public BookDaoImpl(Connection conn) {
        this.conn = conn;
    }


    @Override
    public void create(Book book) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("Create request: book={}", book);

        Transaction tr = new Transaction(conn);

        tr.transactionWrapper( c -> {
            BookDaoLowLevel bookDao = new BookDaoLowLevel(c);
            bookDao.create(book); // updates author list also

            if (book.getBookStat() == null) {
                throw new DaoException("book stat cannot be null");
            }
            book.getBookStat().setId(book.getId());

            BookStatDao bookStatDao = new BookStatDao(c);
            bookStatDao.create(book.getBookStat());
            // TODO add editing history
        });
    }

    @Override
    public Book read(long id) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("Read request: id={}", id);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BookDaoLowLevel bookDao = new BookDaoLowLevel(c);
            Book book = bookDao.read(id);
            if (book == null) {
                return null;
            }

            List<Book> list = resolveDependencies(c, Collections.singletonList(bookDao.read(id)));
            return list.get(0);
        });
    }

    @Override
    public void update(Book book) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("Update request: book={}", book);

        Transaction tr = new Transaction(conn);
        tr.transactionWrapper( c -> {
            BookDaoLowLevel dao = new BookDaoLowLevel(c);
            dao.update(book);

            BookStatDao bookStatDao = new BookStatDao(c);
            bookStatDao.update(book.getBookStat());

            AuthorDaoImpl authorDao = new AuthorDaoImpl(c);
            Disjoint<Author> disjoint = new Disjoint<>(authorDao.findByBookID(book.getId()), book.getAuthors());

            for (Author author: disjoint.getToDelete()) {
                dao.deleteBound(book, author);
            }

            for (Author author: disjoint.getToAdd()) {
                dao.createBound(book, author);
            }
        });
    }

    @Override
    public void delete(long id) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("Delete request: id={}", id);

        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
                BookDaoLowLevel dao = new BookDaoLowLevel(c);
                dao.delete(id);
                // book_stat deletes by cascade
                // book_author also
        });
    }

    @Override
    public int findByPatternCount(String what, String searchBy)
            throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("request: what={}, searchBy={}",
                what, searchBy);

        validColumns.checkSearch(searchBy);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BookDaoLowLevel dao = new BookDaoLowLevel(c);
            return dao.findByPatternCount(what, searchBy);
        });
    }


    @Override
    public List<Book> findBy(String what, String searchBy) throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("request: what={}, searchBy={}",
                what, searchBy);

        validColumns.checkSearch(searchBy);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BookDaoLowLevel dao = new BookDaoLowLevel(c);
            return resolveDependencies(c, dao.findBy(what, searchBy));
        });
    }

    @Override
    public List<Book> findByPattern(String what, String searchBy, String sortBy, int num, int page)
            throws ServiceException, DaoException {
        logger.debug(START_MSG);
        logger.trace("request: what={}, searchBy={}, sortBy={}, num={}, page={}",
                what, searchBy, sortBy, num, page);

        validColumns.checkSearch(searchBy);
        validColumns.checkSort(sortBy);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BookDaoLowLevel dao = new BookDaoLowLevel(c);
            return resolveDependencies(c, dao.findByPattern(what, searchBy, sortBy, num, page));
        });
    }

    @Override
    public List<Book> getBooksInBooking(long id) throws DaoException {
        logger.debug(START_MSG);
        logger.debug("Get books in booking request: id={}", id);

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BookDaoLowLevel dao = new BookDaoLowLevel(c);

            return resolveDependencies(c, dao.getBooksInBooking(id));
        });
    }

    private List<Book> resolveDependencies(Connection c, List<Book> books) throws DaoException {
        logger.debug(START_MSG);
        if (books == null) {
            return new ArrayList<>();
        }
        AuthorDaoImpl authorDao = new AuthorDaoImpl(c);
        BookStatDao bookStatDao = new BookStatDao(c);

        for (Book b: books) {
            b.setBookStat(bookStatDao.read(b.getId()));
            b.setAuthors(authorDao.findByBookID(b.getId()));
        }
        logger.trace("complete books={}", books);
        logger.debug(END_MSG);
        return books;
    }
}
