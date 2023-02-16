package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.AuthorDao;
import com.github.vitaliibaranetskyi.library.entity.impl.Author;
import com.github.vitaliibaranetskyi.library.entity.impl.I18AuthorName;
import com.github.vitaliibaranetskyi.library.entity.impl.Lang;
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

public class TestAuthorDaoImpl {
    private static final DBManager dbManager = DBManager.getInstance();

    private static final String AUTHOR_NAME = "Джоан Кэтлин Роулинг";
    private static final String AUTHOR_NAME_EN = "Joanne Kathleen Rowling";
    private static final String NEW_AUTHOR = "TEST TEST";
    private static final String NEW_AUTHOR_UK = "Тест Тест";
    private static final String TO_DELETE_AUTHOR = "TO DELETE";
    private static final String BAD_INPUT = "akjsd;faklsdj asdf";
    private static final String TABLE = "author";
    private static final String COLUMN = "name";
    private static final String I18_TABLE = "author_name_i18n";
    private static final long TO_DELETE_ID = 99;
    private static final long ID = 1;
    private static final Lang[] supportedLangs =
            {   new Lang.Builder().setId(1).setCode("en").build(),
                    new Lang.Builder().setId(2).setCode("uk").build()
            };

    @Before
    public void initTest() throws ServiceException, IOException, InterruptedException {
        dbManager.executeScript();
    }

    @Test
    public void testCreate() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author toCreate = createAuthor(NEW_AUTHOR, NEW_AUTHOR, NEW_AUTHOR_UK);

            authorDao.create(toCreate);

            Assert.assertTrue(dbManager.read(TABLE, COLUMN, NEW_AUTHOR));
            Assert.assertTrue(dbManager.read(I18_TABLE, COLUMN, NEW_AUTHOR));
            Assert.assertTrue(toCreate.getId() > 0);
        });
    }

    @Test
    public void testReadByName() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author read = authorDao.read(AUTHOR_NAME);
            Author expected = createAuthor(AUTHOR_NAME, AUTHOR_NAME, AUTHOR_NAME_EN);

            Assert.assertEquals(expected, read);
            Assert.assertTrue(read.getId() == ID);
        });
    }

    @Test
    public void testReadByID() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper(c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author read = authorDao.read(ID);
            Author expected = createAuthor(AUTHOR_NAME, AUTHOR_NAME, AUTHOR_NAME_EN);

            Assert.assertEquals(expected, read);
            Assert.assertTrue(read.getId() == ID);
        });
    }
    
    @Test
    public void testUpdatePrimaryName() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            String newName = AUTHOR_NAME + "edit";
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author toUpdate = createAuthor(AUTHOR_NAME, AUTHOR_NAME, AUTHOR_NAME_EN);
            toUpdate.setId(ID);

            toUpdate.setName(newName);
            authorDao.update(toUpdate);

            Assert.assertTrue(dbManager.read(TABLE, COLUMN, newName));
        });
    }

    @Test
    public void testUpdateI18Name() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            String newName = AUTHOR_NAME + "edit";
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author toUpdate = createAuthor(AUTHOR_NAME, AUTHOR_NAME, AUTHOR_NAME_EN);
            toUpdate.setId(ID);

            if (toUpdate.getI18Names() == null || toUpdate.getI18Names().isEmpty()) {
                Assert.fail("You should set I18Names for this test");
            }

            toUpdate.setName(new Lang.Builder().setId(1).setCode("en").build(), newName);
            authorDao.update(toUpdate);

            Assert.assertTrue(dbManager.read(I18_TABLE, COLUMN, newName));
            Assert.assertTrue(dbManager.read(I18_TABLE, COLUMN, AUTHOR_NAME_EN));
            Assert.assertFalse(dbManager.read(I18_TABLE, COLUMN, AUTHOR_NAME));
        });
    }

    @Test(expected = DaoException.class) // because of existed book with such author
    public void testDeleteShouldThrowException() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);

            authorDao.delete(ID);

            Assert.assertFalse(dbManager.read(TABLE, COLUMN, AUTHOR_NAME));
            Assert.assertFalse(dbManager.read(I18_TABLE, COLUMN, AUTHOR_NAME));
        });
    }

    @Test
    public void testDelete() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);

            authorDao.delete(TO_DELETE_ID);

            Assert.assertFalse(dbManager.read(TABLE, COLUMN, TO_DELETE_AUTHOR));
            Assert.assertFalse(dbManager.read(I18_TABLE, COLUMN, TO_DELETE_AUTHOR));
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateShouldThrowException() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author toCreate = new Author.Builder().setName(NEW_AUTHOR).build();
            authorDao.create(toCreate);
        });
    }

    @Test
    public void testFindByBookIDShouldReturnListOfOneAuthor() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            List<Author> authors = authorDao.findByBookID(ID);
            Assert.assertTrue(authors.size() == 1);
        });
    }

    private Author createAuthor(String name, String... i18Names) {
        Author.Builder b = new Author.Builder();

        b.setName(name);
        b.setModified(Calendar.getInstance());

        if (i18Names != null) {
            List<I18AuthorName> names = new ArrayList<>();
            int i = 0;

            for (String n: i18Names) {
                if (i == supportedLangs.length) {
                    break;
                }
                names.add(new I18AuthorName.Builder()
                            .setLang(supportedLangs[i++])
                            .setName(n)
                            .build()
                );
            }
            b.setI18Names(names);
        }

        return b.build();
    }

    @Test
    public void testFindByBookIDShouldReturnListOfOneExactAuthor() throws ServiceException, DaoException, SQLException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            Author expected = createAuthor(AUTHOR_NAME);
            List<Author> authors = authorDao.findByBookID(ID);

            if (authors.size() == 1) {
                Assert.assertEquals(expected, authors.get(0));
            } else {
                Assert.fail("Author size != 1");
            }
        });
    }

    @Test
    public void testFindByPatternShouldReturnOneAuthor() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            List<Author> list = authorDao.findByPattern(AUTHOR_NAME, COLUMN, COLUMN, 5, 1);
            List<Author> expected = new ArrayList<>();

            expected.add(createAuthor(AUTHOR_NAME, AUTHOR_NAME, AUTHOR_NAME_EN));

            Assert.assertEquals(expected, list);
        });
    }

    @Test
    public void testFindByPatternCountShouldReturn1() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            int count = authorDao.findByPatternCount(AUTHOR_NAME, COLUMN);

            Assert.assertEquals(1, count);
        });
    }

    @Test
    public void testFindByPatternShouldReturnEmptyList() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            List<Author> list = authorDao.findByPattern(BAD_INPUT, COLUMN, COLUMN, 5, 1);

            Assert.assertEquals(0, list.size());
        });
    }

    @Test
    public void testFindByPatternCountShouldReturn0() throws ServiceException, SQLException, DaoException {
        dbManager.testWrapper( c -> {
            AuthorDao authorDao = new AuthorDaoImpl(c);
            int count = authorDao.findByPatternCount(AUTHOR_NAME, COLUMN);

            Assert.assertEquals(1, count);
        });
    }
}
