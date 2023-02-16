<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>

<div class="container pt-4">
    <h2><fmt:message key='header.books.in.subscription'/></h2>
    <c:if test="${not empty bookings}">
        <div class="card">
            <div class="card-header">
            </div>
            <div class="card-body">
                <table class="table table-hover">
                    <thead>
                        <th scope="col"><fmt:message key='header.name'/></th>
                        <th scope="col"><fmt:message key='header.authors'/></th>
                        <th scope="col"><fmt:message key='header.isbn'/></th>
                        <th scope="col"><fmt:message key='header.year'/></th>
                        <th scope="col"><fmt:message key='header.return.up.to'/></th>
                    </thead>
                    <tbody>
                        <c:forEach var="booking" items="${bookings}">
                            <c:forEach var="book" items="${booking.books}">
                                <tr class="table-light">
                                    <td><c:out value="${book.title}"/></td>
                                    <td>
                                        <c:forEach var="author" items="${book.authors}">
                                            <l:printAuthor author="${author}" lang="${lang}"/>
                                        </c:forEach>
                                    </td>
                                    <td><c:out value="${book.isbn}"/></td>
                                    <td><c:out value="${book.year}"/></td>
                                    <td class="fw-bold">
                                        <l:printCalendar calendar="${booking.modified}"
                                            addDays="${book.keepPeriod}" format="yyyy-MM-dd HH:mm:ss"/>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>
    <c:if test="${empty bookings}">
        <div class="container pt-4"><h5><fmt:message key='message.no.book.in.subscription'/></h5></div>
    </c:if>
</div>


<jsp:include page="/WEB-INF/jspf/footer.jsp"/>