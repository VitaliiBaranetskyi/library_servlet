<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/libTags.tld" prefix="l" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<%@ attribute name="booking" required="true" rtexprvalue="true" type="com.github.vitaliibaranetskyi.library.entity.impl.Booking"%>
<%@ attribute name="books" required="true" rtexprvalue="true" type="java.util.List"%>
<%@ attribute name="state" required="true" rtexprvalue="true"%>
<%@ attribute name="error" required="true" %>

<c:if test="${not empty books}">
    <div class="card">
        <div class="card-header">
        </div>
        <div class="card-body">
            <table class="table table-hover">
                <thead>
                    <th scope="col"><fmt:message key='header.title'/></th>
                    <th scope="col"><fmt:message key='header.authors'/></th>
                    <th scope="col"><fmt:message key='header.isbn'/></th>
                    <th scope="col"><fmt:message key='header.year'/></th>
                    <c:if test="${state eq 'DELIVERED'}">
                        <th scope="col"><fmt:message key='header.return.up.to'/></th>
                    </c:if>
                </thead>
                <tbody>
                    <c:forEach var="book" items="${books}">
                        <tr class="table-light">
                            <td><c:out value="${book.title}"/></td>
                            <td>
                                <c:forEach var="author" items="${book.authors}">
                                    <l:printAuthor author="${author}" lang="${lang}"/>
                                </c:forEach>
                            </td>
                            <td><c:out value="${book.isbn}"/></td>
                            <td><c:out value="${book.year}"/></td>
                            <c:if test="${state eq 'DELIVERED'}" >
                                <td class="fw-bold">
                                    <c:choose>
                                        <c:when test="${booking.located eq 'USER'}">
                                            <c:set var="days" value="${book.keepPeriod}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="days" value="1" />
                                        </c:otherwise>
                                    </c:choose>
                                    <l:printCalendar calendar="${booking.modified}"
                                        addDays="${days}" format="yyyy-MM-dd HH:mm:ss"/>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</c:if>
<c:if test="${empty books}">
    <div><c:out value="${error}" /></div>
</c:if>