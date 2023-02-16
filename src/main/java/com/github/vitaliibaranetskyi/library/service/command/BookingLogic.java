package com.github.vitaliibaranetskyi.library.service.command;

import com.github.vitaliibaranetskyi.library.constant.Pages;
import com.github.vitaliibaranetskyi.library.dao.AbstractSuperDao;
import com.github.vitaliibaranetskyi.library.dao.BookDao;
import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryCreator;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.entity.impl.Book;
import com.github.vitaliibaranetskyi.library.entity.impl.BookStat;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.entity.impl.User;
import com.github.vitaliibaranetskyi.library.exception.AjaxException;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.service.validator.SafeRequest;
import com.github.vitaliibaranetskyi.library.service.validator.SafeSession;
import com.github.vitaliibaranetskyi.library.constant.Common;
import com.github.vitaliibaranetskyi.library.constant.ServletAttributes;
import com.github.vitaliibaranetskyi.library.controller.servlet.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

/**
 * Class-util, has only static methods by design. All methods must be related to Booking entity, like find booking, etc.
 * All public methods here must comply with {@link Command} signature, as
 * they will be used in CommandContext as lambda-functions and called from Front Controller
 * {@link Controller}
 *
 * Behaviour depends upon authorized user.
 * USER role can:
 * <ul>
 * <li> add book to a NEW booking
 * <li> delete book from NEW booking
 * <li> book NEW booking
 * <li> cancel NEW/BOOKED booking
 * <li> see all their booking in any state
 * <li> see books in specific booking
 * <li> see books in subscription (means see all books in bookings with state equal to DELIVERED and LOCATION
 * equal to USER
 * </ul>
 * All new booking are saved to session. User can have only one new booking. (//TODO save new booking to DB also)
 * Look up for new booking should be in such order:
 * <ul>
 *      <li> in session? if no:
 *      <li> in db? if no:
 *      <li> if needed, create new booking
 * </ul>
 *
 *
 * LIBRARIAN role can:
 * <ul>
 *      <li> deliver BOOKED booking
 *      <li> done DELIVERED booking
 *      <li> cancel BOOKED booking
 *      <li> search for booking by user email or name, by booking state
 *  </ul>
 *  For all operation, except search, LIBRARIAN needs bookingID for proceed
 */
public class BookingLogic {
    private static final Logger logger = LogManager.getLogger(BookingLogic.class);
    private static final DaoFactoryImpl daoFactory = DaoFactoryCreator.getDefaultFactory().newInstance();
    private static final String BOOKING_TRACE = "booking={}";
    private static final String ATTR_BOOKING_SEARCH_LINK = "booking" + ServletAttributes.ATTR_SEARCH_LINK;

    /**
     * Made private intentionally, no instance is needed by design
     */
    private BookingLogic() {
    }

    private static Booking findBooking(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        return findBooking(req, false);
    }

    private static Booking findBooking(HttpServletRequest req, boolean create) throws ServiceException, DaoException,
            AjaxException {
        logger.debug(Common.START_MSG);
        HttpSession session = req.getSession();

        logger.trace("sessionID={}, reqURI={}", session.getId(), req.getRequestURI());
        User u = (User) session.getAttribute("user");

        Booking booking = null;
        if (u.getRole().equals(User.Role.USER)) {
            booking = findBookingForUser(session, u, create);
        }

        if (u.getRole().equals(User.Role.LIBRARIAN)) {
            String bookingID = req.getParameter(ServletAttributes.BOOKING_ID);
            if (bookingID != null) {
                logger.debug("found {} in request", ServletAttributes.BOOKING_ID);
                logger.trace("{}={}", ServletAttributes.BOOKING_ID, bookingID);
                BookingDao dao = daoFactory.getBookingDao();
                booking = dao.read(Long.parseLong(bookingID));
            }
            if (booking == null) {
                throw new ServiceException("error.booking.not.found");
            }
        }
        logger.trace(BOOKING_TRACE, booking);
        logger.debug(Common.END_MSG);
        return booking;
    }

