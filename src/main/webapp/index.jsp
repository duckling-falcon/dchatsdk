<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
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
<title>dChat SDK Server 后台管理操作</title>
</head>
<body>
	<h1>VMT与RTP同步管理</h1>
	<p><font color="red">警告：仅当你清楚知道这些操作对RTP所带来的影响的情况下，你才可以操作。</font></p>
	<div>
		<p>注意事项：</p>
		<ul>
			<li>此处涉及的操作均为直接读取VMT的LDAP数据，然后利用SDK写到RTP后台，强烈建议在晚上操作，因为当数据量很大时LDAP读取操作有可能导致VMT访问缓慢</li>
			<li>当操作失败时，请及时查看SDK Server的日志，并联系SDK Server或RTP开发人员解决</li>
			<li><font color="red">务必确保以下操作进行时，没有人对VMT以及RTP后台进行操作，尽量保证其他应用不会产生MQ消息，以免产生不利影响！</font></li>
		</ul>
	</div>
	<div>
		<table border="1">
			<thead>
				<tr>
					<td width="10%">操作</td>
					<td width="4%">类型</td>
					<td width="45%">描述</td>
					<td width="10%">影响数据范围</td>
					<td width="25%">失败后怎么办</td>
					<td width="6%">你敢点吗？</td>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td>初始化RTP群</td>
					<td>群</td>
					<td>
						<p>该操作将删除现有RTP中的所有群，然后从VMT中读取所有群数据，并写入RTP；若VMT中的群成员不在组织中，则会创建默认用户，该用户被放到默认组织中。
						<font color="red">此操作属于系统初始化操作，对于正在使用的系统请勿使用！</font></p>
					</td>
					<td>
						<p>整个RTP中的群数据</p>
					</td>
					<td>
						<p>可直接再次执行此操作，若两次都执行失败，请查看SDK Server日志并联系开发人员进行处理。</p>
					</td>
					<td>
						<input id="rebuildAllGroup" type="button" value="执行">
					</td>
				</tr>
				<tr>
					<td>重建某几个群</td>
					<td>群</td>
					<td>
						<p>该操作将删除现有RTP中的指定要同步的群，然后从VMT中读取这些群的数据，并写入RTP。
						<font color="red">若群成员不是RTP系统用户将创建默认用户。</font></p>
					</td>
					<td>
						<p>RTP中指定群的数据</p>
					</td>
					<td>
						<p>可直接再次执行此操作，若两次都执行失败，请查看SDK Server日志并联系开发人员进行处理。</p>
					</td>
					<td>
						<a href="views/rebuildGroups.jsp">进入</a>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	<jsp:include page="views/scripts.jsp"></jsp:include>
	<script type="text/javascript">
	$(document).ready(function(){
		$("#rebuildAllGroup").click(function(){
			if(!confirm("你确定重建所有RTP群！？该操作将删除现有RTP系统中的所有群，然后重新创建！")){
				return;
			}
			$(this).attr("disabled",true);
			$.ajax({
				url:'${path}/v1/sa/sync/group/all',
				type:'POST',
				contentType:"application/x-www-form-urlencoded;charset=utf-8",
				success:function(){
					alert("重建群操作【完成】，请检查日志看是否有异常！");
					$("#rebuildAllGroup").removeAttr("disabled");
				},
				error:function(xhr,status,error){
					if(xhr.status == 403){
						alert("您无权执行此操作！请联系管理员。");
					}else{
						alert("重建群操作【失败】，请检查日志看是否有异常！");
					}
					$("#rebuildAllGroup").removeAttr("disabled");
				}
			});
		});
	});
	</script>
</body>
</html>