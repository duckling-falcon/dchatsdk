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
	IRtpSvc rtpsvc = RtpSvc.getInstance(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<f:script  src="${path}/scripts/protcol-laucher.js"/>
	<%=rtpsvc.refJS()%>
</head>
<body style="margin:0;padding:0">
<c:choose>
<c:when test="${flag eq true }">
	<iframe id="hiddenIframe" src="about:blank" style="display:none;"></iframe>
	<a id="hiddenAnchor" style="display:none;">nothing</a>
	<dchat:pubacc account="${account}" autoclick="true" 
		params="name=${unitName}&content=${content}&stype=${stype}小时&telephone=${telephone}&indexURL=${indexURL}"/>
</c:when>
<c:otherwise>
	<span>403 Forbidden</span>
</c:otherwise>
</c:choose>
</body>
</html>