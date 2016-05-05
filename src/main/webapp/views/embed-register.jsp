<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="WEB-INF/tld/falcon.tld" prefix="f"%>
<%@ taglib uri="WEB-INF/tld/dchat.tld" prefix="dchat"%>
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
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap.min.css" />
	<f:css href="${path}/resource/thirdparty/bootstrap/css/bootstrap-responsive.min.css" />
	<%=rtpsvc.refJS()%>
</head>
<body>
<h1>dchat客服注册</h1>
<form class="form-horizontal">
  <div class="control-group">
    <label class="control-label" for="inputEmail">客服账号</label>
    <div class="controls">
      <input type="text" id="inputEmail" placeholder="Email">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="inputPassword">单位名称</label>
    <div class="controls">
      <input type="text" id="inputPassword" placeholder="单位名称">
    </div>
  </div>
    <div class="control-group">
    <label class="control-label" for="inputEmail">服务类别</label>
    <div class="controls">
      <label class="radio inline">
	  	<input type="radio" name="optionsRadios" id="optionsRadios1" value="option1" checked>
	 	 5x8小时
	  </label>
	  <label class="radio inline">
	  	<input type="radio" name="optionsRadios" id="optionsRadios2" value="option2">
	    7x24小时
	  </label>
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="inputEmail">服务主页</label>
    <div class="controls">
      <input type="text" id="telephone" placeholder="http://www.demo.com">
    </div>
  </div>
  <div class="control-group">
    <label class="control-label" for="inputEmail">客服电话</label>
    <div class="controls">
      <input type="text" id="telephone" placeholder="010-58811234">
    </div>
  </div>
  <div class="control-group">
    <div class="controls">
      <button type="submit" class="btn btn-primary">Sign in</button>
    </div>
  </div>
</form>
</body>
<f:script  src="${path}/resource/js/jquery-1.7.2.min.js"/>
<script type="text/javascript">
$(document).ready(function(){
	var ajaxRequest = function(requestURL,queryString,callBackFunction){
		$.ajax({
			url:requestURL,
			type: "PUT",
			headers: { 
		            'Accept': 'application/json',
		            'Content-Type': 'application/json' 
		    },
			data: queryString,
	        dataType: "json",
			error: function (xhr, ajaxOptions, thrownError) {
		    },
		    success: function(data){
				callBackFunction(data);
			},
			statusCode:{
				450:function(){alert('会话已过期,请重新登录');},
				403:function(){alert('您没有权限进行该操作');}
			}
	    });
	};
	var params = {
			username:"changeConfig",
			password:"wordd",
			id:"1",
			realname:"hello"
	}
	ajaxRequest("http://127.0.0.1:8080/dchat/rest/user/add?page=1&pageSize=2",
			JSON.stringify(params),function(){
		alert("success");
	});
});
</script>
</html>