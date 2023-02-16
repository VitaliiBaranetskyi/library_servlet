package com.github.vitaliibaranetskyi.library.controller.servlet;

public class TestLogin {
    /*
    private static Login servlet;
    private static HttpServletRequest request;
    private static HttpServletResponse response;
    private static HttpSession session;
    private static RequestDispatcher dispatcher;

    @Before
    public void initTest() {
        servlet = new Login();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
    }

    @Test
    public void testDoPost() throws IOException {
        when(request.getSession()).thenReturn(session);
        servlet.doPost(request, response);
        verify(session).setAttribute("user", null);
    }

    @Test
    public void testDoPostOnNullUser() throws IOException, ServletException {
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(Pages.LOGIN)).thenReturn(dispatcher);
        when(session.getAttribute("user")).thenReturn(null);
        servlet.doPost(request, response);
        verify(request).setAttribute(ServletAttributes.LOGIN_PAGE_ERROR_MSG, "Incorrect login or password");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void testDoPostOnNotNullUser() throws IOException, ServletException {
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(Pages.HOME)).thenReturn(dispatcher);
        when(session.getAttribute("user")).thenReturn(new User.Builder().build());
        servlet.doPost(request, response);
        verify(dispatcher).forward(request, response);
    }


     */
}
