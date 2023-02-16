package com.github.vitaliibaranetskyi.library.controller.filter;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.service.command.CommandContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static com.github.vitaliibaranetskyi.library.constant.ServletAttributes.SERVICE_ERROR;

/**
 * Checks if current user is authorized to visit page or do command
 */
public class AuthFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(AuthFilter.class);

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        logger.debug("starts");

        try {
            if (isAllowed(req)) {
                logger.debug("request is allowed");
                chain.doFilter(req, resp);
            } else {
                logger.debug("forward to login page={}", Pages.LOGIN);
                req.getRequestDispatcher(Pages.LOGIN).forward(req, resp);
            }
        } catch (ServiceException e) {
            logger.error(e.getMessage());
            req.setAttribute(SERVICE_ERROR, e.getMessage());
            req.getRequestDispatcher(Pages.ERROR).forward(req, resp);
        }
        logger.debug("finished");
    }

    private boolean isAllowed(ServletRequest req) throws ServiceException {
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        logger.trace("will check request uri={}", httpRequest.getRequestURI());

        HttpSession session = httpRequest.getSession(false);

        User user = null;
        if (session != null) {
            user = (User) session.getAttribute("user");
        }

        User.Role role = User.Role.UNKNOWN;
        if (user != null) {
            role = user.getRole();
        }

        String command = req.getParameter(ServletAttributes.COMMAND);
        if (command == null) {
            command = ((HttpServletRequest) req).getRequestURI();
        }

        boolean authContext = CommandContext.isAllowed(command, role);
        if (authContext) {
            return true;
        }

        if (role == User.Role.UNKNOWN) {
            return false;
        }

        throw new ServiceException("error.resource.forbidden");
    }
}