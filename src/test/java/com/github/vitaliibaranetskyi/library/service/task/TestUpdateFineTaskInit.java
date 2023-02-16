package com.github.vitaliibaranetskyi.library.service.task;

import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.servlet.ServletContext;
import java.util.Arrays;

import static com.github.vitaliibaranetskyi.library.service.task.UpdateFineTask.INIT_PARAM_FINE_PER_DAY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class TestUpdateFineTaskInit {
    private static DaoFactoryImpl daoFactory;

    @Parameters(name = "{index}: {0}")
    public static Iterable<? extends String> data() {
        return Arrays.asList(null, "", "not_a_number", "-4");
    }

    @Parameterized.Parameter // first data value (0) is default
    public String input;

    @Test(expected = ServiceException.class)
    public void testInitWithNotValidConfig() throws ServiceException {
        UpdateFineTask task = new UpdateFineTask();
        ServletContext context = mock(ServletContext.class);
        when(context.getInitParameter(INIT_PARAM_FINE_PER_DAY)).thenReturn(input);
        task.init(context);
    }
}
