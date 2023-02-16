package com.github.vitaliibaranetskyi.library.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Indicates errors in work with application, such as:
 * <ul>
 *     <li> some needed parameter wasn't provided
 *     <li> needed parameter is null
 *     <li> etc
 * </ul>
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 1L;
    private final List<String> msgParameters;

    public ServiceException(String msg, Throwable cause) {
        super(msg, cause);
        msgParameters = null;
    }

    public ServiceException(String msg) {
        super(msg);
        msgParameters = null;
    }

    /**
     * Used if there's a need to show parametrized and localized error message to user.
     * @param msg contains parametrized error message
     * @param msgParameters parameters for the error message
     */
    public ServiceException(String msg, String... msgParameters) {
        super(msg);
        this.msgParameters = Arrays.asList(msgParameters);
    }

    public List<String> getMsgParameters() {
        if (msgParameters == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(msgParameters);
    }
}
