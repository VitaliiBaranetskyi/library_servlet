package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows keeping valid columns for search and sort operation to particular entity dao
 */
public class SearchSortColumn {
    private static final Logger logger = LogManager.getLogger(SearchSortColumn.class);
    private static final String SEARCH = "Search";
    private static final String SORT = "Sort";

    private final Set<String> columns = new HashSet<>();

    /**
     * @param columns list of columns allowed for search and sort
     */
    public SearchSortColumn(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
    }

    private void check(String s, String action) throws ServiceException {
        if (!columns.contains(s)) {
            logger.error("{} forbidden for column {}", action, s);
            throw new ServiceException(action + " forbidden for column " + s);
        }
    }

    /**
     * Checks string if it's allowed to be sorted on
     *
     * @param s checked string
     * @throws ServiceException in case search/sort is forbidden
     */
    public void checkSort(String s) throws ServiceException {
        check(s, SORT);
    }

    /**
     * Checks string if it's allowed to be searched on
     *
     * @param s checked string
     * @throws ServiceException in case search/sort is forbidden
     */
    public void checkSearch(String s) throws ServiceException {
        check(s, SEARCH);
    }
}
