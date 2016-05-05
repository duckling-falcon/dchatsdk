<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="WEB-INF/tld/falcon.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> 
<%
	String path = request.getContextPath();
	request.setAttribute("path", path);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<f:css href="${path}/resource/dchat.css" />
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap.min.css" />
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap-responsive.min.css" />
</head>
<body style="background:#f1f5f7">
	<c:choose>
		<c:when test="${status == 'success'}">
			<h1 class="success">${username}已被激活，欢迎登录使用科信（dChat）!<br><br><a id="goto_parent_btn" href="#" class="btn btn-primary">返回</a></h1>
		</c:when>
		<c:when test="${status == 'existed' }">
			<h1 class="fail">${username}状态已被激活，无需再次激活！<br><br><a id="goto_parent_btn"  href="#" class="btn btn-primary">返回</a></h1>
		</c:when>
		<c:otherwise>
			<h1 class="fail">激活失败，请检查您的帐号和密码。<br><br><a id="goto_parent_btn"  href="#" class="btn btn-primary">返回</a></h1>
		</c:otherwise>
	</c:choose>
</body>
<f:script  src="${path}/resource/js/jquery-1.7.2.min.js"/>
<script type="text/javascript">
$(document).ready(function(){
        $("#goto_parent_btn").click(function(){
                window.parent.location.href='${homeURL}';
        });
});
</script>
</html>