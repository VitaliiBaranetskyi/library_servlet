package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import com.github.vitaliibaranetskyi.library.dao.impl.mysql.func.EntityParser;
import com.github.vitaliibaranetskyi.library.dao.impl.mysql.func.StatementFiller;
import com.github.vitaliibaranetskyi.library.entity.Entity;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.github.vitaliibaranetskyi.library.constant.Common.*;

/**
 * Common low level MySQL functions to be used by all DAO
 * @param <T> Entity with which instance will be working
 */
public class BaseDao<T extends Entity> {
    private static final Logger logger = LogManager.getLogger(BaseDao.class);
    public static final int START = 1;
    public static final String PATTERN_QUERY_LOG = "pattern={}, query={}";
    private final Connection conn;

    public BaseDao(Connection conn) {
        this.conn = conn;
        logger.trace("conn={}", conn);
    }

    private void logAndThrow(SQLException e) throws DaoException {
        logger.error(e.getMessage());
        throw new DaoException(e.getMessage(), e);
    }

    public static String escapeForLike(String param) {
        logger.debug(START_MSG);

        String result = param.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
        logger.debug(END_MSG);
        return "%" + result + "%";
    }

    /**
     * Creates entity of given type in DB
     *
     * @param entity instance of entity to be created
     * @param query SQL query to create that instance
     * @param filler fills statements of such entity types
     * @throws DaoException in case of error
     */
    public void create(T entity, String query, StatementFiller<T> filler) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("entity={}, query={}", entity, query);

