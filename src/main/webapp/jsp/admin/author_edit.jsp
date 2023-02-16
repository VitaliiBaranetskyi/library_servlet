<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>


<l:redirectIfEmpty value="${param.command}" errorMsg="No command passed" />
<c:choose>
    <c:when test="${not empty authorSearchLink}" >
        <c:set value="${authorSearchLink}" var="cancelLink"/>
    </c:when>
    <c:otherwise>
        <c:set value="/jsp/admin/authors.jsp" var="cancelLink"/>
    </c:otherwise>
</c:choose>

<c:if test="${param.command eq 'author.add'}">
    <c:set value="header.create.author" var="dynamicHeader"/>
</c:if>
<c:if test="${param.command eq 'author.edit'}">
    <c:set value="header.edit" var="dynamicHeader"/>
</c:if>

<c:choose>
    <c:when test="${not empty savedUserInput}">
        <c:set value="${savedUserInput}" var="author" />
    </c:when>
    <c:otherwise>
        <c:set value="${proceedAuthor}" var="author" />
    </c:otherwise>
</c:choose>


<div class="container-sm bg-light border col-sm-6 col-sm-offset-3 my-5 pt-2">
    <div class="container-sm col-sm-10 col-sm-offset-1">
        <div class="row">
            <h1>
                <fmt:message key='${dynamicHeader}'/>
            </h1>
        </div>
        <form action="/controller" method="post">
            <input type="hidden" name="command" value="${param.command}">
            <c:forEach var="lang" items="${langs}">
                <div class="row mb-1">
                    <label for="${lang.code}" class="col-md-3 col-form-label">
                        <fmt:message key='header.name.in.lang'/> <c:out value="${lang.code}" />:
                    </label>
                    <div class="col-md-7">
                        <input name="${lang.code}" type="text" id="${lang.code}"
                            <c:if test="${not empty author}">
                                value="<l:printAuthor author='${author}' lang='${lang}' fallback='false' />"
                            </c:if>
                            class="col-md-6 form-control" required >
                    </div>
                </div>
            </c:forEach>
            <div class="row mb-2">
                <label for="primaryLang" class="col-md-3 col-form-label"><fmt:message key='header.primary.language'/>: </label>
                <c:if test="${param.command eq 'author.edit'}" >
                    <input name="primaryLang" value="${author.primaryLang.code}" type='hidden' >
                </c:if>
                <div class="col-md-3">
                    <select name="primaryLang" id="primaryLang" class="form-select"
                        aria-label="<fmt:message key='header.primary.language'/>"
                        <c:if test="${param.command eq 'author.edit'}" >
                            disabled
                        </c:if>
                        >
                        <c:forEach var="lang" items="${langs}">
                            <option
                                <c:if test="${not empty author}">
                                    <c:if test="${author.primaryLang eq lang}" >selected</c:if>
                                </c:if>
                                >
                                <c:out value="${lang.code}" />
                            </option>
                        </c:forEach>
                    </select>
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

<c:remove var="savedUserInput" scope="session"/>

<jsp:include page="/WEB-INF/jspf/footer.jsp"/>