package com.github.vitaliibaranetskyi.library.service.validator;

import javax.servlet.ServletContext;

/**
 * Wrapper for Context {@link Safe}
 */
public class SafeContext extends Safe<Object> {
    private final ServletContext context;

    public SafeContext(ServletContext context) {
        this.context = context;
    }

    @Override
    public SafeContext get(String s) {
        value = context.getAttribute(s);
        setParam(s);
        return this;
    }
}