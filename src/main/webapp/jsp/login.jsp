<%@ include file="/WEB-INF/jspf/normal_page_directive.jspf" %>

<head>
    <title><fmt:message key='header.login'/></title>
    <style>
        .login-page {
            color: #06051b;
            text-align: center;
            position: relative;
            width: 250px;
            padding: 10% 0 0;
            margin: 0 auto;

        }
        .form-login {
            position: relative;
            width: 330px;
            height: 330px;
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
<c:if test="${not empty successMsg}">
    <div class="container-sm pt-2 col-sm-4 col-sm-offset-4">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <fmt:message key="${successMsg}"/>. <fmt:message key="message.try.to.login"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </div>
</c:if>
<div class="form-login">
    <div class="login-page">
        <form action="/controller" method="post">
            <h1 align="center"><fmt:message key='header.login'/></h1>
            <input type="hidden" name="command" value="user.login">
            <div class="form-group ">
                <label for="email"><fmt:message key='header.email'/>:</label>
                <div class="input-group shadow-lg">
                    <input name="email" type="email" class="form-control" id="email" required>
                </div>
            </div>
            <div class="form-group ">
                <label for="password"><fmt:message key='header.password'/>:</label>
                <div class="input-group shadow-lg">
                    <input name="password" type="password" class="form-control" id="password" required>
                </div>
            </div>
            <div class="row my-2">
                <t:error/>
                <div class="col-sm container overflow-hidden">
                    <button type="submit" class="btn btn-primary"><fmt:message key='link.sign.in'/></button>
                    <a href="/jsp/register.jsp?command=user.add" class="btn btn-secondary"><fmt:message
                            key='link.sign.up'/></a>
                </div>
            </div>
        </form>
    </div>
</div>
<c:remove var="successMsg" scope="session"/>
<c:remove var="savedUserInput" scope="session"/>

<jsp:include page="/WEB-INF/jspf/footer.jsp"/>