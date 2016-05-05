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
	<style>
		table.ui-table-form th {text-align:right;}
		table.ui-table-form th,table.ui-table-form td {vertical-align:middle;}
		table.ui-table-form ul li {float:left; margin-right:20px;}
		p.ui-text-note {font-size:12px; color:#999; margin:-5px 0 0 10px}
	</style>
</head>
<body style="background:#f1f5f7">
	<div class="config-float">
		<form name="team-info" action="#">
			<input type="hidden" value="${token}" name="token">
			<table class="ui-table-form" style="margin:20px auto">
			<tbody>
				<tr><th>新邮件提醒：</th>
					<td>
						<ul style="list-style:none; padding:0; margin:0;">
							<li>
								<input type="radio" name="switchNotice" value="0" 
									<c:if test="${switchNotice eq '0' }"> checked="checked"</c:if>/>关闭
							</li>
							<li>
								<input type="radio" name="switchNotice" value="1" 
									<c:if test="${switchNotice eq '1' }"> checked="checked"</c:if>/>打开
							</li>
						</ul>
					</td>
				</tr>
				<tr><th>发件人过滤：</th>
					<td>
						<ul style="list-style:none; padding:0; margin:0;">
							<li>
								<input type="text" name="filterRule" style="width:110px;height:20px; overflow:hidden; margin-top:8px;" value="${filterRule}"  />
							</li>
						</ul>
						
					</td>
				</tr>
				<tr>
					<td colSpan="2"><p class="ui-text-note">以逗号分隔，如：a@a.cn,@b.cn</p></td>
				<tr>
					<td colSpan="2" class="largeButtonHolder" style="text-align:center; padding-top:0.8em;">
						<input type="button" id="saveBasicButton" class="btn-success" value="保存更改"/>
					</td>
				</tr>
				<tr>
					<td colSpan="2" style="text-align:center; padding-top:0.8em;"><span class="ui-spotLight" id="updateAll-spotLight"></span></td>
				</tr>
			</tbody>
			</table>
		</form>
	</div>
<div class="clear"></div>
</body>

<f:script  src="${context}/resource/js/jquery-1.7.2.min.js"/>
<f:script  src="${context}/resource/js/jquery.validate.min.js"/>
<f:script  src="${context}/resource/thirdparty/bootstrap/js/bootstrap.min.js"/>
<script type="text/javascript">
$(document).ready(function(){
	var ajaxRequest = function(requestURL,queryString,callBackFunction){
		$.ajax({
			url:requestURL,
			type: "POST",
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
	function ui_spotLight(obj, stat, text, fade) {
		if (typeof(obj)=='object')
			var spotLight = obj;
		else if (typeof(obj)=='string')
			var spotLight = document.getElementById(obj);
		$(spotLight).removeClass('ui-spotLight-success')
			.removeClass('ui-spotLight-fail')
			.removeClass('ui-spotLight-processing');
		
		switch(stat) {
		case 'processing': {
			$(spotLight).addClass('ui-spotLight-processing');
			$(spotLight).html(text);
			$(spotLight).show();
			break;
		}
		case 'success': {
			$(spotLight).addClass('ui-spotLight-success');
			$(spotLight).html(text);
			$(spotLight).show();
			if (fade=='fade')
				$(spotLight).delay(1000).fadeOut();
			break;
		}
		case 'fail': {
			$(spotLight).addClass('ui-spotLight-fail');
			$(spotLight).html(text);
			$(spotLight).show();
			if (fade=='fade')
				$(spotLight).delay(2000).fadeOut();
			break;
		}
		case 'show': {
			$(spotLight).slideDown();
			break;
		}
		case 'hide': {
			$(spotLight).fadeOut();
			break;
		}
		}//end of switch
	}
	
	$('#saveBasicButton').click(function(){
		var url = "${dchatDomain}/dchat/v1/preferences?func=changeConfig";
		var switchNoticeVal;
		$("input[type=radio][name=switchNotice]").each(function(){
			if($(this).attr('checked')){
				switchNoticeVal = $(this).val();
				return;
			}
		});
		var params = {
				func:"changeConfig",
				switchNotice:switchNoticeVal+"",
				filterRule:$("input[name=filterRule]").val()
		}
		ajaxRequest(url,params,function(data){
			if(data&&data.success!=undefined&&data.success==false){
				alert(data.message);				
			}else{
				ui_spotLight('updateAll-spotLight', 'success', '当前设置已保存', 'fade');
			}
		});
	});
	
});
</script>
</html>