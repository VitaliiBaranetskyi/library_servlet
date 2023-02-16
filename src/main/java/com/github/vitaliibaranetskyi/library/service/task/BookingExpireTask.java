package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.dao.BookingDao;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryCreator;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.entity.impl.Booking;
import com.github.vitaliibaranetskyi.library.exception.DaoException;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import com.github.vitaliibaranetskyi.library.constant.Common;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

/**
 * TimerTask which cancels BOOKED booking after period specified in web.xml time to free the reserved books
 */
public class BookingExpireTask extends AbstractPeriodicTask {
    public static final Logger logger = LogManager.getLogger(BookingExpireTask.class);
    static final String INIT_PARAM_PERIOD = BookingExpireTask.class.getName() + ".period";

    public final DaoFactoryImpl daoFactory;
    private volatile int daysBeforeExpired = -1;

    /**
     * Normal way to use this class
     */
    public BookingExpireTask() {
        this.daoFactory = DaoFactoryCreator.getDefaultFactory().newInstance();
    }

    /**
     * For testing purpose
     * @param daoFactory daoFactory to be used to get daos
     */
    public BookingExpireTask(DaoFactoryImpl daoFactory) {
        this.daoFactory = daoFactory;
    }

    @Override
    public void run() {
        logger.debug(Common.START_MSG);
        if (daysBeforeExpired == -1) {
            logger.fatal(Common.TIMER_TASK_INIT_ERROR, INIT_PARAM_PERIOD);
            return;
        }

        BookingDao dao = daoFactory.getBookingDao();
        try {
            for (Booking booking: dao.findBy("BOOKED", "state")) {
                logger.trace("check booking={}", booking);
                Calendar now = Calendar.getInstance();
                long pastDays = ChronoUnit.DAYS.between(booking.getModified().toInstant(), now.toInstant());

                if (pastDays >= daysBeforeExpired) {
                    booking.setState(Booking.State.CANCELED);
                    dao.update(booking);
                    logger.info("booking (id {}) is expired", booking.getId());
                }
            }
            logger.info("All BOOKED bookings proceed");
        } catch (ServiceException | DaoException e) {
            logger.error("Unable to get list of BOOKED bookings: {}", e.getMessage());
        }

        logger.debug(Common.END_MSG);
    }

    @Override
    public void init(ServletContext context) throws ServiceException {
        logger.debug(Common.START_MSG);

        String periodStr = context.getInitParameter(INIT_PARAM_PERIOD);
        if (periodStr == null) {
            throw new ServiceException(INIT_PARAM_PERIOD + " is not specified in web.xml");
        }

        try {
            int daysBeforeExpiredCandidate = Integer.parseInt(periodStr);
            if (daysBeforeExpiredCandidate < 0) {
                throw new ServiceException("it's not positive " + daysBeforeExpiredCandidate);
            }
            synchronized (this) {
                daysBeforeExpired = daysBeforeExpiredCandidate;
            }
            logger.info("Days before expired initialized successfully");
        } catch (NumberFormatException e) {
            throw new ServiceException(INIT_PARAM_PERIOD + " should be valid positive integer value: " + e.getMessage());
        }

        logger.debug(Common.END_MSG);
    }
}