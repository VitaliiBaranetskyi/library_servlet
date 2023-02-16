<%@ include file="/WEB-INF/jspf/error_page_directive.jspf" %>

<c:set var="serviceError" scope="session" value="${null}" />

<div class="container pt-4">
    <div class="container">
        <h1><fmt:message key='message.sorry'/></h1>
        <p><fmt:message key="${serviceError}"/></p>
    </div>
</div>

<jsp:include page="/WEB-INF/jspf/footer.jsp"/>