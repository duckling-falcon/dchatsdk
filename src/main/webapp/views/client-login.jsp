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
	<title>登录科信</title>
	<f:script  src="${path}/scripts/protcol-laucher.js"/>
	<style>
		.btn-link{text-decoration:none; color:#999;}
		.btn-link:hover{border-bottom:1px solid #999;}
	</style>
</head>
<f:script  src="${path}/resource/js/my_rtp.js"/>
	<%=rtpsvc.refJS()%>
<body style="margin:0;padding:0">
<iframe id="hiddenIframe" src="about:blank" style="display:none;"></iframe>
<a id="hiddenAnchor" style="display:none;">nothing</a>
<h1 style="width:500px; margin:50px auto 0; font-size:24px; text-align:center; font-family:Arial,'微软雅黑';color:#666; font-weight:normal">科信（dChat） - 登录科信</h1>
<div class="container" style="margin:20px auto 0px; padding:10px 30px; width:500px; text-align:center;border:1px solid #ddd; background:#eef; ">
<H1 style="font-size:16px; color:#666; font-family:Arial,'微软雅黑';font-weight:normal; margin-bottom:20px;">点击下面按钮完成登录....</H1>
<p style="margin-bottom:50px;">
<a id="rtp_cmd_btn" 
	rtp_link_uri="rooyee:${loginLink}"
	rtp_sdkuri="/dchat/rtpsvc" 
	rtp_downuri="https://dchat.escience.cn/client/dChatSetup_last.exe" 
	style="display:inline; padding:7px 15px; border-radius:4px; background:#08a; color:#fff; font-size:14px; font-weight:bold;">点我登录</a>
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
<f:script  src="${path}/resource/js/jquery-1.7.2.min.js"/>
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