<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="/WEB-INF/libTags.tld" prefix="l" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<%@ attribute name="action" required="true" %>
<%@ attribute name="searchParameters" required="true" rtexprvalue="true" type="java.util.List"%>
<%@ attribute name="addButtonHeader"%>
<%@ attribute name="addButtonLink"%>
<%@ attribute name="addFilter"%>


<div class="container pt-4">
    <form action="/controller" >
        <input type="hidden" name="command" value="${action}">
        <input type="hidden" name="page" value=1>
        <div class="row justify-content-end align-items-center row-cols-auto ">
            <input class="form-control-lg col-4" type="search" name="query"
                placeholder="<fmt:message key='search.placeholder.msg'/>"
                value="<c:out value='${param.query}' />"
            >
            <div class="form-floating col-2" <c:if test="${searchParameters.size() eq 1}">hidden</c:if> >
                <select class="form-select" name="searchBy" id="searchBy" aria-label="searchBy"
                    data-header="<fmt:message key='header.search.by'/>"
                >
                    <c:forEach var="attr" items="${searchParameters}">
                        <option class="dropdown-item" value="${attr}"
                            <c:if test="${param.searchBy eq attr}">
                                selected
                            </c:if>
                        >
                            <fmt:message key="header.${attr}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="searchBy"><fmt:message key="header.search.by"/></label>
            </div>
            <div class="form-floating col-2" <c:if test="${searchParameters.size() eq 1}">hidden</c:if>>
                <select class="form-select" name="sortBy" id="sortBy" aria-label="Sort by">
                    <c:forEach var="attr" items="${searchParameters}">
                        <option class="dropdown-item" value="${attr}"
                            <c:if test="${param.sortBy eq attr}">
                                selected
                            </c:if>
                        >
                            <fmt:message key="header.${attr}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="sortBy"><fmt:message key='header.sort.by'/></label>
            </div>
            <div class="form-floating col-1">
                <select class="form-select" id="showBy" aria-label="show by" name="num">
                    <option <c:if test="${param.num eq 5}">selected</c:if> >
                        5
                    </option>
                    <option <c:if test="${param.num eq 10}">selected</c:if> >10</option>
                    <option <c:if test="${param.num eq 20}">selected</c:if> >20</option>
                </select>
                <label for="showBy"><fmt:message key='header.show.by'/></label>
            </div>
            <div class="col-1">
                <button type="submit" class="btn btn-primary"><fmt:message key='header.search'/></button>
            </div>
            <c:if test="${not empty addButtonHeader}" >
                <div class="col-auto">
                    <a class="btn btn-info" href="${addButtonLink}"><fmt:message key="${addButtonHeader}"/></a>
                </div>
            </c:if>
            <c:if test="${not empty addFilter}" >
                <div class="col-auto">
                    <div class="dropdown">
                      <button class="btn btn-secondary dropdown-toggle " type="button" id="filter" data-bs-toggle="dropdown" aria-expanded="false">
                        Filter
                      </button>
                      <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="filter">
                        <li><a class="dropdown-item" onclick="toggleBooking('BOOKED');"><fmt:message key="header.booking.state.BOOKED" /></a></li>
                        <li><a class="dropdown-item" onclick="toggleBooking('SUBSCRIPTION');"><fmt:message key="header.booking.state.subscription" /></a></li>
                        <li><a class="dropdown-item" onclick="toggleBooking('READING_ROOM');"><fmt:message key="header.booking.state.library" /></a></li>
                        <li><a class="dropdown-item" onclick="toggleBooking('DONE');"><fmt:message key="header.booking.state.DONE" /></a></li>
                        <li><a class="dropdown-item" onclick="toggleBooking('CANCELED');"><fmt:message key="header.booking.state.CANCELED" /></a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item" onclick="toggleBooking('ALL');"><fmt:message key="header.booking.state.ALL" /></a></li>
                      </ul>
                    </div>
                </div>
            </c:if>
        </div>
    </form>
</div>
