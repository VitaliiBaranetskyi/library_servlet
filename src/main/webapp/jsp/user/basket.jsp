<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>


<div class="container pt-4">
    <h2><fmt:message key='header.current.booking'/></h2>
    <c:if test="${not empty proceedBooking and not empty proceedBooking.books}">
        <div class="card">
            <div class="card-header container">
                <div class="row justify-content-start row-cols-auto">
                    <div class="col"><c:out value="${proceedBooking.state}"/>:</div>
                    <div class="col"><l:printCalendar calendar="${proceedBooking.modified}" format="yyyy-MM-dd HH:mm:ss"/></div>
                </div>
            </div>
            <div class="card-body">
                <c:if test="${not empty proceedBooking.books}">
                    <table class="table table-hover">
                        <thead>
                            <th scope="col"><fmt:message key='header.title'/></th>
                            <th scope="col"><fmt:message key='header.authors'/></th>
                            <th scope="col"><fmt:message key='header.isbn'/></th>
                            <th scope="col"><fmt:message key='header.year'/></th>
                            <c:if test="${proceedBooking.state == 'NEW'}">
                                <th scope="col"><fmt:message key='header.action'/></th>
                            </c:if>
                        </thead>
                        <tbody>
                            <c:forEach var="book" items="${proceedBooking.books}">
                                <tr class="table-light">
                                    <td><c:out value="${book.title}"/></td>
                                    <td>
                                        <c:forEach var="author" items="${book.authors}">
                                            <l:printAuthor author="${author}" lang="${lang}"/>
                                        </c:forEach>
                                    </td>
                                    <td><c:out value="${book.isbn}"/></td>
                                    <td><c:out value="${book.year}"/></td>
                                    <c:if test="${proceedBooking.state == 'NEW'}">
                                        <td>
                                            <a href="/controller?command=booking.removeBook&id=${book.id}">
                                                <fmt:message key='header.delete'/>
                                            </a>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <div class="container">
                        <div class="row justify-content-end row-cols-auto">
                            <c:if test="${proceedBooking.state eq 'NEW'}">
                                <div class="col">
                                    <form action="/controller" method="post">
                                        <input type="hidden" name="command" value="booking.book">
                                        <button type="submit" class="btn btn-primary">
                                            <fmt:message key='header.finish.booking'/>
                                        </button>
                                    </form>
                                </div>
                                <div class="col">
                                    <form action="/controller" method="post">
                                        <input type="hidden" name="command" value="booking.cancel">
                                        <button type="submit" class="btn btn-danger"><fmt:message key='header.cancel.booking'/></button>
                                    </form>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </c:if>
    <c:choose>
        <c:when test="${empty proceedBooking}">
            <div class="container pt-4 fw-bold"><h5><fmt:message key='message.no.current.booking'/></h5></div>
        </c:when>
        <c:when test="${empty proceedBooking.books}">
                <div class="container pt-4 fw-bold"><h5><fmt:message key='message.no.current.booking'/></h5></div>
        </c:when>
    </c:choose>
</div>

<div class="container pt-4">
    <h2><fmt:message key='header.past.bookings'/></h2>
    <t:listBookings bookings="${bookings}" user="${user}" />
    <c:if test="${empty bookings}">
        <div class="container pt-4 fw-bold"><h5><fmt:message key='message.no.past.booking'/></h5></div>
    </c:if>
</div>


<jsp:include page="/WEB-INF/jspf/footer.jsp"/>