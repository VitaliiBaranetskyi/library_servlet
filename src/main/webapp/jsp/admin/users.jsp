<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>


<c:set var="action" value="user.find" />
<l:setList var="list" value="email name role state" />
<c:set var="searchLink" value="${userSearchLink}" />
<c:remove var="proceedUser" scope="session" />

<div class="container">
    <t:searchBar searchParameters="${list}" action="${action}"
        addButtonHeader="header.create.user" addButtonLink="/jsp/register.jsp?command=user.add" />
    <c:if test="${not empty successMsg}">
        <div class="container-sm pt-2 col-sm-4 col-sm-offset-4">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <fmt:message key="${successMsg}" />!
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </div>
    </c:if>
    <div class="container pt-4">
        <c:if test="${not empty users}">
            <table class="table table-hover">
                <thead class="bg-secondary bg-gradient text-white">
                    <th scope="col"><fmt:message key='header.email'/></th>
                    <th scope="col"><fmt:message key='header.role'/></th>
                    <th scope="col"><fmt:message key='header.state'/></th>
                    <th scope="col"><fmt:message key='header.name'/></th>
                    <th scope="col"><fmt:message key='header.user.fine'/></th>
                    <th scope="col"><fmt:message key='header.edit'/></th>
                    <th scope="col"><fmt:message key='header.delete'/></th>
                </thead>
                <tbody>
                    <c:forEach var="user" items="${users}">
                        <c:if test="${user.state ne 'DELETED'}" >
                            <tr class="table-light">
                                <td><c:out value="${user.email}"/></td>
                                <td><c:out value="${user.role}"/></td>
                                <td><c:out value="${user.state}"/></td>
                                <td><c:out value="${user.name}"/></td>
                                <td><c:out value="${user.fine}"/></td>
                                <td><a class="btn btn-warning" href="/controller?command=user.edit&id=${user.id}">Edit</a></td>
                                <td>
                                    <form action="/controller" method="post">
                                        <input type="hidden" value="user.delete" name="command">
                                        <input type="hidden" value="${user.id}" name="id">
                                        <button type="submit" class="btn btn-danger"><fmt:message key='header.delete'/></button>
                                    </form>
                                </td>
                            </tr>
                        </c:if>
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

<c:remove var="successMsg" scope="session" />
<jsp:include page="/WEB-INF/jspf/footer.jsp"/>