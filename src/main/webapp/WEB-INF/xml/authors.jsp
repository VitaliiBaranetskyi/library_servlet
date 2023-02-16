<?xml version="1.0" encoding="UTF-8"?>

<%@page contentType="application/xml" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<authors>
    <c:choose>
        <c:when test="${empty authors}">
            <error><fmt:message key="error.not.found" /></error>
        </c:when>
        <c:otherwise>
            <c:forEach items="${authors}" var="author">
                <author>
                    <id><c:out value="${author.id}"/></id>
                    <name><c:out value="${author.name}"/></name>
                </author>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</authors>