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
	<f:script  src="${path}/scripts/protcol-laucher.js"/>
	<title>IRC服务</title>
	<style>
		.btn-link{text-decoration:none; color:#999;}
		.btn-link:hover{border-bottom:1px solid #999;}
	</style>
</head>
<body style="margin:0;padding:0">
<iframe id="hiddenIframe" src="about:blank" style="display:none;"></iframe>
<a id="hiddenAnchor" style="display:none;">nothing</a>
<h1 style="width:500px; margin:50px auto 0; font-size:24px; text-align:center; font-family:Arial,'微软雅黑';color:#666; font-weight:normal">科信（dChat） - IRC聊天服务</h1>
<div class="container" style="margin:20px auto 0px; padding:10px 30px; width:500px; text-align:center;border:1px solid #ddd; background:#eef ">
<H1 style="font-size:16px; color:#666; font-family:Arial,'微软雅黑';font-weight:normal; margin-bottom:20px;">群组ID：${targetGroup}</H1>
<p>
<a id="rtp_cmd_btn" 
	rtp_userto="${targetGroup}"
	rtp_link_uri="rooyee:${groupTalkLink}"
	rtp_sdkuri="/dchat/rtpsvc" 
	rtp_downuri="" href="#" >
	<c:choose>
		<c:when test="${targetState eq 'online' }">
			<c:set var="salary" scope="session" value="${2000*2}"/>
		</c:when>
		<c:when test="${targetState eq 'chat' }">
			正在交谈
		</c:when>
		<c:when test="${targetState eq 'away' }">
			暂时离开
		</c:when>
		<c:when test="${targetState eq 'dnd' }">
			请勿打扰
		</c:when>
		<c:when test="${targetState eq 'xa' }">
			长时间离开
		</c:when>
		<c:when test="${targetState eq 'offline' }">
			离线
		</c:when>
	</c:choose>

	<img id="abc" class="rtp_state_img" 
		 border=0 
		 src="${path}/rtp/images/state/${stateImage}"/>	
</a>
</p>
<p style="margin-bottom:50px;">
<a href="https://dchat.escience.cn/client/dChatSetup_last.exe" style="padding:7px 15px; border-radius:4px; background:#0a8; color:#fff; font-size:14px; font-weight:bold; text-decoration:none;">下载PC客户端</a>
</p>
<img alt="" style="width:120px; height:120px; border:1px solid #ddd; border-radius:5px;" src="http://dchat.escience.cn/images/kexin_download.png">
<p style="text-align:center; color:#999; font-size:12px;">扫描二维码，下载移动客户端。</p>

</div>
<div style="background:#fff; width:500px; margin:-1px auto 20px; padding:10px 30px; border:1px solid #ddd; text-align:center;">
<p style="margin:20px 0 20px; font-size:12px;">
	<a class="btn-link" href="http://iask.cstnet.cn/?/home/explore/category-8" target="_blank">如有问题或者建议，请反馈到问答社区</a>
	
</p>
<p style="margin:0px 0 20px; font-size:12px;">
	<a class="btn-link" href="http://dchat.escience.cn/" target="_blank">访问科信主页</a>
</p>
</div>
</body>
<f:script src="${path}/resource/js/my_rtp.js"/>
<f:script src="${path}/resource/js/jquery-1.7.2.min.js"/>
<script type="text/javascript">
$(document).ready(function(){
	$('#rtp_cmd_btn').click(function(){
		my_open_im($("#rtp_cmd_btn").attr('rtp_link_uri'),
				$("#rtp_cmd_btn").attr("rtp_downuri"));
	});
	my_open_im($("#rtp_cmd_btn").attr('rtp_link_uri'),
			$("#rtp_cmd_btn").attr("rtp_downuri"));
});

</script> 
</html>