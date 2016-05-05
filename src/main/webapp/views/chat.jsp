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
		span.imgContent {}
	</style>
</head>
<body style="background:#f1f5f7">
	 <fieldset>
	 	<legend><img alt="" src="${path}/rtp/images/unit/cstnet_logo.png">中国科技网</legend>
	 	<ul>
	 		<li>
	 			<span>网络运行：</span>
	 			<span class="imgContent">
	 			<c:choose>
	 				<c:when test="${profile == 'dev' }">
		 				<dchat:pubacc account="liuyudetest@cstnet.cn" 
			 				params="name=cstnet&content=网络运行&stype=7x24小时&telephone=010-58812000&indexURL=http://iask.cstnet.cn/?/home/explore/category-12"/>
	 				</c:when>
	 				<c:when test="${profile == 'test' }">
		 				<dchat:pubacc account="liuyudetest@cstnet.cn" 
			 				params="name=cstnet&content=网络运行&stype=7x24小时&telephone=010-58812000&indexURL=http://iask.cstnet.cn/?/home/explore/category-12"/>
	 				</c:when>
	 				<c:when test="${profile == 'product' }">
			 			<dchat:pubacc account="58812000@cstnet.cn" 
			 				params="name=cstnet&content=网络运行&stype=7x24小时&telephone=010-58812000&indexURL=http://iask.cstnet.cn/?/home/explore/category-12"/>
	 				</c:when>
	 			</c:choose>
	 			</span>
	 			<span class="time">7x24小时</span>
	 		</li>
	 		<li>
	 			<span>邮箱服务：</span>
	 			
	 			<span class="imgContent">
	 			<c:choose>
	 				<c:when test="${profile == 'dev' }">
	 					<dchat:pubacc account="liuyudetest@cstnet.cn" 
			 				params="name=cstnet&content=邮箱服务&stype=5x8小时&telephone=010-58812857&indexURL=http://iask.cstnet.cn/?/home/explore/category-1"/>
	 				</c:when>
	 				<c:when test="${profile == 'test' }">
		 				<dchat:pubacc account="liuyudetest@cstnet.cn" 
			 				params="name=cstnet&content=邮箱服务&stype=5x8小时&telephone=010-58812857&indexURL=http://iask.cstnet.cn/?/home/explore/category-1"/>
	 				</c:when>
	 				<c:when test="${profile == 'product' }">
			 			<dchat:pubacc account="jiangting@cstnet.cn" 
			 				params="name=cstnet&content=邮箱服务&stype=5x8小时&telephone=010-58812857&indexURL=http://iask.cstnet.cn/?/home/explore/category-1"/>
	 				</c:when>
	 			</c:choose>
	 			</span>
	 			<span class="time">5x8小时</span>
	 		</li>
	 		<li>
	 			<span>科信服务：</span>
	 			<span class="imgContent">
	 			<c:choose>
	 				<c:when test="${profile == 'dev' }">
	 					<dchat:pubacc account="yangxuan@cstnet.cn" 
			 				params="name=cstnet&content=科信服务&stype=5x8小时&telephone=010-58812858&indexURL=http://iask.cstnet.cn/?/home/explore/category-8"/>
	 				</c:when>
	 				<c:when test="${profile == 'test' }">
		 				<dchat:pubacc account="yangxuan@cstnet.cn" 
			 				params="name=cstnet&content=科信服务&stype=5x8小时&telephone=010-58812858&indexURL=http://iask.cstnet.cn/?/home/explore/category-8"/>
	 				</c:when>
	 				<c:when test="${profile == 'product' }">
		 				<dchat:pubacc account="liuyude@cstnet.cn" 
			 				params="name=cstnet&content=科信服务&stype=5x8小时&telephone=010-58812858&indexURL=http://iask.cstnet.cn/?/home/explore/category-8"/>
	 				</c:when>
	 			</c:choose>
	 			</span>
	 			<span class="time">5x8小时</span>
	 		</li>
	 	</ul>
	 </fieldset>
	 <fieldset>
	 	<legend><img alt="" src="${path}/rtp/images/unit/las_logo.png">国家科学图书馆</legend>
	 	<ul>
	 		<li>
	 			<span>系统咨询：</span>
	 			<span class="imgContent">
	 			<c:choose>
	 				<c:when test="${profile == 'dev' }">
	 					<dchat:pubacc account="wydtest@cstnet.cn" 
	 						params="name=guoketu&content=系统咨询&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 				<c:when test="${profile == 'test' }">
		 				<dchat:pubacc account="wydtest@cstnet.cn" 
		 					params="name=guoketu&content=系统咨询&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 				<c:when test="${profile == 'product' }">
			 			<dchat:pubacc account="it@mail.las.ac.cn" 
			 				params="name=guoketu&content=系统咨询&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 			</c:choose>
	 			</span>
	 			<span class="time">5x8小时</span>
	 		</li>
	 		<li>
	 			<span>科技监测：</span>
	 			<span class="imgContent">
	 			<c:choose>
	 				<c:when test="${profile == 'dev' }">
	 					<dchat:pubacc account="liji@cstnet.cn" 
	 						params="name=guoketu&content=科技监测&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 				<c:when test="${profile == 'test' }">
		 				<dchat:pubacc account="liji@cstnet.cn" 
		 					params="name=guoketu&content=科技监测&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 				<c:when test="${profile == 'product' }">
			 			<dchat:pubacc account="automonitoralter@mail.las.ac.cn" 
			 				params="name=guoketu&content=科技监测&stype=5x8小时&telephone=010-82626611&indexURL=http://www.las.ac.cn"/>
	 				</c:when>
	 			</c:choose>
	 			</span>
	 			<span class="time">5x8小时</span>
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