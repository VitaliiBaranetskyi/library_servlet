package com.github.vitaliibaranetskyi.library.dao.impl.mysql;

import com.github.vitaliibaranetskyi.library.dao.AuthorDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.BaseDao;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Disjoint;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.SearchSortColumn;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.util.Transaction;
import com.github.vitaliibaranetskyi.library.entity.impl.Author;
import com.github.vitaliibaranetskyi.library.entity.impl.I18AuthorName;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.github.vitaliibaranetskyi.library.constant.Common.START_MSG;

/**
 * Author DAO. Produce/consume complete entity of {@link Author} class
 */
public class AuthorDaoImpl implements AuthorDao {
    private static final Logger logger = LogManager.getLogger(AuthorDaoImpl.class);
    private static final SearchSortColumn validColumns = new SearchSortColumn("name");
    private static class I18AuthorNameDaoImpl {
        private static final Logger logger = LogManager.getLogger(I18AuthorNameDaoImpl.class);
        private final BaseDao<I18AuthorName> dao;
        private final LangDaoImpl langDao;

        public I18AuthorNameDaoImpl(Connection conn) {
            this.dao = new BaseDao<>(conn);
            this.langDao = new LangDaoImpl(conn);
        }

        public void create(long authorId, I18AuthorName name) throws DaoException {
            logger.debug(START_MSG);
            logger.trace("authorID={}, i18name={}", authorId, name);
            final String query = "INSERT INTO author_name_i18n (lang_id, name, author_id) VALUES (?, ?, ?)";

            dao.createBound(authorId, name, query, this::fillStatement);
        }

        private int fillStatement(I18AuthorName name, PreparedStatement ps) throws SQLException {
            int i = BaseDao.START;
            if (name.getLang() != null) {
                ps.setLong(i++, name.getLang().getId());
            } else {
                throw new SQLException("getLang() is null");
            }
            ps.setString(i++, name.getName());
            return i;
        }

        public List<I18AuthorName> readByAuthorID(long id) throws DaoException {
            logger.debug(START_MSG);
            logger.trace("id={}", id);
            final String query = "SELECT * FROM author_name_i18n WHERE author_id = " + id;

            return dao.getRecords(query, this::parse);
        }

        private I18AuthorName parse(Connection c, ResultSet rs) throws SQLException, DaoException {
            I18AuthorName.Builder builder = new I18AuthorName.Builder();
            builder.setId(rs.getInt("author_id"));
            long langID = rs.getInt("lang_id");

            builder.setLang(langDao.read(langID));
            builder.setName(rs.getString("name"));
            return builder.build();
        }

        public void updateNamesForAuthor(long authorId, List<I18AuthorName> newList) throws DaoException {
            logger.debug(START_MSG);
            logger.trace("authorId={}, i18names={}", authorId, newList);
            Disjoint<I18AuthorName> disjoint = new Disjoint<>(readByAuthorID(authorId), newList);

            for (I18AuthorName name: disjoint.getToDelete()) {
                delete(authorId, name);
            }

            for (I18AuthorName name: disjoint.getToAdd()) {
                create(authorId, name);
            }
        }

        public void delete(long authorId, I18AuthorName name) throws DaoException {
            logger.debug(START_MSG);
            logger.trace("authorId={}, i18name={}", authorId, name);
            final String query = "DELETE FROM author_name_i18n WHERE lang_id = ? AND author_id = ?";

            dao.deleteBound(name.getLang().getId(), authorId, query);
        }
    }

    private Connection conn;

