package com.github.vitaliibaranetskyi.library.service.validator;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;

public class TestSafeRequest {
    private static final String PARAM = "badStr";
    private HttpServletRequest request;
    @Before
    public void mockRequest() {
        request = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    public void testEscapeNotNullString() throws ServiceException {
        final String expected = "All of these should be escaped: \\\\ \\^ \\$ \\* \\+ \\? \\| \\{ \\} \\[ \\< \\>" +
                " \\]";
        when(request.getParameter(PARAM)).thenReturn("All of these should be escaped: \\ ^ $ * + ? | { } [ < >" +
                " ]");
        SafeRequest safe = new SafeRequest(request);
        String result = safe.get(PARAM).notEmpty().escape().convert();
        Assert.assertEquals(expected, result);
    }

    @Test(expected = ServiceException.class)
    public void testShouldThrowExceptionOnNullString() throws ServiceException {
        when(request.getParameter(PARAM)).thenReturn(null);
        SafeRequest safe = new SafeRequest(request);
        String result = safe.get(PARAM).notEmpty().convert();
    }

    @Test(expected = ServiceException.class)
    public void testShouldThrowExceptionOnBadEmail() throws ServiceException {
        when(request.getParameter(PARAM)).thenReturn("asdf.ak-sd.s");
        SafeRequest safe = new SafeRequest(request);
        String result = safe.get(PARAM).notEmpty().asEmail().convert();
    }

    @Test
    public void testValidRussianEmailShouldPass() throws ServiceException {
        final String expected = "фыиф.лыв@ываю.оа";
        when(request.getParameter(PARAM)).thenReturn(expected);
        SafeRequest safe = new SafeRequest(request);
        String result = safe.get(PARAM).notEmpty().asEmail().convert();
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testValidEnglishEmailShouldPass() throws ServiceException {
        final String expected = "test24_.s@gmail.com";
        when(request.getParameter(PARAM)).thenReturn(expected);
        SafeRequest safe = new SafeRequest(request);
        String result = safe.get(PARAM).notEmpty().asEmail().convert();
        Assert.assertEquals(expected, result);
    }

    @Test(expected = ServiceException.class)
    public void testShouldThrowExceptionOnBadLongID() throws ServiceException {
        when(request.getParameter(PARAM)).thenReturn("skdjf");
        SafeRequest safe = new SafeRequest(request);
        long result = safe.get(PARAM).notNull().convert(Long::parseLong);
    }

    @Test
    public void testValidLongIDShouldPass() throws ServiceException {
        final long expected = 104;
        when(request.getParameter(PARAM)).thenReturn(String.valueOf(expected));
        SafeRequest safe = new SafeRequest(request);
        long result = safe.get(PARAM).notNull().convert(Long::parseLong);
        Assert.assertEquals(expected, result);
    }
}
