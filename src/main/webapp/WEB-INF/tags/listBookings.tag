<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/libTags.tld" prefix="l" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="t" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<%@ attribute name="bookings" required="true" rtexprvalue="true" type="java.util.List"%>
<%@ attribute name="user" required="true" rtexprvalue="true" type="com.github.vitaliibaranetskyi.library.entity.impl.User"%>

<c:if test="${not empty bookings}">
    <div class="accordion pt-4" id="bookingAccordion">
        <div class="accordion-item HEADER">
            <h5 class="accordion-header" id="head">
            <button class="accordion-button bg-secondary bg-gradient text-white">
                <div class="container">
                    <div class="row fw-bold align-items-center">
                        <div class="col-2"><fmt:message key='header.created'/></div>
                        <div class="col-2"><fmt:message key='header.state'/></div>
                        <c:if test="${user.role eq 'LIBRARIAN'}">
                            <div class="col-2"><fmt:message key='header.email'/></div>
                            <div class="col-2"><fmt:message key='header.name'/></div>
                        </c:if>
                        <div class="col-2"><fmt:message key='header.book.count'/></div>
                    </div>
                </div>
            </button>
            </h5>
        </div>
        <c:forEach var="booking" items="${bookings}">
            <c:choose>
                <c:when test='${booking.state eq "DELIVERED" and booking.located eq "USER"}'>
                    <c:set var="toggleClass" value="SUBSCRIPTION" />
                </c:when>
                <c:when test="${booking.state eq 'DELIVERED' and booking.located eq 'LIBRARY'}">
                    <c:set var="toggleClass" value="READING_ROOM" />
                </c:when>
                <c:otherwise>
                    <c:set var="toggleClass" value="${booking.state}" />
                </c:otherwise>
            </c:choose>
            <div class="accordion-item ${toggleClass}">
                <h2 class="accordion-header" id="heading${booking.id}">
                    <button class="accordion-button" type="button" data-bs-toggle="collapse"
                        data-bs-target="#collapse${booking.id}" aria-expanded="true"
                        aria-controls="collapse${booking.id}">
                        <div class="container">
                            <div class="row">
                                <div class="col-2">
                                    <l:printCalendar calendar="${booking.modified}" format="yyyy-MM-dd HH:mm:ss"/>
                                </div>
                                <div class="col-2">
                                    <c:choose>
                                        <c:when test="${booking.state eq 'DELIVERED' and booking.located eq 'USER'}" >
                                            <fmt:message key="header.booking.state.subscription" />
                                        </c:when>
                                        <c:when test="${booking.state eq 'DELIVERED' and booking.located eq 'LIBRARY'}" >
                                            <fmt:message key="header.booking.state.library" />
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:message key="header.booking.state.${booking.state}" />
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <c:if test="${user.role eq 'LIBRARIAN'}">
                                    <div class="col-2"><c:out value="${booking.user.email}"/></div>
                                    <div class="col-2"><c:out value="${booking.user.name}" default="none"/></div>
                                </c:if>
                                <div class="col-2"><c:out value="${booking.books.size()}"/></div>
                            </div>
                        </div>
                    </button>
                </h2>
                <div id="collapse${booking.id}" class="accordion-collapse collapse"
                    aria-labelledby="heading${booking.id}" data-bs-parent="#bookingAccordion">
                    <div class="accordion-body">
                        <t:listBooks books="${booking.books}" error="No books in this booking" state="${booking.state}" booking="${booking}"/>
                        <c:if test="${user.role eq 'LIBRARIAN'}">
                            <div class="container pt-2">
                                <div class="row justify-content-end row-cols-auto">
                                    <c:if test="${booking.state eq 'BOOKED'}">
                                        <div class="col">
                                            <form action="/controller" method="post">
                                                <input type="hidden" name="command" value="booking.deliver">
                                                <input type="hidden" name="subscription" value="true">
                                                <input type="hidden" name="bookingID" value="${booking.id}">
                                                <button class="btn btn-primary" type="submit">
                                                    <fmt:message key='header.subscription'/>
                                                </button>
                                            </form>
                                        </div>
                                        <div class="col">
                                            <form action="/controller" method="post">
                                                <input type="hidden" name="command" value="booking.deliver">
                                                <input type="hidden" name="subscription" value="false">
                                                <input type="hidden" name="bookingID" value="${booking.id}">
                                                <button class="btn btn-secondary" type="submit"><fmt:message key='header.in.house'/></button>
                                            </form>
                                        </div>
                                        <div class="col">
                                            <form action="/controller" method="post">
                                                <input type="hidden" name="command" value="booking.cancel">
                                                <input type="hidden" name="bookingID" value="${booking.id}">
                                                <button class="btn btn-danger" type="submit"><fmt:message key='header.cancel.booking'/></button>
                                            </form>
                                        </div>
                                    </c:if>
                                    <c:if test="${booking.state eq 'DELIVERED'}">
                                        <div class="col">
                                            <form action="/controller" method="post">
                                                <input type="hidden" name="command" value="booking.done">
                                                <input type="hidden" name="bookingID" value="${booking.id}">
                                                <button class="btn btn-primary" type="submit"><fmt:message key='header.done'/></button>
                                            </form>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>