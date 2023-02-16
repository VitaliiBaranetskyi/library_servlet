package com.github.vitaliibaranetskyi.library.controller.servlet;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.exception.AjaxException;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.service.command.Command;
import com.github.vitaliibaranetskyi.library.service.command.CommandContext;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Front controller, use Command pattern. It gets user logic by command name, forward request to according Command,
 * gets next page from it, redirect/forward request to that page for command result to be shown to user
 * Post requests automatically supports RPG (except AJAX reading requests)
 */
@WebServlet("/controller")
public class Controller extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(Controller.class);

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        logger.debug("start");
        logger.trace("uri={}, query={}", req.getRequestURI(), req.getQueryString());

        String page;
        try {
            page = proceed(req);
        } catch (AjaxException e) {
            if (e.getNextPage() != null) {
                logger.debug("forward to page={}", e.getNextPage());
                req.getRequestDispatcher(e.getNextPage()).forward(req, resp);
                return;
            }
            responseAjaxError(req, resp, e);
            return;
        }

        logger.debug("redirect to page={}", page);
        resp.sendRedirect(page);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        logger.debug("start");
        logger.trace("uri={},query={}", req.getRequestURI(), req.getQueryString());

        String page;
        try {
            page = proceed(req);
        } catch (AjaxException e) {
            if (e.getNextPage() == null) {
                responseAjaxError(req, resp, e);
                return;
            }
            page = e.getNextPage();
        }

        logger.debug("forward to page={}", page);
        req.getRequestDispatcher(page).forward(req, resp);
    }

    /**
     * Gets command and executes it
     *
     * @param req user request
     * @return page to be shown to user
     * @throws AjaxException in case of AJAX request, which is treated in different way by POST requests
     */
    private String proceed(HttpServletRequest req) throws AjaxException {
        String commandStr = req.getParameter("command");
        logger.trace("commandStr={}, encoding={}", commandStr, req.getCharacterEncoding());

        try {
            Command command = CommandContext.getCommand(commandStr);
            String page = command.execute(req);
            if (page == null) {
                throw new ServiceException("error.no.page.was.returned");
            }
            return page;
        } catch (DaoException | ServiceException e) {
            return redirectToError(e.getMessage(), req);
        }
    }

    private String redirectToError(String msg, HttpServletRequest req) {
        logger.error(msg);
        req.getSession().setAttribute(ServletAttributes.SERVICE_ERROR, msg);
        return Pages.ERROR;
    }

    private void responseAjaxError(HttpServletRequest req, HttpServletResponse resp, AjaxException e)
            throws IOException, ServletException {
        logger.error(e.getMessage());
        logger.trace("{}={}", ServletAttributes.SERVICE_ERROR, e.getMessage());

        resp.setStatus(e.getErrorCode());
        req.setAttribute(ServletAttributes.SERVICE_ERROR, e.getMessage());
        req.getRequestDispatcher(Pages.XML_SIMPLE_OUTPUT).forward(req, resp);
    }
}
