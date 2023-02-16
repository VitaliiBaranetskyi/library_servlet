package com.github.vitaliibaranetskyi.library.service.validator;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;

import javax.servlet.http.HttpServletRequest;

/**
 * Wrapper for Request {@link Safe}
 * Has special functions to work with String, because it's the most needed functionality for request parameters
 */
public class SafeRequest extends Safe<String> {
    private final HttpServletRequest req;

    public SafeRequest(HttpServletRequest req) {
        this.req = req;
    }

    @Override
    public SafeRequest get(String s) {
        value = req.getParameter(s);
        if (value != null) {
            value = value.trim();
        }
        setParam(s);

        return this;
    }
    
    public String convert() {
        return value == null ? "" : value;
    }

    public SafeRequest notEmpty() throws ServiceException {
        if (value == null || value.equals("")) {
            throw new ServiceException("error.parameter.is.empty", param);
        }

        return this;
    }

    public SafeRequest asEmail() throws ServiceException {
        if (value == null || !value.matches("^[\\p{LD}\\\\._-]+@[\\p{LD}\\\\._-]+\\.[\\p{L}]{2,6}$")) {
            throw new ServiceException("error.invalid.email", value);
        }
        return this;
    }

    public SafeRequest escape() {
        if (value != null) {
            value = value.replaceAll("([<>\\*\\+\\?^\\$\\{\\}\\|\\[\\]\\\\])", "\\\\$1");
        }
        return this;
    }
}
