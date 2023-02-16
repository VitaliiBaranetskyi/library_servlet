<?xml version="1.0" encoding="UTF-8"?>

<%@page contentType="application/xml" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<response>
    <c:choose>
        <c:when test="${not empty serviceError}">
            <error><fmt:message key="${serviceError}" /></error>
        </c:when>
        <c:otherwise>
            <output><c:out value="${output}"/></output>
        </c:otherwise>
    </c:choose>
</response>