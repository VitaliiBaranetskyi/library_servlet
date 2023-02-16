package com.github.vitaliibaranetskyi.library.service.command;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.dao.AuthorDao;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryCreator;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.entity.impl.Author;
import com.github.vitaliibaranetskyi.library.entity.impl.I18AuthorName;
import com.github.vitaliibaranetskyi.library.entity.impl.Lang;
import com.github.vitaliibaranetskyi.library.exception.AjaxException;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.service.validator.SafeRequest;
import com.github.vitaliibaranetskyi.library.service.validator.SafeSession;
import com.github.vitaliibaranetskyi.library.constant.Common;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import com.github.vitaliibaranetskyi.library.controller.servlet.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.List;

/**
 * Class-util, has only static methods by design. All methods must be related to Author entity, like find author, etc.
 * All public methods here must comply with {@link Command} signature, as
 * they will be used in CommandContext as lambda-functions and called from Front Controller
 * {@link Controller}
 */
public class AuthorLogic {
    private static final Logger logger = LogManager.getLogger(AuthorLogic.class);
    private static final DaoFactoryImpl daoFactory = DaoFactoryCreator.getDefaultFactory().newInstance();
    private static final String ATTR_AUTHOR_SEARCH_LINK = "author" + ServletAttributes.ATTR_SEARCH_LINK;

    /**
     * Made private intentionally, no instance is needed by design
     */
    private AuthorLogic() {
    }

    @SuppressWarnings("unchecked")
    private static Author getValidParams(HttpServletRequest req) throws ServiceException {
        logger.debug(Common.START_MSG);

        SafeRequest safeReq = new SafeRequest(req);
        String primaryLangCode = safeReq.get(ServletAttributes.ATTR_PRIMARY_LANG).notEmpty().convert();
        logger.trace("primaryLangCode={}", primaryLangCode);

        List<Lang> supportedLangs = (List<Lang>) req.getServletContext().getAttribute(ServletAttributes.SUPPORTED_LANGUAGES);
        if (supportedLangs == null) {
            throw new ServiceException("error.supported.langs.not.found");
        }

        Author.Builder authorBuilder = new Author.Builder();
        String primaryName = null;
        Lang primaryLang = null;
        for (Lang lang : supportedLangs) {
            if (lang.getCode().equals(primaryLangCode)) {
                primaryLang = lang;
            }

            String i18name = safeReq.get(lang.getCode()).convert();
            if (i18name.equals("")) {
                continue;
            }

            logger.trace("i18name={}, lang={}", i18name, lang);
            authorBuilder.addI18Name(lang, i18name);

            if (lang.getCode().equals(primaryLangCode)) {
                primaryName = i18name;
                logger.trace("found primaryName: {}, primaryLang={}", i18name, primaryLang);
            }
        }
        authorBuilder.setModified(Calendar.getInstance());
        Author author = authorBuilder.build();

        // by this we preserve user edition in case of error
        req.getSession().setAttribute(ServletAttributes.ATTR_SAVED_USER_INPUT, author);

        if (primaryName == null) {
            throw new ServiceException("error.primary.name.is.empty");
        }

        if (primaryLang == null) {
            throw new ServiceException("error.primary.lang.not.found");
        }
        author.setName(primaryName);
        author.setPrimaryLang(primaryLang);

        logger.trace("got author params={}", author);
        logger.debug(Common.END_MSG);

        req.getSession().removeAttribute(ServletAttributes.ATTR_SAVED_USER_INPUT);
        logger.debug(Common.END_MSG);
        return author;
    }

    /**
     * Adds a new author. Preserves user input, shows error in place of editing.
     *
     * @param req HttpServletRequest
     * @return page to be shown to user
     * @throws ServiceException in case of programmer or DB error
     */
    public static String add(HttpServletRequest req) throws ServiceException {
        logger.debug(Common.START_MSG);
        String errorPage = Pages.AUTHOR_EDIT + "?command=author.add";

        try {
            Author author = getValidParams(req);
            AuthorDao dao = daoFactory.getAuthorDao();
            //preserve user editing
            req.getSession().setAttribute(ServletAttributes.ATTR_SAVED_USER_INPUT, author);
            checkExistenceInDb(author, dao);
            dao.create(author);
            req.getSession().removeAttribute(ServletAttributes.ATTR_SAVED_USER_INPUT);
        } catch (ServiceException | DaoException e) {
            errorPageLogic(req, e);
            return errorPage;
        }

        logger.debug(Common.END_MSG);
        return nextPageLogic(req);
    }

    private static void checkExistenceInDb(Author author, AuthorDao dao) throws ServiceException, DaoException {
        Author existed = dao.read(author.getName());
        if (existed != null) {
            throw new ServiceException("error.such.author.name.exists",
                    author.getName());
        }

        for (I18AuthorName i18name: author.getI18NamesAsList()) {
            String nameTranslation = i18name.getName();
            existed = dao.read(nameTranslation);
            if (existed != null && existed.getName(i18name.getLang()).equals(nameTranslation)) {
                throw new ServiceException("error.such.author.name.exists",
                        i18name.getName());
            }
        }
    }

    private static void errorPageLogic(HttpServletRequest req, Exception e) {
        logger.debug(e.getMessage());

        req.getSession().setAttribute(ServletAttributes.USER_ERROR, e.getMessage());
        if (e instanceof ServiceException) {
            req.getSession().setAttribute(ServletAttributes.USER_ERROR_PARAMS, ((ServiceException) e).getMsgParameters());
        }
    }

