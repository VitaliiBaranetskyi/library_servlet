<%@ include file="/WEB-INF/jspf/error_page_directive.jspf" %>
    <div class="container">
        <div>Request from ${pageContext.errorData.requestURI} is failed</div>
        <div>Servlet name: ${pageContext.errorData.servletName}</div>
        <div>Status code: ${pageContext.errorData.statusCode} </div>
        <div>Exception: ${pageContext.exception}</div>
        <div>Message from exception: ${pageContext.exception.message}</div>
    </div>
<jsp:include page="/WEB-INF/jspf/footer.jsp"/>