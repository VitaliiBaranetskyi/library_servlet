package com.github.vitaliibaranetskyi.library.service.command;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.dao.AbstractSuperDao;
import com.github.vitaliibaranetskyi.library.entity.Entity;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.service.validator.SafeRequest;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Class-util, has only static methods by design.
 * All methods must be related to at least two type of entities (like User and Book, for example)
 * Methods will be only called from according &lt;&lt;Entity&gt;&gt;Logics class.
 * That's why no signature limitation is provided and class is package-private
 */
class CommonLogicFunctions {
    private static final Logger logger = LogManager.getLogger(CommonLogicFunctions.class);

    /**
     * Made private intentionally, no instance is needed by design
     */
    private CommonLogicFunctions() {
    }

    /**
     * Find Entities, according DAO of which implements {@link AbstractSuperDao}
     * interface.
     * Implements pagination, so in request current page number and number of entities to be shown are required
     *
     * @param req                 user request
     * @param dao                 entity's dao, must implement {@link AbstractSuperDao}
     * @param reqAttribute        result of search will be saved in request attribute with this name
     * @param searchLinkAttribute in concatenation with common attributes ATTR_SEARCH_LINK form attribute of current
     *                            search link to be saved in session. Used in pagination jsp
     * @param desiredPage         page on which result will be shown
     * @param <E>                 Entity class
     * @return page on which result of request will be shown
     * @throws ServiceException in case needed request attributes are missed
     */
    public static <E extends Entity> String findWithPagination(HttpServletRequest req,
                                                               AbstractSuperDao<E> dao,
                                                               String reqAttribute,
                                                               String searchLinkAttribute,
                                                               String desiredPage) throws ServiceException {

        SafeRequest safeReq = new SafeRequest(req);
        String query = safeReq.get("query").escape().convert();
        String searchBy = safeReq.get("searchBy").notEmpty().escape().convert().toLowerCase();

        String sortBy = safeReq.get("sortBy").notEmpty().escape().convert().toLowerCase();
        int num = safeReq.get("num").notNull().convert(Integer::parseInt);

        int pageNum = 1;
        try {
            pageNum = safeReq.get("page").notEmpty().convert(Integer::parseInt);
        } catch (ServiceException e) {
            logger.trace("page is empty, set it to 1");
        }

        if (num == 0) {
            throw new ServiceException("error.amount.cannot.be.zero");
        }

        logger.trace("query={}, searchBy={}, sortBy={}, num={}, pageNum={}",
                query, searchBy, sortBy, num, pageNum);

        List<E> list = null;
        String page;
        int totalCount = -1;
        HttpSession session = req.getSession();
        try {
            if (pageNum == 1) {
                session.removeAttribute(ServletAttributes.PAGES_NUM);
                totalCount = dao.findByPatternCount(query, searchBy);
                session.setAttribute(ServletAttributes.PAGES_NUM, Math.ceil(1.0 * totalCount / num));
                logger.trace("totalCount={}", totalCount);
            }
            if ((pageNum == 1 && totalCount > 0) || pageNum > 1) {
                list = dao.findByPattern(query, searchBy, sortBy, num, pageNum);
            }
            page = desiredPage;
        } catch (DaoException | ServiceException e) {
            req.setAttribute(ServletAttributes.SERVICE_ERROR, e.getMessage());
            page = Pages.ERROR;
        }

        // books = null if totalCount < 1
        if (list == null || list.isEmpty()) {
            logger.trace("{} not found", reqAttribute);
            req.setAttribute(ServletAttributes.NOT_FOUND, "error.not.found");
        }

        req.setAttribute(reqAttribute, list);
        session.setAttribute(searchLinkAttribute + ServletAttributes.ATTR_SEARCH_LINK, req.getRequestURI()
                + '?' + req.getQueryString().replace("&page=" + pageNum, ""));
        logger.debug("end");
        return page;
    }
}