<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>

<c:set var="action" value="book.find" />
<l:setList var="list" value="title author isbn year" />
<c:set var="searchLink" value="${bookSearchLink}" />
<c:remove var="proceedBook" scope="session" />
<c:remove var="proceedUser" scope="session" />

<div class="container">
    <c:choose>
        <c:when test="${not empty user and user.role eq 'ADMIN'}">
            <t:searchBar searchParameters="${list}" action="${action}" addButtonHeader="header.create.book" addButtonLink="/jsp/admin/book_edit.jsp?command=book.add" />
        </c:when>
        <c:otherwise>
            <t:searchBar searchParameters="${list}" action="${action}" />
        </c:otherwise>
    </c:choose>
    <div class="alert alert-success col-sm-6" style="display: none;" role="alert" id="addedBookAlert">
      <fmt:message key='message.book.added'/>
    </div>
    <div class="alert alert-danger col-sm-6" style="display: none;" role="alert" id="addedBookError">
    </div>
    <div class="container pt-4">
        <c:if test="${not empty books}">
            <table class="table table-hover">
                <thead class="bg-secondary bg-gradient text-white">
                    <th scope="col"><fmt:message key='header.title'/></th>
                    <th scope="col"><fmt:message key='header.authors'/></th>
                    <th scope="col"><fmt:message key='header.isbn'/></th>
                    <th scope="col"><fmt:message key='header.year'/></th>
                    <c:if test="${not empty user and user.role eq 'USER'}">
                        <th scope="col"><fmt:message key='header.in.stock'/></th>
                        <th scope="col"><fmt:message key='header.action'/></th>
                    </c:if>
                    <c:if test="${not empty user and user.role eq 'ADMIN'}">
                        <th scope="col"><fmt:message key='header.keep.period'/></th>
                        <th scope="col"><fmt:message key='header.total.amount'/></th>
                        <th scope="col"><fmt:message key='header.in.stock'/></th>
                        <th scope="col"><fmt:message key='header.reserved'/></th>
                        <th scope="col"><fmt:message key='header.was.booked'/></th>
                        <th scope="col"><fmt:message key='header.action'/></th>
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
                            <c:if test="${not empty user and user.role eq 'USER'}">
                                <c:set var="availableAmount" value="${book.bookStat.inStock - book.bookStat.reserved}"/>
                                <td><c:out value="${availableAmount}"/></td>
                                <td>
                                    <c:set var="bookActionState" value="false" />
                                    <c:if test="${not empty booking and booking.books.contains(book)}">
                                        <c:set var="bookActionState" value="true" />
                                    </c:if>
                                    <c:if test="${availableAmount <= 0}">
                                        <c:set var="bookActionState" value="true" />
                                    </c:if>
                                    <button onclick="addBook(${book.id})" class="btn btn-primary" id="<c:out value='${book.id}' />"
                                        <c:if test="${bookActionState}">
                                            disabled
                                        </c:if>
                                    >
                                        <fmt:message key="header.book" />
                                    </button>
                                </td>
                            </c:if>
                            <c:if test="${not empty user and user.role eq 'ADMIN'}">
                                <td scope="col"><c:out value="${book.keepPeriod}" /></td>
                                <td scope="col"><c:out value="${book.bookStat.total}" /></td>
                                <td scope="col"><c:out value="${book.bookStat.inStock}" /></td>
                                <td scope="col"><c:out value="${book.bookStat.reserved}" /></td>
                                <td scope="col"><c:out value="${book.bookStat.timesWasBooked}" /></td>
                                <td>
                                    <div class="container">
                                        <div class="row justify-content-start">
                                            <div class="col-auto">
                                                <a class="btn btn-warning" href="/controller?command=book.edit&id=${book.id}">
                                                    <i class="bi bi-pencil"></i>
                                                </a>
                                            </div>
                                            <div class="col-auto">
                                                <form action="/controller" method="post">
                                                    <input type="hidden" name="command" value="book.delete">
                                                    <input type="hidden" name="id" value="${book.id}">
                                                    <button type="submit" class="btn btn-danger">
                                                        <i class="bi bi-trash"></i>
                                                    </button>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
            <%@ include file="/WEB-INF/jspf/pagination.jspf" %>
        </c:if>
        <c:if test="${not empty notFound}">
            <div class="container pt-4">
                <h5><fmt:message key='${notFound}'/></h5>
            </div>
        </c:if>
    </div>
</div>
<jsp:include page="/WEB-INF/jspf/footer.jsp"/>