    /**
     * Updates existed author. Preserves user input, shows user input errors in place of editing.
     *
     * @param req user request
     * @return page to be shown to user
     * @throws ServiceException in case of programmer error
     * @throws DaoException in case of programmer/db error
     */
    public static String edit(HttpServletRequest req) throws ServiceException, DaoException {
        logger.debug(Common.START_MSG);

        try {
            return findByIDAndPutInSession(req);
        } catch (ServiceException e) {
            logger.debug("id is not in the request, proceed with author editing");
            return updateAuthorFoundInSession(req);
        } finally {
            logger.debug(Common.END_MSG);
        }
    }

    private static String updateAuthorFoundInSession(HttpServletRequest req) throws ServiceException {
        logger.debug(Common.START_MSG);
        final String userErrorPage = Pages.AUTHOR_EDIT + "?command=author.edit";

        HttpSession session = req.getSession();
        SafeSession safeSession = new SafeSession(session);
        Author oldAuthorVersion = safeSession
                .get(ServletAttributes.ATTR_PROCEED_AUTHOR)
                .notNull()
                .convert(Author.class::cast);

        try {
            Author updatedAuthorVersion = getValidParams(req);
            updatedAuthorVersion.setId(oldAuthorVersion.getId());
            // to preserve user edition in case of error throwing in code below
            session.setAttribute(ServletAttributes.ATTR_PROCEED_AUTHOR, updatedAuthorVersion);

            AuthorDao dao = daoFactory.getAuthorDao();
            dao.update(updatedAuthorVersion);
        } catch (ServiceException | DaoException e) {
            errorPageLogic(req, e);
            return userErrorPage;
        }

        session.removeAttribute(ServletAttributes.ATTR_PROCEED_AUTHOR);
        logger.debug(Common.END_MSG);
        return nextPageLogic(req);
    }

    private static String findByIDAndPutInSession(HttpServletRequest req) throws ServiceException, DaoException {
        logger.debug(Common.START_MSG);
        SafeRequest safeRequest = new SafeRequest(req);

        long id = safeRequest
                .get(ServletAttributes.JSP_FORM_ATTR_ID)
                .notNull()
                .convert(Long::parseLong);

        AuthorDao dao = daoFactory.getAuthorDao();
        Author author = dao.read(id);
        req.getSession().setAttribute(ServletAttributes.ATTR_PROCEED_AUTHOR, author);

        logger.trace("set {} to {}", ServletAttributes.ATTR_PROCEED_AUTHOR, author);
        logger.debug(Common.END_MSG);
        return Pages.AUTHOR_EDIT;
    }
    /**
     * Deletes author.
     *
     * @param req user request
     * @return page to be shown to user
     * @throws ServiceException in case of programmer error
     * @throws DaoException in case of programmer/db error
     */
    public static String delete(HttpServletRequest req) throws ServiceException, DaoException {
        logger.debug(Common.START_MSG);

        SafeRequest safeRequest = new SafeRequest(req);

        long id = safeRequest.get(ServletAttributes.JSP_FORM_ATTR_ID).notNull().convert(Long::parseLong);
        AuthorDao dao = daoFactory.getAuthorDao();
        dao.delete(id);

        logger.debug(Common.END_MSG);
        return nextPageLogic(req);
    }

    /**
     * Find author according to pattern with pagination.
     *
     * @param req user request
     * @return page to be shown to user
     * @throws ServiceException in case of programmer error
     */
    public static String find(HttpServletRequest req) throws ServiceException {
        logger.debug(Common.START_MSG);

        return CommonLogicFunctions.findWithPagination(req, daoFactory.getAuthorDao(),
                ServletAttributes.ATTR_AUTHORS, "author", Pages.AUTHORS);
    }

    /**
     * Find author according to pattern without pagination.
     *
     * @param req user request
     * @return page to be shown to user
     * @throws ServiceException in case of programmer error
     */
    public static String findAll(HttpServletRequest req) throws ServiceException, AjaxException {
        logger.debug(Common.START_MSG);

        SafeRequest safeReq = new SafeRequest(req);
        String query = safeReq.get("query").escape().convert();
        String searchBy = safeReq.get("searchBy").notEmpty().escape().convert().toLowerCase();

        logger.trace("query={}, searchBy={}", query, searchBy);
        List<Author> list;
        try {
            list = daoFactory.getAuthorDao().findByPattern(query);
        } catch (DaoException e) {
            throw new AjaxException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

        if (list.isEmpty()) {
            throw new AjaxException(HttpServletResponse.SC_NOT_FOUND, "error.not.found");
        }

        req.setAttribute(ServletAttributes.ATTR_AUTHORS, list);
        logger.debug(Common.END_MSG);
        throw new AjaxException(Pages.XML_AUTHOR);
    }

    private static String nextPageLogic(HttpServletRequest req) throws ServiceException {
        SafeSession safeSession = new SafeSession(req.getSession());

        String page = safeSession.get(ATTR_AUTHOR_SEARCH_LINK).convert(String.class::cast);
        if (page == null) {
            logger.trace("no previous search link in session");
            page = Pages.AUTHORS;
        }

        return page;
    }
}