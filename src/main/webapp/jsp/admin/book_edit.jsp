<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>


<l:redirectIfEmpty value="${param.command}" errorMsg="No command passed" />

<c:choose>
    <c:when test="${not empty bookSearchLink}" >
        <c:set value="${bookSearchLink}" var="cancelLink"/>
    </c:when>
    <c:otherwise>
        <c:set value="/jsp/home.jsp" var="cancelLink"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty savedUserInput}">
        <c:set value="${savedUserInput}" var="book" />
    </c:when>
    <c:otherwise>
        <c:set value="${proceedBook}" var="book" />
    </c:otherwise>
</c:choose>

<c:if test="${param.command eq 'book.add'}">
    <c:set value="header.create.book" var="currentHeader"/>
</c:if>
<c:if test="${param.command eq 'book.edit'}">
    <c:set value="header.edit" var="currentHeader"/>
</c:if>

<jsp:useBean id="now" class="java.util.Date" />
<fmt:formatDate var="thisYear" value="${now}" pattern="yyyy" />


<div class="container-sm bg-light border col-sm-6 col-sm-offset-3 my-5 pt-2">
    <div class="container-sm col-sm-10 col-sm-offset-1">
        <div class="row">
            <h1>
                <fmt:message key='${currentHeader}'/>
            </h1>
        </div>
        <form action="/controller" method="post">
            <input type="hidden" name="command" value="${param.command}">
            <div class="row mb-1">
                <label for="title" class="col-md-3 col-form-label"><fmt:message key='header.title'/>: </label>
                <div class="col-md-7">
                    <input name="title" type="text" id="title"
                        class="col-md-6 form-control" required
                        <c:if test="${not empty book}">
                            value="<c:out value='${book.title}' />"
                        </c:if>
                        >
                </div>
            </div>
            <div class="row mb-1">
                <label for="isbn" class="col-md-3 col-form-label"><fmt:message key='header.isbn'/>: </label>
                <div class="col-md-7">
                    <input name="isbn" type="text" id="isbn"
                        class="col-md-6 form-control" required
                        <c:if test="${not empty book}">
                            value="<c:out value='${book.isbn}' />"
                        </c:if>
                        >
                </div>
            </div>
            <div class="row mb-2">
                <label for="year" class="col-md-3 col-form-label"><fmt:message key='header.year'/>: </label>
                <div class="col-md-7">
                    <input name="year" type="number" id="year" min=1900 max="${thisYear}"
                        class="form-control"
                        <c:if test="${not empty book}">
                            value="<c:out value='${book.year}' />"
                        </c:if>
                        required>
                </div>
            </div>
            <div class="row mb-2">
                <label for="langCode" class="col-md-3 col-form-label"><fmt:message key='link.language'/>: </label>
                <div class="col-md-7">
                    <input name="langCode" type="text" id="langCode" class="form-control"
                    <c:if test="${not empty book}">
                        value="<c:out value='${book.langCode}' />"
                    </c:if>
                    required>
                </div>
            </div>
            <div class="row mb-2">
                <label for="keepPeriod" class="col-md-3 col-form-label"><fmt:message key='header.keep.period'/>: </label>
                <div class="col-md-7">
                    <input name="keepPeriod" type="number" min="1" max="365" id="keepPeriod"
                        class="form-control"
                        <c:if test="${not empty book}">
                            value="<c:out value='${book.keepPeriod}' />"
                        </c:if>
                        required>
                </div>
            </div>
            <div class="row mb-2">
                <label for="total" class="col-md-3 col-form-label"><fmt:message key='header.amount'/>: </label>
                <div class="col-md-7">
                    <input name="total" type="number" id="total" min=0 class="form-control"
                        <c:if test="${not empty book}">
                            value="<c:out value='${book.bookStat.total}' />"
                        </c:if>
                        required>
                </div>
            </div>
            <div class="row mb-2">
                <label for="authors" class="col-md-3 col-form-label"><fmt:message key='header.authors'/>: </label>
                <div class="col-md-7">
                    <div class="input-group mb-3">
                            <input id="authorQuery" name="query" type="text" class="form-control"
                                placeholder="<fmt:message key='placeholder.search.author'/>"
                                aria-label="Search authors" aria-describedby="searchButton"
                                onclick="document.getElementById('searchResults').classList.remove('show');"
                            >
                            <button onclick="findAuthor();" class="btn btn-outline-secondary" type="button" id="searchButton">
                                <fmt:message key='header.search'/>
                            </button>
                    </div>
                    <div>
                        <ul id="searchResults" class="bg-white dropdown-menu">
                        </ul>
                    </div>
                    <div id="selectedAuthors">
                        <c:if test="${not empty book}">
                            <c:forEach var="author" items="${book.authors}">
                                <div class="form-check">
                                    <c:set var="thisID" value="selectedAuthors${author.id}" />
                                    <input type="checkbox" name="authorIDs" class="form-check-input"
                                        id="${thisID}"
                                        value="<c:out value='${author.id}' />"
                                        checked
                                    />
                                    <label class="form-check-label" for="${thisID}">
                                         <c:out value='${author.name}' />
                                    </label>
                                </div>
                            </c:forEach>
                        </c:if>
                    </div>
                </div>
            </div>
            <div class="row mb-2 my-2">
                <div class="col-sm container overflow-hidden">
                    <t:error />
                    <button type="submit" class="btn btn-primary"><fmt:message key='header.apply'/></button>
                    <a class="btn btn-danger" href="${cancelLink}"><fmt:message key='header.cancel'/></a>
                </div>
            </div>
        </form>
    </div>
</div>

<c:set var="userError" scope="session" value="" />
<c:set var="savedUserInput" scope="session" value="${null}" />

<jsp:include page="/WEB-INF/jspf/footer.jsp"/>