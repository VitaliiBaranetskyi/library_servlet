package com.github.vitaliibaranetskyi.library.service.validator;

import javax.servlet.http.HttpSession;

/**
 * Wrapper for Session {@link Safe}
 */
public class SafeSession extends Safe<Object> {
    private final HttpSession session;

    public SafeSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public SafeSession get(String s) {
        value = session.getAttribute(s);
        setParam(s);
        return this;
    }
}