    /**
     * This will definitely return booking in case of create=true;
     */
    private static Booking findBookingForUser(HttpSession session, User u, boolean create) throws AjaxException {
        logger.debug("findBooking request for USER role init...");

        Booking booking = (Booking) session.getAttribute(ServletAttributes.BOOKING);
        if (booking != null) {
            logger.debug("found booking in session");
            logger.trace(BOOKING_TRACE, booking);
            return booking;
        }

        logger.debug("booking was not found");
        if (create) {
            logger.debug("create new one requested");
            if (u.getFine() > 0) {
                throw new AjaxException(HttpServletResponse.SC_FORBIDDEN, "error.illegal.user.state");
            }
            Booking.Builder builder = new Booking.Builder();
            builder.setUser(u);
            booking = builder.build();
            session.setAttribute(ServletAttributes.BOOKING, booking);
            logger.trace(BOOKING_TRACE, booking);
        }

        return booking;
    }

    /**
     * Finds all booking in state DELIVERED and saves this list to session for further showing to a user
     *
     * @param req user request
     * @return page to be shown to user
     */
    public static String listBookInSubscription(HttpServletRequest req) throws DaoException {
        logger.debug(Common.START_MSG);
        HttpSession session = req.getSession();

        User user = (User) session.getAttribute("user");
        logger.trace("user={}", user);

        logger.debug("looking for user bookings in DB...");
        BookingDao dao = daoFactory.getBookingDao();
        List<Booking> bookings = dao.findDeliveredByUserID(user.getId());

        session.setAttribute(ServletAttributes.ATTR_BOOKINGS, bookings);
        logger.trace("set session attribute {}={}", ServletAttributes.ATTR_BOOKINGS, bookings);
        logger.debug(Common.END_MSG);
        return Pages.MY_BOOKS;
    }

    /**
     * Finds all booking according to pattern with pagination and saves this list to session for further showing to a
     * user
     *
     * @param req user request
     * @return page to be shown to user
     */
    public static String find(HttpServletRequest req) throws ServiceException {
        // /booking?command=find - (mb later user) or all (for librarian) bookings
        // only for librarian now
        logger.debug(Common.START_MSG);
        BookingDao dao = daoFactory.getBookingDao();
        return CommonLogicFunctions.findWithPagination(req, dao, ServletAttributes.ATTR_BOOKINGS, ServletAttributes.BOOKING, Pages.BOOKING);
    }

    /**
     * Finds current booking for a user and also all previous bookings. Saves all this to session
     *
     * @param req user request
     * @return page to be shown to user
     * @throws AjaxException is not thrown from this function
     */
    public static String basket(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);
        HttpSession session = req.getSession();

        User u = (User) session.getAttribute("user");
        if (!u.getRole().equals(User.Role.USER)) {
            throw new ServiceException("error.resource.forbidden");
        }

        Booking currentBooking = findBookingForUser(session, u, false);
        session.setAttribute(ServletAttributes.ATTR_PROCEED_BOOKING, currentBooking);
        logger.trace("set {} to {}", ServletAttributes.ATTR_PROCEED_BOOKING, currentBooking);

        BookingDao bookingDao = daoFactory.getBookingDao();

        List<Booking> bookings = bookingDao.findBy(u.getEmail(), "email");
        // we don't want current booking to be repeated
        bookings.remove(currentBooking);
        bookings.sort(Comparator.comparing(Booking::getState));

        session.setAttribute(ServletAttributes.ATTR_BOOKINGS, bookings);
        logger.trace("set {} to {}", ServletAttributes.ATTR_BOOKINGS, bookings);

