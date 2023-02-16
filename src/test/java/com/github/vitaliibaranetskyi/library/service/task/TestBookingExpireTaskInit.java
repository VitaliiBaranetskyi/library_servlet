package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.servlet.ServletContext;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TestBookingExpireTaskInit {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<? extends String> data() {
        return Arrays.asList(null, "", "not_a_number", "-4");
    }

    @Parameterized.Parameter
    public String input;

    @Test(expected = ServiceException.class)
    public void testInitWithWrongInitConfig() throws ServiceException {
        ServletContext context = mock(ServletContext.class);
        when(context.getInitParameter(BookingExpireTask.INIT_PARAM_PERIOD)).thenReturn(input);
        BookingExpireTask task = new BookingExpireTask();
        task.init(context);
    }
}
