<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	String path = request.getContextPath();
	request.setAttribute("path", path);
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" href="http://www.escience.cn/dface/images/favicon.ico" 
	mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="http://www.escience.cn/dface/images/favicon.ico" 
	mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
<title>重建指定ID的群</title>
</head>
<body>
	<h1>VMT与RTP同步管理</h1>
	<p><font color="red">警告：仅当你清楚知道这些操作对RTP所带来的影响的情况下，你才可以操作。</font></p>
	<div>
		<p>注意事项：</p>
		<ul>
			<li>此处涉及的操作均为直接读取VMT的LDAP数据，然后利用SDK写到RTP后台，强烈建议在晚上操作，因为当数据量很大时LDAP读取操作有可能导致VMT访问缓慢</li>
			<li>当操作失败时，请及时查看SDK Server的日志，并联系SDK Server或RTP开发人员解决</li>
		</ul>
	</div>
	<div>
		<h2>重建某几个群</h2>
		<p>输入VMT中群的vmt-symbol值（多个值以逗号隔开）</p><input type="text" name="groupSymbol" value="">
		<input type="button" id="rebuildGroup" value="执行重建群操作">
		<a href="../index.jsp">返回</a>
	</div>
	<jsp:include page="scripts.jsp"></jsp:include>
	<script type="text/javascript">
	$(document).ready(function(){
		$("#rebuildGroup").click(function(){
			var groupSymbols = $.trim($("input[name=groupSymbol]").val());
			var groups = groupSymbols.split(",");
			if(groupSymbols !=""){
				if(!confirm("你确定重建这些RTP群！？该操作将删除RTP中现有的这些群，然后重新创建！")){
					return;
				}
				$(this).attr("disabled",true);
				$.ajax({
					url:"${path}/v1/sa/sync/group",
					type:"POST",
					data:{"groupSymbols":groups},
					contentType:"application/x-www-form-urlencoded;charset=utf-8",
					success:function(data){
						if(data.status == "success"){
							alert("操作成功！");
						}else{
							alert("操作失败："+data.message);
						}
						$("#rebuildGroup").removeAttr("disabled");
					},
					error:function(xhr,status,error){
						if(xhr.status == 403){
							alert("您无权执行此操作！请联系管理员。");
						}else{
							alert("执行操作【失败】，请检查日志看是否有异常！");
						}
						$("#rebuildGroup").removeAttr("disabled");
					}
				});
			}else{
				alert("群ID不能为空，请参考VMT中群组的vmt-symbol属性填写，多个请以逗号分隔！");
			}
		});
	});
	</script>
</body>
</html>