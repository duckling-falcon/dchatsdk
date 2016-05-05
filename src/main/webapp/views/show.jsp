<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
	<f:css href="${path}/resource/dchat.css" />
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap.min.css" />
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap-responsive.min.css" />
	<style>
		ul {list-style:none; margin:5px; padding:0;font-size:12px;}
		li.header {font-size:18px; font-weight:bold; margin-bottom:5px;}
		img {width:32px; height:32px; margin-right:5px;}
	</style>
</head>
<body style="background:#f1f5f7">
	<c:if test="${name == 'cstnet' }">
		 	
	</c:if>


	<c:choose>
		<c:when test="${name == 'cstnet' }">
			<ul>
		 		<li class="header"><img src="${path}/rtp/images/unit/cstnet_logo.png"/>中国科技网</li>
		 		<li>客服：${content}</li>
		 		<li>类型：${stype }</li>
		 		<li>网址：<a href="${indexURL}" target='_blank'>服务主页</a></li>
		 		<li>电话：${telephone}</li>
		 	</ul>
		</c:when>
		<c:when test="${name == 'guoketu' }">
		 	<ul>
			 	<li class="header">
			 		<img src="${path}/rtp/images/unit/las_logo.png"/>国家科学图书馆
			 	</li>
		 		<li>客服：${content}</li>
		 		<li>类型：${stype }</li>
		 		<li>网址：<a href="${indexURL}" target='_blank'>服务主页</a></li>
		 		<li>电话：${telephone}</li>
		 	</ul>
		</c:when>
		<c:when test="${name == 'public' }">
			<ul>
			 	<li class="header">
			 		<img src="${path}/rtp/images/unit/cstnet_logo.png"/>公共账号
			 	</li>
		 		<li>客服：${content}</li>
		 		<li>类型：${stype }</li>
		 	</ul>
		</c:when>
		<c:otherwise>
			<ul>
		 		<li class="header"><img src="${path}/rtp/images/unit/cstnet_logo.png"/>${name}</li>
		 		<li>客服：${content}</li>
		 		<li>类型：${stype }</li>
		 		<li>网址：<a href="${indexURL}" target='_blank'>服务主页</a></li>
		 		<li>电话：${telephone}</li>
		 	</ul>
		</c:otherwise>
	</c:choose>
</body>
<f:script  src="${path}/resource/js/jquery-1.7.2.min.js"/>
</html>