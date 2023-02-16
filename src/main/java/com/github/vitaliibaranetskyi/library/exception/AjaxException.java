package com.github.vitaliibaranetskyi.library.exception;

/**
 * Used to indicate the Command is AJAX-only command
 */
public class AjaxException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int errorCode;
    private final String nextPage;

    /**
     * Error was occurred during Ajax request, will be redirected to error XML
     * @param errorCode HTTP error code
     * @param msg error msg
     */
    public AjaxException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
        this.nextPage = null;
    }

    /**
     * Forward request, no error was occurred
     * @param nextPage page to be shown to user
     */
    public AjaxException(String nextPage) {
        this.nextPage = nextPage;
        this.errorCode = -1;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getNextPage() {
        return nextPage;
    }
}
