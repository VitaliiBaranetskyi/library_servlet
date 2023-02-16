package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.BookDao;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.BookStat;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.testutil.DBManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class TestBookDao {
    public static final String TITLE = "Гарри Поттер. Полное собрание (комплект из 7 книг) (сборник)";
    public static final String ISBN = "978-5-389-10668-0";
    public static final int YEAR = 2016;
    public static final long ID = 1;

    private static final String TABLE = "book";
    private static final String AUTHOR = "author";
    public static final String COLUMN = "title";
    public static final String BAD_INPUT = "sjkldfj;adskjf;asdjf";
    public static final String ISBN_COLUMN = "isbn";

    public static final int NUM = 5;
    public static final int PAGE = 1;
    private static final String BOOK_STAT_TABLE = "book_stat";
    private static final String BOOK_ID_COLUMN = "book_id";
    private static final DBManager dbManager = DBManager.getInstance();

    @Before
    public void initDBConnection() throws ServiceException, IOException, InterruptedException {
        dbManager.executeScript();
    }

    private Book createBook(String title, String isbn, int year, String langCode) {
        Book.Builder b = new Book.Builder();
        b.setTitle(title);
        b.setIsbn(isbn);
        b.setYear(year);
        b.setModified(Calendar.getInstance());
        b.setLangCode(langCode);
        return b.build();
    }

    @Test
    public void testFindByTitlePatternShouldReturnListOfExactOneBook() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern(TITLE,
                    COLUMN, COLUMN, 5, 1);
            Assert.assertEquals(1, books.size());
            Book expected = createBook(TITLE, ISBN, YEAR, "uk");
            Assert.assertEquals(expected, books.get(0));
        });
    }

    @Test
    public void testFindByTitlePatternShouldReturnEmptyList() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern(BAD_INPUT, COLUMN, COLUMN, NUM, PAGE);
            Assert.assertEquals(0, books.size());
        });
    }

    @Test
    public void testFindByISBNPatternShouldReturnListOfOneBook() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern(ISBN, ISBN_COLUMN, ISBN_COLUMN, NUM, PAGE);
            Assert.assertEquals(1, books.size());
        });
    }

    @Test
    public void testFindByISBNPatternShouldReturnEmptyList() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern("11111111111111111", ISBN_COLUMN, ISBN_COLUMN, NUM, PAGE);
            Assert.assertEquals(0, books.size());
        });
    }

    @Test
    public void testFindByAuthorPatternShouldReturnEmptyList() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern(BAD_INPUT, COLUMN, COLUMN, NUM, PAGE);
            Assert.assertEquals(0, books.size());
        });
    }

    @Test
    public void testFindByAuthorPatternShouldReturnListOfOneBook() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern("Джоан", AUTHOR, AUTHOR, NUM, PAGE);
            Assert.assertEquals(1, books.size());
        });
    }

    @Test
    public void testFindByAuthorPatternShouldReturnListOfTwoBook() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.findByPattern("Марио", AUTHOR, AUTHOR, NUM, PAGE);
            Assert.assertEquals(2, books.size());
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateWithEmptyModifiedShouldThrowException() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            Book toCreate = new Book.Builder().setTitle(BAD_INPUT).build();
            bookDao.create(toCreate);
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateWithEmptyBookStatShouldThrowException() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            String title = "new title";
            Book toCreate = createBook(title, "11111234728", 2021, "en");
            bookDao.create(toCreate);
        });
    }

    @Test
    public void testCreate() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            String title = "new title";
            Book toCreate = createBook(title, "11111234728", 2021, "en");

            toCreate.setBookStat(new BookStat.Builder().setTotal(2).build());
            bookDao.create(toCreate);
            Assert.assertTrue(dbManager.read(TABLE, COLUMN, title));
            Assert.assertTrue(dbManager.read(TABLE, COLUMN, title));
        });
    }

    @Test
    public void testDelete() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            bookDao.delete(ID);
            Assert.assertFalse(dbManager.read(TABLE, COLUMN, TITLE));
            Assert.assertFalse(dbManager.read(BOOK_STAT_TABLE, BOOK_ID_COLUMN, ID));
        });
    }

    @Test
    public void testUpdate() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            Book book = bookDao.read(ID);
            String newTitle = book.getTitle() + "edited";
            book.setTitle(newTitle);
            bookDao.update(book);
            Assert.assertTrue(dbManager.read(TABLE, COLUMN, newTitle));
        });
    }

    @Test
    public void testFindByPatternCount() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            int count = bookDao.findByPatternCount(TITLE, COLUMN);
            Assert.assertEquals(1, count);
        });
    }

    @Test
    public void testGetBooksInBooking() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            BookDao bookDao = new BookDaoImpl(c);
            List<Book> books = bookDao.getBooksInBooking(ID);
            Assert.assertEquals(2, books.size());
            Assert.assertEquals(TITLE, books.get(0).getTitle());
        });
    }
}
