package com.github.vitaliibaranetskyi.library.jstltag;

import com.github.vitaliibaranetskyi.library.entity.impl.Author;
import com.github.vitaliibaranetskyi.library.entity.impl.Lang;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

import static com.github.vitaliibaranetskyi.library.constant.Common.START_MSG;

/**
 * Prints Author name in current language or fallback name, if needed
 */
public class PrintAuthor extends TagSupport {
    private static final Logger logger = LogManager.getLogger(PrintAuthor.class);
    private Author author;
    private Lang lang;
    private boolean fallback = true;

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setLang(Lang lang) {
        this.lang = lang;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    @Override
    public int doStartTag() throws JspException {
        logger.debug(START_MSG);
        logger.trace("author={}", author);

        JspWriter out = pageContext.getOut();
        try {
            out.write(author.getName(lang, fallback));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new JspException(e.getMessage(), e);
        }

        return SKIP_BODY;
    }
}
