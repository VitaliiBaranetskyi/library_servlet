<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>

<c:set var="action" value="booking.find" />
<l:setList var="list" value="email name" />
<c:set var="searchLink" value="${bookingSearchLink}" />

<div class="container">
    <t:searchBar searchParameters="${list}" action="${action}" addFilter="true"/>

    <t:listBookings bookings="${bookings}" user="${user}" />
    <c:if test="${not empty bookings}">
        <%@ include file="/WEB-INF/jspf/pagination.jspf" %>
    </c:if>
    <c:if test="${not empty notFound}">
        <div class="container pt-4">
            <h5><fmt:message key='${notFound}'/></h5>
        </div>
    </c:if>
</div>
<jsp:include page="/WEB-INF/jspf/footer.jsp"/>