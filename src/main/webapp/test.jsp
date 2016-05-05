<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	request.setAttribute("path", path);
	
%>
<html>
	<body>
		<h1><%= request.getSession().getId() %></h1>
	</body>
</html>