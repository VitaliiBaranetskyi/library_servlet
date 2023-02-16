package com.github.vitaliibaranetskyi.library.jstltag;

import com.github.vitaliibaranetskyi.library.entity.impl.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Outputs username or their email, if name is empty
 */
public class UserName extends TagSupport {
    private static final Logger logger = LogManager.getLogger(UserName.class);
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int doStartTag() throws JspException {
        logger.trace("init: user={}", user);
        JspWriter out = pageContext.getOut();

        String name = user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getEmail();
        try {
            out.print(name);
        } catch(Exception e) {
            logger.error(e.getMessage());
            throw new JspException(e.getMessage(), e);
        }

        return SKIP_BODY;
    }
}
