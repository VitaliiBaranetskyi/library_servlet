package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;

import javax.servlet.ServletContext;

/**
 * If task needs configuration parameters they should be initialized from web.xml. So it should support this interface
 */
public interface PeriodicTask {
    /**
     * Initialize task parameters from application context
     * @param context application context
     * @throws ServiceException in case of errors
     */
    void init(ServletContext context) throws ServiceException;
}
