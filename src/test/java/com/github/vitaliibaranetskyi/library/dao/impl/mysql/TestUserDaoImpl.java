package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.UserDao;
import com.github.vitaliibaranetskyi.library.entity.impl.Lang;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.testutil.DBManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TestUserDaoImpl {
    private static final DBManager dbManager = DBManager.getInstance();
    public static final String USER_TABLE = "user";
    public static final String EMAIL_COLUMN = "email";
    public static final String NAME_COLUMN = "name";
    public static final long ID = 1;
    public static final String COMMON_EMAIL_PATTERN = "gmail.com";
    private static final int USERS_IN_DB = 4;
    private static final String BAD_INPUT = "aksjdflkjba jdfWER";

    private static User newUser;
    private static User existingUser;

    @BeforeClass
    public static void initTest() {
        final String pass = "123";
        final String salt = "123";
        final String newEmail = "test@test.com";
        final String existingEmail = "admin@gmail.com";
        User.Builder builder = new User.Builder();

        builder.setEmail(newEmail);
        builder.setPassword(pass);
        builder.setSalt(salt);
        builder.setPreferredLang(new Lang.Builder().setCode("en").setId(1).build());
        builder.setModified(Calendar.getInstance());
        builder.setFineLastChecked(Calendar.getInstance());
        newUser = builder.build();

        builder.setEmail(existingEmail);
        builder.setId(ID);
        existingUser = builder.build();
    }

    @Before
    public void init() throws IOException, InterruptedException, ServiceException {
        dbManager.executeScript();
    }

    @Test(expected = DaoException.class)
    public void testCreateShouldThrowExceptionPreferredLangIsNull() throws SQLException, ServiceException, DaoException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            User badUser = new User.Builder().build();

            userDao.create(badUser);
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateShouldThrowExceptionModifiedIsNull() throws SQLException, ServiceException, DaoException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            User badUser = new User.Builder().build();

            badUser.setPreferredLang(newUser.getPreferredLang());
            userDao.create(badUser);
        });
    }

    @Test(expected = DaoException.class)
    public void testCreateShouldThrowExceptionEmailIsNull() throws SQLException, ServiceException, DaoException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            User badUser = new User.Builder().build();

            badUser.setPreferredLang(newUser.getPreferredLang());
            badUser.setModified(Calendar.getInstance());
            userDao.create(badUser);
        });
    }
    
    @Test
    public void testCreateShouldCreateNewUserInDB() throws DaoException, SQLException, ServiceException {
        dbManager.testWrapper(c -> {
            UserDao userDao = new UserDaoImpl(c);

            userDao.create(newUser);
            Assert.assertTrue(dbManager.read(USER_TABLE, EMAIL_COLUMN, newUser.getEmail()));
        });
    }

    @Test
    public void testCreateShouldSetUserID() throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);

            userDao.create(newUser);
            Assert.assertNotEquals(-1, newUser.getId());
        });
    }

    @Test
    public void testUpdateUserName() throws DaoException, SQLException, ServiceException {
        final String name = "test";

        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);

            //userDao.create(user);
            existingUser.setName(name);
            userDao.update(existingUser);
        });
        Assert.assertEquals(name,
                dbManager.readField(NAME_COLUMN, USER_TABLE, EMAIL_COLUMN, existingUser.getEmail()));
    }

    @Test
    public void testReadUserShouldReturnExistingUser() throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            //dbManager.execute(conn, CREATE_USER);
            User test = userDao.read(ID);

            Assert.assertEquals(test, existingUser);
        });
    }

    @Test
    public void testReadUserShouldReturnNothing() throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            //dbManager.execute(conn, CREATE_USER);
            User test = userDao.read(999);

            Assert.assertNull(test);
        });
    }

    @Test
    public void testDeleteUser() throws DaoException, SQLException, ServiceException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);

            userDao.delete(existingUser.getId());
            Assert.assertFalse(dbManager.read(USER_TABLE, EMAIL_COLUMN, newUser.getEmail()));
        });
    }

    @Test
    public void testFindByEmailShouldReturnExistingUser() throws DaoException, SQLException, ServiceException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);

            User test = userDao.findByEmail(existingUser.getEmail());
            Assert.assertEquals(existingUser, test);
        });
    }

    @Test
    public void testFindByPatternShouldReturnOneExistingUser() throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            List<User> testList =
                    userDao.findByPattern(existingUser.getEmail(), "email", "email", 5, 1);
            List<User> users = new ArrayList<>();

            users.add(existingUser);
            Assert.assertEquals(users, testList);
        });
    }

    @Test
    public void testFindByPatternCountShouldReturn4()
            throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            int count =
                    userDao.findByPatternCount(COMMON_EMAIL_PATTERN, "email");
            Assert.assertEquals(USERS_IN_DB, count);
        });
    }

    @Test
    public void testFindByPatternCountShouldReturn0()
            throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            int count =
                    userDao.findByPatternCount(BAD_INPUT, EMAIL_COLUMN);
            Assert.assertEquals(0, count);
        });
    }

    @Test
    public void testFindByPatternShouldReturnOneExistingUserWithNumLimitAndSortByConstraints()
            throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            List<User> testList =
                    userDao.findByPattern(COMMON_EMAIL_PATTERN, EMAIL_COLUMN, EMAIL_COLUMN, 1, 1);
            List<User> users = new ArrayList<>();

            users.add(existingUser);
            Assert.assertEquals(users, testList);
        });
    }

    @Test(expected = ServiceException.class)
    public void testFindByPatternWithWrongSearchColumn()
            throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            userDao.findByPattern(COMMON_EMAIL_PATTERN,
                            BAD_INPUT, EMAIL_COLUMN, 1, 0);
        });
    }

    @Test(expected = ServiceException.class)
    public void testFindByPatternWithWrongSortColumn()
            throws DaoException, ServiceException, SQLException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);
            userDao.findByPattern(COMMON_EMAIL_PATTERN,
                    EMAIL_COLUMN, BAD_INPUT, 1, 1);
        });
    }

    @Test
    public void testGetAllShouldReturnUsersCount() throws DaoException, SQLException, ServiceException {
        dbManager.testWrapper( c -> {
            UserDao userDao = new UserDaoImpl(c);

            List<User> users = userDao.getAll();
            Assert.assertEquals(USERS_IN_DB,users.size());
        });
    }
}