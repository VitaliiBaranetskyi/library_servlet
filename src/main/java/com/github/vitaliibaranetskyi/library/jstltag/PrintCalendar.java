package com.github.vitaliibaranetskyi.library.jstltag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Adds to date specified number of days and outputs the result
 */
public class PrintCalendar extends TagSupport {
    private static final Logger logger = LogManager.getLogger(PrintCalendar.class);

    private String format;
    private Calendar calendar;
    private int addDays;

    public void setFormat(String format) {
        this.format = format;
    }

    public void setCalendar(Calendar calendar) {
        if (calendar != null) {
            this.calendar = Calendar.getInstance();
            this.calendar.setTime(calendar.getTime());
        }
    }

    public void setAddDays(int addDays) {
        this.addDays = addDays;
    }

    @Override
    public int doStartTag() throws JspException {
        logger.trace("init: formatStr={}, calendar={}", format, calendar);

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        JspWriter out = pageContext.getOut();
        if (calendar != null) {
            if (addDays > 0) {
                calendar.add(Calendar.DATE, addDays);
            }
            try {
                out.print(sdf.format(calendar.getTime()));
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new JspException(e.getMessage(), e);
            }
        }

        return SKIP_BODY;
    }
}
