package com.github.vitaliibaranetskyi.library.constant;

/**
 * Common attributes used in business logic and JSP
 */
public class ServletAttributes {
    /**
     * Made private intentionally
     */
    private ServletAttributes() {}

    // Servlet only
    public static final String PAGE = "page";
    public static final String USER = "user";
    public static final String ROLE = "role";
    public static final String BOOKING = "booking";

    // jsp also
    public static final String SERVICE_ERROR = "serviceError";
    public static final String SERVICE_ERROR_PARAMETERS = "serviceErrorParameters";
    public static final String USER_ERROR = "userError";
    public static final String USER_ERROR_PARAMS = "userErrorParams";
    public static final String PASS = "password";
    public static final String EMAIL = "email";
    public static final String ATTR_BOOKINGS = "bookings";
    public static final String BOOKING_ID = "bookingID";
    public static final String NOT_FOUND = "notFound";
    public static final String APP_ROLES = "appRoles";
    public static final String COMMAND = "command";
    public static final String PLAIN_TEXT = "plainText";
    public static final String PAGES_NUM = "pagesNum";
    public static final String ATTR_SEARCH_LINK = "SearchLink";
    public static final String LOGIN_TRIES_NUMBER = "loginTriesNumber";

    public static final String ATTR_BOOKS = "books";
    public static final String ATTR_USERS = "users";
    public static final String ATTR_AUTHORS = "authors";

    public static final String SUPPORTED_LANGUAGES = "langs";
    public static final String PREFERRED_USER_LANG = "lang";
    public static final String DEFAULT_LANG = "defaultLang";
    public static final String LAST_VISITED_PAGE = "lastVisitedPage";

    public static final String ATTR_PROCEED_BOOK = "proceedBook";
    public static final String ATTR_PROCEED_USER = "proceedUser";
    public static final String ATTR_PROCEED_AUTHOR = "proceedAuthor";
    public static final String ATTR_PROCEED_BOOKING = "proceedBooking";
    public static final String ATTR_SAVED_USER_INPUT = "savedUserInput";
    public static final String ATTR_PRIMARY_AUTHOR_LANG = "primaryAuthorLang";
    public static final String ATTR_SUCCESS_MSG = "successMsg";

    public static final String JSP_AUTHOR_FORM_NAME = "name";
    public static final String ATTR_PRIMARY_LANG = "primaryLang";
    public static final String JSP_FORM_ATTR_ID = "id";


    public static final String ATTR_OUTPUT = "output";
    public static final String ATTR_FORMAT = "format";

    public static final String ATTR_AUTHOR_IDS = "authorIDs";
    public static final String AJAX_ERROR_PAGE = Pages.XML_SIMPLE_OUTPUT;
}