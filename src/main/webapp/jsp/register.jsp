<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<l:redirectIfEmpty value="${param.command}" errorMsg="No command passed" />

<c:choose>
    <c:when test="${not empty savedUserInput}">
        <c:set value="${savedUserInput}" var="userToEdit" />
    </c:when>
    <c:otherwise>
        <c:set value="${proceedUser}" var="userToEdit" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty user and user.role eq 'ADMIN'}">
        <l:redirectIfEmpty value="${appRoles}" errorMsg="Application roles were not initialized at startup" />
        <c:choose>
            <c:when test="${not empty userSearchLink}" >
                <c:set value="${userSearchLink}" var="cancelLink"/>
            </c:when>
            <c:otherwise>
                <c:set value="/jsp/admin/users.jsp" var="cancelLink"/>
            </c:otherwise>
        </c:choose>
        <c:if test="${param.command eq 'user.add'}">
            <c:set value="header.create.user" var="currentHeader"/>
        </c:if>
        <c:if test="${param.command eq 'user.edit'}">
            <c:set value="header.edit.user" var="currentHeader"/>
        </c:if>
        <c:if test="${empty userToEdit or userToEdit.id ne user.id}">
            <c:set var="notHidden" value="true" />
        </c:if>
    </c:when>
    <c:when test="${empty user}">
        <c:set value="/jsp/login.jsp" var="cancelLink" />
        <c:set value="header.register" var="currentHeader"/>
    </c:when>
    <c:otherwise>
        <c:set value="/jsp/home.jsp" var="cancelLink" />
        <c:set value="header.edit.my.info" var="currentHeader"/>
    </c:otherwise>
</c:choose>


<fmt:setLocale value="${lang.code}" />
<fmt:setBundle basename="i18n" />

<head>
    <title><fmt:message key='header.register'/></title>
    <style>
        .register-page {
            color: #06051b;
            text-align: center;
            position: relative;
            width: 250px;
            padding: 7% 0 0;
            margin: 0 auto;
        }

        .form-register {
            position: relative;
            width: 330px;
            height: 470px;
            top: 50%;
            left: 50%;
            margin-top: 260px;
            margin-left: -170px;
            background: #ffffff;
            border-radius: 3px;
            border: 1px solid #ccc;
            box-shadow: 0 1px 2px rgba(0, 0, 0, .1);
            -webkit-animation-name: bounceIn;
            -webkit-animation-fill-mode: both;
            -webkit-animation-duration: 1s;
            -webkit-animation-iteration-count: 1;
            -webkit-animation-timing-function: linear;
            -moz-animation-name: bounceIn;
            -moz-animation-fill-mode: both;
            -moz-animation-duration: 1s;
            -moz-animation-iteration-count: 1;
            -moz-animation-timing-function: linear;
            animation-name: bounceIn;
            animation-fill-mode: both;
            animation-duration: 1s;
            animation-iteration-count: 1;
            animation-timing-function: linear;
        }
    </style>
</head>
<div class="form-register">
    <div id="main-container" class="register-page">
        <div class="row pb-2">
            <h1>
                <fmt:message key='${currentHeader}'/>
            </h1>
        </div>
        <form action="/controller" method="post" accept-charset="UTF-8">
            <input type="hidden" name="command" value="${param.command}">
            <div class="form-group">
                <label for="email"><fmt:message key='header.email'/>: </label>
                <div class="cols-sm-10">
                    <input name="email" type="email" id="email" class=" form-control" onkeyup="check(this);" required
                            value="<c:out value='${userToEdit.email}' />"
                    >
                </div>
            </div>
            <div class="form-group">
                <label for="password"><fmt:message key='header.password'/>: </label>
                <div class="cols-sm-10">
                    <input name="password" type="password" id="password"
                        class="col-md-6 form-control" onkeyup='checkPass();'>
                </div>
            </div>
            <div class="form-group">
                <label for="confirmPass"><fmt:message key='header.confirm.password'/>: </label>
                <div class="cols-sm-10">
                    <input name="confirmPass" type="password" id="confirmPass"
                        class="col-md-6 form-control" onkeyup='checkPass();'>
                    <span id='message' class="text-danger" hidden><fmt:message key="error.msg.passwords.dont.match"/></span>
                </div>
            </div>
            <div class="form-group">
                <label for="name"><fmt:message key='header.name'/>: </label>
                <div class="cols-sm-10">
                    <input name="name" type="text" id="name" class="form-control" onkeyup="makeValid(this);"
                            value="<c:out value='${userToEdit.name}' />"
                    >
                </div>
            </div>
            <c:choose>
                <%-- if existing user edits herself --%>
                <c:when test="${empty notHidden and not empty userToEdit}" >
                    <input name="state" value="${userToEdit.state}" type="hidden">
                    <input name="role" value="${userToEdit.role}" type="hidden">
                </c:when>
                <%-- if user is not authenticated yet and tried to register --%>
                <c:when test="${empty userToEdit and empty user}" >
                    <input name="state" value="VALID" type="hidden">
                    <input name="role" value="USER" type="hidden">
                </c:when>
                <%-- if user is ADMIN and edit not himself --%>
                <c:otherwise>
                    <div class="row mb-2">
                        <label for="roles" class="col-md-3 col-form-label"><fmt:message key='header.role'/>: </label>
                        <div class="col-md-3">
                            <select name="role" id="roles" class="form-select"
                                aria-label="<fmt:message key='header.role'/>"
                            >
                                <c:forEach var="role" items="${appRoles}">
                                    <option <c:if test="${role eq fn:toLowerCase(userToEdit.role)}">selected</c:if>><c:out value="${role}" /></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input" type="checkbox" name="state" id="flexCheckChecked" value="valid"
                        <c:if test="${empty userToEdit or userToEdit.state eq 'VALID'}">checked</c:if>
                      >
                      <label class="form-check-label" for="flexCheckChecked" >
                        <fmt:message key='header.enable'/>
                      </label>
                    </div>
                </c:otherwise>
            </c:choose>
            <div class="row mb-2 my-2">
                <div class="col-sm container overflow-hidden">
                    <t:error />
                    <button type="submit" class="btn btn-primary">
                        <fmt:message key='header.apply'/>
                    </button>
                    <a class="btn btn-danger" href="${cancelLink}">
                        <fmt:message key='header.cancel'/>
                    </a>
                </div>
            </div>
        </form>
    </div>
</div>

<c:remove var="savedUserInput" scope="session" />
<jsp:include page="/WEB-INF/jspf/footer.jsp"/>

