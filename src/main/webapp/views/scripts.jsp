<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	request.setAttribute("path", path);
%>
<script type="text/javascript" src="${path}/scripts/jquery-1.10.2.min.js"></script>