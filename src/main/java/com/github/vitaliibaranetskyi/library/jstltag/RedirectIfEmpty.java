package com.github.vitaliibaranetskyi.library.jstltag;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.List;

/**
 * Redirect to error page if something is empty
 */
public class RedirectIfEmpty extends TagSupport {
    private static final Logger logger = LogManager.getLogger(RedirectIfEmpty.class);
    private static final String error = ServletAttributes.SERVICE_ERROR;
    private String errorMsg;
    private Object value; // String or List

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public int doStartTag() throws JspException {
        logger.trace("init: value={}, errorMsg={}", value, errorMsg);

        HttpServletResponse resp = (HttpServletResponse) pageContext.getResponse();
        // this prevents multiple redirection while using tag multiple times on a page
        if (resp.isCommitted()) {
            return SKIP_BODY;
        }

        if (value == null) {
            return redirect(resp);
        }

        if (value instanceof String && value.equals("")) {
            return redirect(resp);
        }

        if (value instanceof List && ((List) value).isEmpty()) {
            return redirect(resp);
        }

        logger.trace("finish: value passed");
        return SKIP_BODY;
    }

    private int redirect(HttpServletResponse resp) throws JspException {
        HttpSession session = pageContext.getSession();
        session.setAttribute(ServletAttributes.SERVICE_ERROR, errorMsg);
        try {
            resp.sendRedirect(Pages.ERROR);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new JspException(e.getMessage(), e);
        }

        return SKIP_BODY;
    }
}
