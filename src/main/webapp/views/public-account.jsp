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
	String[] array = new String[10];
	int index = 0;
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
	<f:script  src="${path}/scripts/protcol-laucher.js"/>
	<%=rtpsvc.refJS()%>
	<style>
		legend {font-size:18px; font-weight:bold; border-bottom:1px solid #ddd; padding-bottom:0px;}
		legend img {width:32px; height:32px; margin-right:5px;}
		ul {list-style:none; margin:-15px 0px 10px 25px; font-size:12px;}
		ul li {padding:5px; margin:0}
		ul li span {font-size:14px;}
	</style>
</head>
<body style="background:#f1f5f7">
	 <fieldset>
	 	<legend>公共账号</legend>
	 	<ul>
	 		<li>
	 			<span>订阅会议室<dchat:pubacc account="dbookingtest@openfire.escience.cn" 
	 				params="name=public&content=会议小助手&stype=7x24小时"
	 				outside="true"/>
	 			</span>
	 		</li>
	 	</ul>
	 </fieldset>

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