        logger.debug(Common.END_MSG);
        return Pages.BASKET;
    }

    /**
     * Add book to new booking, if last doesn't exists, it'll create a new one. Booking will be saved to session
     * It supposes to work only with AJAX, so as result Ajax exception will be thrown to force Front Controller
     * treat this request as POST
     *
     * @param req user request
     * @return this never will be returned
     * @throws AjaxException on state of which will be decided that to show to user
     */
    public static String addBook(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);

        //Booking booking, long id
        Booking booking = findBooking(req, true); // booking != null guarantied
        if (req.getParameter("id") == null) {
            throw new AjaxException(HttpServletResponse.SC_BAD_REQUEST, "error.no.id.in.request");
        }

        long id = Long.parseLong(req.getParameter("id"));

        logger.trace("addBook request: booking={}, id={}", booking, id);
        if (booking.getState() != Booking.State.NEW) {
            throw new AjaxException(HttpServletResponse.SC_BAD_REQUEST, "error.add.book.to.not.new.booking");
        }

        BookDao bookDao = daoFactory.getBookDao();
        Book book = bookDao.read(id);
        if (book == null) {
            throw new AjaxException(HttpServletResponse.SC_NOT_FOUND, "error.not.found");
        }
        logger.trace("book={}", book);

        if (!booking.getBooks().contains(book)) {
            booking.addBook(book);
            logger.debug("Book was added");
        } else {
            logger.debug("Book already exists in booking");
        }
        req.setAttribute(ServletAttributes.ATTR_OUTPUT, String.valueOf(booking.getBooks().size()));
        logger.debug(Common.END_MSG);
        throw new AjaxException(Pages.XML_SIMPLE_OUTPUT);
    }

    /**
     * Removes book from a new booking. Supposes to work only as AJAX request, so it'll throw exception anyway
     *
     * @throws AjaxException on state of which will be decided that to show to user
     */
    public static String removeBook(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);

        // Booking booking, long id
        Booking booking = findBooking(req);
        if (req.getParameter("id") == null) {
            throw new ServiceException("error.no.id.in.request");
        }

        long id = Long.parseLong(req.getParameter("id"));

        logger.trace("removeBook request: booking={}, id={}", booking, id);

        if (booking == null) {
            throw new ServiceException("error.add.some.book");
        }

        if (booking.getState() != Booking.State.NEW) {
            throw new ServiceException("error.remove.illegal.state");
        }

        AbstractSuperDao<Book> bookDao = daoFactory.getBookDao();
        Book book = bookDao.read(id);
        booking.removeBook(book);

        logger.debug(Common.END_MSG);
        return Pages.BASKET;
    }

    /**
     * Cancel booking. Only NEW and BOOKED bookings can be canceled. Supposes to work only with AJAX requests, so
     * exception will be thrown anyway.
     *
     * @throws AjaxException on state of this will be decided that to show to user
     */
    public static String cancel(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);
        HttpSession session = req.getSession();
        // Booking booking
        Booking booking = findBooking(req);
        logger.trace("cancel request: booking={}", booking);

        if (booking == null) {
            throw new ServiceException("error.cancel.null.booking");
        }

        Booking.State state = booking.getState();
        if (state != Booking.State.NEW && state != Booking.State.BOOKED) {
            throw new ServiceException("error.cancel.illegal.state");
        }

        if (state == Booking.State.NEW) {
            // nothing was added to DB yet, so just delete and forget
            booking.setBooks(new ArrayList<>());
            return nextPageLogic(session);
        }

        // so state was BOOKED and written to DB
        for (Book book : booking.getBooks()) {
            BookStat bookStat = book.getBookStat();
            bookStat.setReserved(bookStat.getReserved() - 1);
        }
        booking.setState(Booking.State.CANCELED);
        booking.setModified(Calendar.getInstance());
        daoFactory.getBookingDao().update(booking);
        //req.setAttribute(PAGE, Pages.BASKET);

        logger.debug(Common.END_MSG);
        return nextPageLogic(session);
    }

    private static String nextPageLogic(HttpSession session) throws ServiceException {
        logger.debug(Common.START_MSG);
        SafeSession safeSession = new SafeSession(session);

        String page = new SafeSession(session).get(ATTR_BOOKING_SEARCH_LINK).convert(String.class::cast);
        if (page == null) {
            logger.trace("no previous search link in session");

            User currentUser = safeSession.get(ServletAttributes.USER).notNull().convert(User.class::cast);
            page = currentUser.getRole() == User.Role.LIBRARIAN ? Pages.BOOKING : Pages.BASKET;
        }

        logger.debug(Common.END_MSG);
        return page;
    }

    /**
     * Books booking. Only NEW bookings can be booked. Normal request, so AjaxException will not be thrown here.
     *
     */
    public static String book(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);
        // Booking booking
        Booking booking = findBooking(req);

        logger.trace("book request: booking={}", booking);

        if (booking == null) {
            throw new ServiceException("error.booked.null.booking");
        }

        Booking.State state = booking.getState();
        if (state != Booking.State.NEW) {
            throw new ServiceException("error.booked.illegal.state");
        }

        booking.setState(Booking.State.BOOKED);
        booking.setModified(Calendar.getInstance());

        daoFactory.getBookingDao().create(booking);
        req.getSession().removeAttribute(ServletAttributes.BOOKING);
        logger.debug(Common.END_MSG);
        return Pages.BASKET;
    }

    /**
     * Delivers booking. Can deliver to USER or to LIBRARY, the last means reading room.
     * Changes book stats for all books in booking:
     *  <ul>
     *      <li> in-house decreased by 1
     *      <li> reserved increased by 1
     *  </ul>
     *
     * @param req HttpServletRequest
     * @return page to be shown to user
     * @throws AjaxException won't be thrown here
     */
    public static String deliver(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);
        Booking booking = findBooking(req);

        SafeRequest safeRequest = new SafeRequest(req);
        boolean subscription = safeRequest.get("subscription").convert(Boolean::parseBoolean);
        logger.trace("booking={}, subscription={}", booking, subscription);

        if (booking == null) {
            throw new ServiceException("error.deliver.null.booking");
        }
        if (booking.getState() != Booking.State.BOOKED) {
            throw new ServiceException("error.deliver.illegal.state");
        }

        booking.setState(Booking.State.DELIVERED);
        if (subscription) {
            booking.setLocated(Booking.Place.USER);
            logger.trace("deliver to user");
        }

        for (Book book : booking.getBooks()) {
            BookStat stat = book.getBookStat();
            stat.setReserved(stat.getReserved() - 1);
            stat.setInStock(stat.getInStock() - 1);
        }
        booking.setModified(Calendar.getInstance());

        BookingDao dao = daoFactory.getBookingDao();
        dao.update(booking);

        logger.debug(Common.END_MSG);
        return nextPageLogic(req.getSession());
    }

    /**
     * Finishes booking. All books from it are returned to library.
     * It's final state of booking.
     *
     * @param req user request
     * @return page to be shown to user
     * @throws AjaxException won't be thrown here
     */
    public static String done(HttpServletRequest req) throws ServiceException, DaoException, AjaxException {
        logger.debug(Common.START_MSG);
        Booking booking = findBooking(req);
        logger.trace("done request: booking={}", booking);
        if (booking == null) {
            throw new ServiceException("error.done.null.booking");
        }

        if (booking.getState() != Booking.State.DELIVERED) {
            throw new ServiceException("error.done.illegal.state");
        }

        booking.setState(Booking.State.DONE);
        for (Book book : booking.getBooks()) {
            BookStat stat = book.getBookStat();
            stat.setInStock(stat.getInStock() + 1);
        }
        booking.setModified(Calendar.getInstance());

        daoFactory.getBookingDao().update(booking);

        logger.debug(Common.END_MSG);
        return nextPageLogic(req.getSession());
    }
}