        try(PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            filler.accept(entity, ps);
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        entity.setId(rs.getLong(START));
                    }
                }
                logger.info("New entity added: id={}", entity.getId());
            } else {
                logger.info(NO_UPDATE);
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    /**
     * Reads entity of given type from DB.
     *
     * @param id id of entity to be read
     * @param query SQL query for reading
     * @param parser parser for result set which will be returned
     * @return entity of given type
     * @throws DaoException in case of error
     */
    public T read(long id, String query, EntityParser<T> parser) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, query={}", id, query);

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(START, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parser.accept(conn, rs);
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
        return null;
    }

    /**
     * Counts number of rows for given SQL request with LIKE expression.
     *
     * @param pattern to be looked up
     * @param query SQL query using 'LIKE ?'
     * @return number of rows matched the query
     * @throws DaoException in case of error
     */
    public int count(String pattern, String query) throws DaoException {
        logger.debug(START_MSG);
        logger.trace(PATTERN_QUERY_LOG, pattern, query);

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(START, escapeForLike(pattern));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(START);
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
        return -1;
    }

    /**
     * Updates entity of given type in DB.
     *
     * @param entity to be updated
     * @param query SQL request
     * @param filler fills request with entity data
     * @throws DaoException in case of error
     */
    public void update(T entity, String query, StatementFiller<T> filler) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("entity={}, query={}", entity, query);
        try(PreparedStatement ps = conn.prepareStatement(query)) {
            filler.accept(entity, ps);
            if (ps.executeUpdate() > 0) {
                logger.info("Successful update: id={}", entity.getId());
            } else {
                logger.info(NO_UPDATE);
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    /**
     * Deletes from DB entity of given type with such id
     *
     * @param id  id of entity, which will be deleted
     * @param query SQL deleting query
     * @throws DaoException in case of error
     */
    public void delete(long id, String query) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, query={}", id, query);
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(START, id);
            if (ps.executeUpdate() > 0) {
                logger.info("Successful deleting: id={}", id);
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    public void deleteBound(long id1, long id2, String query) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id1={}, id2={}, query={}", id1, id2, query);

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            int i = START;
            ps.setLong(i++, id1);
            ps.setLong(i, id2);
            if (ps.executeUpdate() > 0) {
                logger.info(SUCCESS);
            } else {
                logger.info(NO_UPDATE);
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    /**
     * Bounds 2 entities in many-to-many relationship table
     *
     * @param id id of entity 1 (any subclass of Entity)
     * @param entity entity of given type
     * @param query SQL query
     * @param filler fills statement with info from entity of given type
     * @throws DaoException in case of errors
     */
    public void createBound(long id, T entity, String query, StatementFiller<T> filler) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, entity={}, query={}", id, entity, query);

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            int i = filler.accept(entity, ps);
            ps.setLong(i, id);
            if (ps.executeUpdate() > 0) {
                logger.info(SUCCESS);
            } else {
                logger.info(NO_UPDATE);
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    /**
     * Finds all entities of given type by pattern (LIKE ? expression) with limit and offset.
     * NB! Page should start from 1
     *
     * @param pattern pattern for looking up
     * @param num limit amount
     * @param page offset amount equal (page-1)*limit
     * @param query SQL lookup query
     * @param parser parses Result set to Entity of given type
     * @return list of entities of given type
     * @throws DaoException in case of errors
     */
    public List<T> findByPattern(String pattern, int num, int page, String query, EntityParser<T> parser)
            throws DaoException {
        logger.debug(START_MSG);
        logger.trace("pattern={}, query={}, num={}, page={}",
                 pattern, query, num, page);
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            int i = START;
            ps.setString(i++, escapeForLike(pattern));
            ps.setInt(i++, num);
            ps.setInt(i, (page - 1) * num);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(parser.accept(conn, rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return list;
    }

    /**
     * Finds all entities of given type by pattern (LIKE ? expression). No limit and offset are used.
     *
     * @param pattern pattern for looking up
     * @param query SQL lookup query
     * @param parser parses Result set to Entity of given type
     * @return list of entities of given type
     * @throws DaoException in case of errors
     */
    public List<T> findByPattern(String pattern, String query, EntityParser<T> parser)
            throws DaoException {
        logger.debug(START_MSG);
        logger.trace(PATTERN_QUERY_LOG,
                pattern, query);

        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(START, escapeForLike(pattern));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(parser.accept(conn, rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return list;
    }

    /**
     * Finds all entities of given type by equals (= ?) expressions. No limit and offset are used.
     *
     * @param pattern pattern for looking up
     * @param query SQL lookup query
     * @param parser parses Result set to Entity of given type
     * @return list of entities of given type
     * @throws DaoException in case of errors
     */
    public List<T> findByString(String pattern, String query, EntityParser<T> parser)
            throws DaoException {
        logger.debug(START_MSG);
        logger.trace(PATTERN_QUERY_LOG,
                pattern, query);

        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(START, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(parser.accept(conn, rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return list;
    }

    /**
     * Updates bound for 2 entities in many-to-many relationship table.
     *
     * @param id1 id of first entity
     * @param id2 id of second entity
     * @param query SQL query
     * @throws DaoException in case of error
     */
    public void updateBound(long id1, long id2, String query) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id1={}, id2={}, query={}", id1, id2, query);

        try(PreparedStatement ps = conn.prepareStatement(query)) {
            int i = START;
            ps.setLong(i++, id1);
            ps.setLong(i, id2);
            if (ps.executeUpdate() > 0) {
                logger.info("success");
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }
    }

    /**
     * Gets all entities of given type by non-parametrised query.
     *
     * @param query SQL query
     * @param parser parses result set to entity of given type
     * @return list of entities
     * @throws DaoException in case of error
     */
    public List<T> getRecords(String query, EntityParser<T> parser) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("query={}", query);

        List<T> list = new ArrayList<>();
        try (Statement ps = conn.createStatement()) {
            try (ResultSet rs = ps.executeQuery(query)) {
                while (rs.next()) {
                    list.add(parser.accept(conn, rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return list;
    }

    /**
     * Read entity by unique string identifier.
     *
     * @param lookUp string identifier for entity, unique in table
     * @param query SQL query
     * @param parser parses result set to entity of given type
     * @return entity of given type
     * @throws DaoException in case of error
     */
    public T read(String lookUp, String query, EntityParser<T> parser) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("key={}, {}", lookUp, query);

        T result = null;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(START, lookUp);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = parser.accept(conn, rs);
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return result;
    }

    public List<T> findById(long id, String query, EntityParser<T> parser) throws DaoException {
        logger.debug(START_MSG);
        logger.trace("id={}, {}", id, query);

        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setLong(START, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(parser.accept(conn, rs));
                }
            }
        } catch (SQLException e) {
            logAndThrow(e);
        } finally {
            logger.debug(END_MSG);
        }

        return list;
    }
}
