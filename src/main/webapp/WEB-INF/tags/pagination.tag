<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="pagesNum" required="true" %>
<%@ attribute name="searchLink" required="true"%>
<%@ attribute name="currentPage" required="true" %>


<c:if test="${not empty pagesNum and pagesNum > 0}" >

<div class="container pt-4">
    <ul class="pagination justify-content-end">
        <li class="page-item  <c:if test='${currentPage eq 1}'>disabled</c:if>">
            <a class="page-link"
                href="${searchLink}&page=${currentPage - 1}" aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
            </a>
        </li>
        <c:forEach begin="1" end="${pagesNum}" varStatus="loop" >
            <li class="page-item  <c:if test='${loop.index eq currentPage}'>active</c:if>">
                <a class="page-link"
                    href="${searchLink}&page=${loop.index}">${loop.index}
                </a>
            </li>
        </c:forEach>
        <li class="page-item  <c:if test='${currentPage eq pagesNum}'>disabled</c:if>">
            <a class="page-link"
                href="${searchLink}&page=${currentPage + 1}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
            </a>
        </li>
    </ul>
</div>

</c:if>