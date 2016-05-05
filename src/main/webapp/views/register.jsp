<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> 
<%@ taglib uri="WEB-INF/tld/falcon.tld" prefix="f"%>
<%
	String path = request.getContextPath();
	request.setAttribute("context", path);
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<f:css href="${context}/resource/dchat.css" />
	<f:css href="${context}/resource/thirdparty/bootstrap/css/bootstrap.min.css" />
	<f:css href="${context}/resource/thirdparty/bootstrap/css/bootstrap-responsive.min.css" />
</head>
<body style="background:#f1f5f7">
	<div class="login-left span8">
		<form class="form-horizontal" id="activateForm" action="/dchat/v1/register?func=activate" method="post">
			<h2 class="login-title">
				激活<span class="mail-title"> 	
					使用中国科技网通行证（已开通单位的用户无需激活）</span>
				</h2>
			
			<div class="control-group">
				<label class="control-label" for="username">
					账号<input type="hidden" value="Validate" name="act">
				</label>
				<div class="controls">
					<input id="username" type="text" placeholder="中国科技网通行证" 
						message="请输入邮箱地址" maxlength="355" name="username" value="${username}">
					<span id="username_error_place" class="error help-inline">
						 <c:if test="${status == 'error'}">
						 	${message}
						 </c:if>
					</span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="password">密码</label>
				<div class="controls">
					<input id="password" type="password" maxlength="355" class="logininput valid" name="password" placeholder="密码">
					<span id="password_error_place" class="error help-inline"></span>
					<span class="help-block text-quote" id="passwordHint">请输入中国科技网通行证密码</span>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="checkCode">验证码</label>
				<div class="controls">
					<input type=text name="checkcode" id="checkcode" value=""/>
					<c:choose>
						<c:when test="${not empty checkError }">
							<span id="checkcode_error_place" class="error help-inline">${checkError}</span>
						</c:when>
						<c:otherwise>
							<span id="checkcode_error_place" class="error help-inline"></span>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			<div class="control-group">
				<div class="controls">
					<img id="checkCodePiceture" style="width:100px; height:50px;" src="${context}/v1/pcode?func=getImage&type=registType">
					<a href="#" id="changeImage">换一张</a>
				</div>
			</div>
			
			<div class="control-group">
				<div class="controls">
					<button type="button" id="submitBtn" class="btn btn-large btn-primary long">激活</button>
						<a target="_blank" href="/findPsw.do?act=stepOne" class="small_link forgetpsw small-font">忘记密码？</a>
				</div>
			</div>
		</form>
	</div>
			
	<div class="login-right span4">
		<p class="header">
			没有中国科技网通行证？
			<br></br>
			 <a href="${umtRegistURL}" target="_blank">
			   <span class="btn btn-success">立即注册</span>
			 </a>
		</p>
		<h3>什么是中国科技网通行证？</h3>
		<p class="sub-gray-text">中国科技网通行证是基于中国科技网的统一账号系统，可以用于登录各类科研应用服务，包括：<a href="http://www.escience.cn" target="_blank">科研在线</a>、<a href="http://ddl.escience.cn" target="_blank">团队文档库</a>、<a href="http://csp.escience.cn" target="_blank">会议服务平台</a>、<a href="http://www.escience.cn/people" target="_blank">科研主页</a>、<a href="http://mail.escience.cn" target="_blank">中科院邮件系统</a>等，以及今后将逐步扩展的更多应用服务。</p>
		<p><strong>原<span class="duckling-logo"></span>Duckling通行证升级为中国科技网通行证。</strong></p>
		<p><strong>中科院邮件系统账号可作为中国科技网通行证账号直接登录。</strong></p>
		
	</div>
	<div class="clear"></div>
</body>

<f:script  src="${context}/resource/js/jquery-1.7.2.min.js"/>
<f:script  src="${context}/resource/js/jquery.validate.min.js"/>
<f:script  src="${context}/resource/thirdparty/bootstrap/js/bootstrap.min.js"/>
<script type="text/javascript">
$(document).ready(function(){
	var picCheckUrl = "${context}/v1/pcode";
										 
	$('#submitBtn').on('click',function(){
		var $form=$('#activateForm');
		if($form.valid()){
			$form.submit();
		}
	});
	$("#changeImage").click(function(){		
		$("#checkCodePiceture").attr("src", picCheckUrl+"?func=getImage&type=registType"+"&&rand="+Math.random());
	});
	
	function resetErrorStatus(){
		$("#checkcode_error_place").html("");
		$("#checkcode").removeClass("error");
		$("#password_error_place").html("");
		$("#password").removeClass("error");
	}
	
	$("#activateForm").validate({
		submitHandler:function(form){
			$.ajax({
				url: "${context}/v1/register?func=validate",
				type:"POST",
				dataType:"json", 
				data:{
					'username':$("#username").val(),
					'password':$("#password").val(),
					'type':'registType',
					'checkcode':$("#checkcode").val()
				}, 
				error:function(){},
				success:function(data) {
					if(data.status=="success"){
						form.submit();
					}else if (data.status == "checkCodeError"){
						resetErrorStatus();
						$("#checkcode").addClass("error");
						$("#checkcode_error_place").html("验证码错误,请重新输入!");
						$("#checkCodePiceture").attr("src",picCheckUrl+"?func=getImage&type=registType"+"&&rand="+Math.random());
						return ;
					} else if (data.status=="passwordError"){
						resetErrorStatus();
						$("#password").addClass("error");
						$("#password_error_place").html("您输入的帐号或密码错误!");
						$("#checkCodePiceture").attr("src",picCheckUrl+"?func=getImage&type=registType"+"&&rand="+Math.random());
						return ;
					}
				},
				statusCode:{
					450:function(){alert('会话已过期,请重新登录');},
					403:function(){alert('您没有权限进行该操作');}
				}
			});
		},
		rules:{
			username:{
				required:true
			},
			password:{
				required:true
			},
			checkcode:{
				required:true
			}
		},
		messages:{
			username:{required:'请输入用户名'},
			password:{required:'请输入密码'},
			checkcode:{required:"请输入验证码"}
		},
		errorPlacement: function(error, element){
			var sub="_error_place";
			var errorPlaceId="#"+$(element).attr("name")+sub;
			$(errorPlaceId).html("");
			error.appendTo($(errorPlaceId));
		}
	});
	
	$.fn.extend({ 
		enter: function (callBack) {
		    $(this).keydown(function(event){
		    	if(event.keyCode=='13'){
		    		callBack.apply(event.currentTarget,arguments);
		    		event.preventDefault();
					event.stopPropagation();
		    	}
		    });
		}
	});
	
	$('input').enter(function(){
		$( "#submitBtn" ).trigger( "click" );
	});
	
});
</script>
</html>