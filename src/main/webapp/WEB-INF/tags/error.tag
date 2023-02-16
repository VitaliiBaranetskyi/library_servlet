<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<p class="text-danger">
    <c:if test="${not empty userError}" >
        <fmt:message key='${userError}'>
            <c:if test="${not empty userErrorParams}" >
                <c:forEach var="errorParam" items="${userErrorParams}">
                    <fmt:param value="${errorParam}" />
                </c:forEach>
            </c:if>
        </fmt:message>
    </c:if>
</p>

<c:remove var="userError" scope="session" />
<c:remove var="userErrorParams" scope="session"/>