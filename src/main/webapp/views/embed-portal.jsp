<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="WEB-INF/tld/dchat.tld" prefix="dchat"%>
<%@ taglib uri="WEB-INF/tld/falcon.tld" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> 
<%@ page import="com.rooyeetone.rtp.sdk.*"%>

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
	<c:choose>
		<c:when test="${state eq 'login' }">
			<title>科信（dChat） - 登录科信</title>
		</c:when>
		<c:when test="${state eq 'grouptalk' }">
			<title>科信（dChat） - 在线群聊科信</title>
		</c:when>
		<c:otherwise>
			<title>科信（dChat） - 在线服务</title>
		</c:otherwise>
	</c:choose>
</head>
<body style="margin:0;padding:0">
	<c:choose>
		<c:when test="${state eq 'login' }">
			<iframe height="600" marginwidth="0" marginheight="0" scrolling="no"
				border="0" frameborder="no" width="100%" id="umtLogin"
				src="${passportDomain}/oauth2/authorize?response_type=code&redirect_uri=${oauthCallbackURI}&state=login&client_id=${oauthClientID}&theme=simple">
			</iframe>
		</c:when>
		<c:when test="${state eq 'grouptalk' }">
			<iframe height="600" marginwidth="0" marginheight="0" scrolling="no"
				border="0" frameborder="no" width="100%" id="umtLogin"
				src="${passportDomain}/oauth2/authorize?response_type=code&redirect_uri=${oauthCallbackURI}&state=grouptalk_${groupid}&client_id=${oauthClientID}&theme=simple">
			</iframe>
		</c:when>
		<c:otherwise>
			<iframe height="600" marginwidth="0" marginheight="0" scrolling="no"
				border="0" frameborder="no" width="100%" id="umtLogin"
				src="${passportDomain}/oauth2/authorize?response_type=code&redirect_uri=${oauthCallbackURI}&state=talkto_${account}&client_id=${oauthClientID}&theme=simple">
			</iframe>
		</c:otherwise>
	</c:choose>

</body>
<f:script  src="${path}/resource/js/jquery-1.7.2.min.js"/>

</html>