    /**
     * Way to instantiate class from business logic
     */
    public AuthorDaoImpl() {}
    /**
     * Way to instantiate class from other DAO or test
     */
    public AuthorDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void create(Author author) throws DaoException {
        final String query = "INSERT INTO author VALUES (DEFAULT, ?, ?)";

        Transaction tr = new Transaction(conn);
        tr.transactionWrapper( c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            dao.create(author, query, this::statementFiller);

            I18AuthorNameDaoImpl i18Dao = new I18AuthorNameDaoImpl(c);
            for (I18AuthorName name: author.getI18NamesAsList()) {
                i18Dao.create(author.getId(), name);
            }
        });
    }

    private int statementFiller(Author author, PreparedStatement ps) throws SQLException {
        int i = BaseDao.START;
        ps.setString(i++, author.getName());
        if (author.getModified() != null) {
            ps.setTimestamp(i++, new Timestamp(author.getModified().getTimeInMillis()));
        } else {
            throw new SQLException("modified field is null");
        }
        return i;
    }

    @Override
    public Author read(long id) throws DaoException {
        final String query = "SELECT * FROM author WHERE id = ?";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            List<Author> author = resolveDependencies(c, Collections.singletonList(dao.read(id, query, this::parse)));

            return author.get(0);
        });
    }

    @Override
    public Author read(String name) throws DaoException {
        final String query = "SELECT * FROM author AS a"
                            + " JOIN author_name_i18n AS i18"
                            + "   ON i18.author_id = a.id "
                            + "WHERE i18.name = ?";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            Author author = dao.read(name, query, this::parse);
            if (author == null) {
                return null;
            }

            List<Author> list = resolveDependencies(c, Collections.singletonList(author));
            return list.get(0);
        });
    }

    private Author parse(Connection c, ResultSet rs) throws SQLException {
        logger.trace("parse author...");
        Author.Builder builder = new Author.Builder();
        builder.setId(rs.getInt("id"));
        builder.setName(rs.getString("name"));

        Timestamp sqlTime = rs.getTimestamp("modified");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sqlTime);
        builder.setModified(cal);

        Author author = builder.build();

        logger.trace("parse author result: {}", author);
        return author;
    }

    @Override
    public void update(Author author) throws DaoException {
        final String query = "UPDATE author SET name = ?, modified = ? WHERE id = ?";

        Transaction tr = new Transaction(conn);
        tr.transactionWrapper( c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            dao.update(author, query, (a, ps) -> {
                        int i = statementFiller(a, ps);
                        ps.setLong(i++, a.getId());
                        return i;
            });

            I18AuthorNameDaoImpl i18Dao = new I18AuthorNameDaoImpl(c);
            i18Dao.updateNamesForAuthor(author.getId(), author.getI18NamesAsList());
        });
    }

    @Override
    public void delete(long id) throws DaoException {
        final String query = "DELETE FROM author WHERE id = ?";

        Transaction tr = new Transaction(conn);
        tr.transactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            dao.delete(id, query); // i18n on delete cascade
        });
    }

    @Override
    public List<Author> findByBookID(long id) throws DaoException {
        final String query = "SELECT a.id, a.name, a.modified FROM author AS a\n" +
                "  JOIN book_author AS ba\n" +
                "    ON ba.author_id = a.id\n" +
                "  JOIN book AS b\n" +
                "    ON b.id = ba.book_id\n" +
                " WHERE b.id = ?;  ";

        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);

            return resolveDependencies(c, dao.findById(id, query, this::parse));
        });
    }

    private List<Author> resolveDependencies(Connection c, List<Author> authors) throws DaoException {
        if (authors == null) {
            return new ArrayList<>();
        }

        I18AuthorNameDaoImpl i18Dao = new I18AuthorNameDaoImpl(c);
        for (Author a: authors) {
            if (a == null) {
                continue;
            }

            List<I18AuthorName> i18names = i18Dao.readByAuthorID(a.getId());
            a.setI18Names(i18names);

            String authorPrimaryName = a.getName();
            for (I18AuthorName name: i18names) {
                if (name.getName().equals(authorPrimaryName)) {
                    a.setPrimaryLang(name.getLang());
                    break;
                }
            }
        }

        return authors;
    }

    @Override
    public List<Author> findByPattern(String what, String searchBy, String sortBy, int num, int page)
            throws ServiceException, DaoException {
        validColumns.checkSearch(searchBy);
        validColumns.checkSort(sortBy);

        String query = patternQuery(false, false, true);
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            return resolveDependencies(c, dao.findByPattern(what, num, page, query, this::parse));
        });
    }


    public List<Author> findByPattern(String what) throws DaoException {
        String query = patternQuery(false, false, false);
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            return resolveDependencies(c, dao.findByPattern(what, query, this::parse));
        });
    }

    @Override
    public int findByPatternCount(String what, String searchBy) throws ServiceException, DaoException {
        validColumns.checkSearch(searchBy);

        final String query = patternQuery(true, false, false);
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            return dao.count(what, query);
        });
    }

    @Override
    public List<Author> findBy(String what, String searchBy) throws ServiceException, DaoException {
        validColumns.checkSearch(searchBy);

        final String query = patternQuery(false, true, false);
        Transaction tr = new Transaction(conn);
        return tr.noTransactionWrapper(c -> {
            BaseDao<Author> dao = new BaseDao<>(c);
            List<Author> authors = dao.findByString(what, query, this::parse);
            return resolveDependencies(c, authors);
        });
    }

    private String patternQuery(boolean count, boolean exactSearch, boolean limit) {
        final String what = count ? "COUNT(*)" : "*";
        final String operator = exactSearch ? " = ?" : " LIKE ?";
        String query = "SELECT " + what + " FROM author "
                     + " WHERE id IN ("
                                        + "SELECT a.id FROM author AS a"
                                        + "  JOIN author_name_i18n AS i18"
                                        + "    ON i18.author_id = a.id"
                                        + " WHERE i18.name " + operator
                                        + " ORDER BY i18.name"
                     + ")";
        if (limit) {
            query += " LIMIT ? OFFSET ?";
        }

        return query;
    }
}
