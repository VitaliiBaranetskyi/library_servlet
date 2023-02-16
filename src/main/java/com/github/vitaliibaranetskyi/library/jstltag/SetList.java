package com.github.vitaliibaranetskyi.library.jstltag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.jsp.tagext.TagSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create list out of string, split by spaces, and saves it to pageContext
 */
public class SetList extends TagSupport {
    private static final Logger logger = LogManager.getLogger(SetList.class);
    private String var;
    private String value;

    public void setVar(String var) {
        this.var = var;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int doStartTag() {
        logger.trace("init: var={}, value={}", var, value);

        List<String> list = new ArrayList<>();
        Collections.addAll(list, value.split("\\s+"));
        pageContext.setAttribute(var, list);

        return SKIP_BODY;
    }
}
