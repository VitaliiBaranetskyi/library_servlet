package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.UserDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.SearchSortColumn;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Calendar;
import java.util.List;

/**
 * User DAO. Produce/consume complete entity of {@link User} class
 */
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);
    private static final SearchSortColumn validColumns =
            new SearchSortColumn("email", "name", "role", "state");
    private Connection conn;

    /**
     * Way to instantiate class from business logic
     */
    public UserDaoImpl() {}

    /**
     * Way to instantiate class from other DAO or test
     */
    public UserDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public User findByEmail(String email) throws DaoException {
        final String query = "SELECT * FROM user WHERE email = ?";
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            return dao.read(email, query, this::parse);
        });
    }

    @Override
    public List<User> getAll() throws DaoException {
        final String query = "SELECT * FROM user WHERE state != 'DELETED'";
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            return dao.getRecords(query, this::parse);
        });
    }

    @Override
    public void create(User user) throws DaoException {
        final String query = "INSERT INTO user VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Transaction tr = new Transaction(conn);
        tr.transactionWrapper( c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            dao.create(user, query, this::fillStatement);
        });
    }

    private int fillStatement(User user, PreparedStatement ps) throws SQLException {
        int i = BaseDao.START;
        ps.setString(i++, user.getEmail());
        ps.setString(i++, user.getPassword());
        ps.setString(i++, user.getSalt());
        ps.setInt(i++, user.getRole().ordinal());
        ps.setInt(i++, user.getState().ordinal());
        ps.setDouble(i++, user.getFine());
        ps.setString(i++, user.getName());

        if (user.getPreferredLang() == null) {
            throw new SQLException("Preferred lang is null");
        }
        ps.setLong(i++, user.getPreferredLang().getId());


        if (user.getModified() == null) {
            throw new SQLException("Modified time is null");
        }
        ps.setTimestamp(i++, new Timestamp(user.getModified().getTimeInMillis()));


        if (user.getFineLastChecked() == null) {
            throw new SQLException("fineLastChecked time is null");
        }
        ps.setTimestamp(i++, new Timestamp(user.getFineLastChecked().getTimeInMillis()));

        return i;
    }

    @Override
    public User read(long id) throws DaoException {
        final String query = "SELECT * FROM user WHERE id = ?";
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper( c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            return dao.read(id, query, this::parse);
        });
    }

    private User parse(Connection c, ResultSet rs) throws SQLException, DaoException {
        User.Builder builder = new User.Builder();
        builder.setId(rs.getInt("id"));
        builder.setEmail(rs.getString("email"));
        builder.setName(rs.getString("name"));
        builder.setPassword(rs.getString("password"));
        builder.setSalt(rs.getString("salt"));
        String dbRole = rs.getString("role");
        builder.setRole(User.Role.valueOf(dbRole));
        String dbState = rs.getString("state");
        builder.setState(User.State.valueOf(dbState));
        builder.setFine(rs.getDouble("fine"));
        builder.setFineLastChecked(getCalendar(rs, "fine_last_checked"));
        builder.setModified(getCalendar(rs, "modified"));

        // get dependencies
        long langID = rs.getInt("preferred_lang_id");
        LangDaoImpl langDao = new LangDaoImpl(c);
        builder.setPreferredLang(langDao.read(langID));

        return builder.build();
    }

    private Calendar getCalendar(ResultSet rs, String column) throws SQLException {
        Timestamp sqlTimestamp = rs.getTimestamp(column);
        Calendar cal = Calendar.getInstance();
        cal.setTime(sqlTimestamp);
        return cal;
    }

    @Override
    public void update(User user) throws DaoException {
        final String query = "UPDATE user SET email = ?, password = ?, salt = ?, role = ?, state = ?, " +
                "fine = ?, name = ?, preferred_lang_id = ?, modified = ?, fine_last_checked = ? WHERE id = ?";
        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            dao.update(user, query,
                    (entity, ps) -> {
                        int nextIndex = fillStatement(entity, ps);
                        ps.setLong(nextIndex++, entity.getId());
                        return nextIndex;
                    }
            );
        });
    }

    @Override
    public void delete(long id) throws DaoException {
        final String query = "UPDATE user SET state = 'deleted' WHERE id = ?";
        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            dao.delete(id, query);
        });
    }

    @Override
    public List<User> findByPattern(String what, String searchBy, String sortBy, int num, int page)
            throws ServiceException, DaoException {

        validColumns.checkSearch(searchBy);
        validColumns.checkSort(sortBy);

        final String query = "SELECT * FROM user WHERE " + searchBy + " LIKE ? AND state != 'DELETED' ORDER BY " + sortBy + " LIMIT ? OFFSET ?";
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            return dao.findByPattern(what, num, page, query, this::parse);
        });
    }

    @Override
    public int findByPatternCount(String what, String searchBy) throws ServiceException, DaoException {
        logger.debug("start");
        validColumns.checkSearch(searchBy);
        final String query = "SELECT COUNT(*) FROM user WHERE " + searchBy + " LIKE ? AND state != 'DELETED'";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
           BaseDao<User> dao = new BaseDao<>(c);
           return dao.count(what, query);
        });
    }


    @Override
    public List<User> findBy(String what, String searchBy) throws ServiceException, DaoException {
        validColumns.checkSearch(searchBy);

        final String query = "SELECT * FROM user WHERE " + searchBy + " = ? AND state != 'DELETED'";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<User> dao = new BaseDao<>(c);
            return dao.findByPattern(what, query, this::parse);
        });
    }
}