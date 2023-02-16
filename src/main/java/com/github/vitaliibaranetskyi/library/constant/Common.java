package com.github.vitaliibaranetskyi.library.constant;

/**
 * Common constants in project
 */
public class Common {
    public static final String START_MSG = "start";
    public static final String END_MSG = "end";
    public static final String SUCCESS = "success";
    public static final String NO_UPDATE = "nothing was updated";

    public static final String TIMER_TASK_INIT_ERROR = "Required attribute {} was not set: " +
            "this.init(servletContext) was not called";

    /**
     * Made private intentionally
     */
    private Common() {}
}
