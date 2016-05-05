<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.rooyeetone.rtp.sdk.*"%>

<%
	String path = request.getContextPath();
	request.setAttribute("path", path);
	IRtpSvc rtpsvc = RtpSvc.getInstance(request);
	rtpsvc.exec("IMLogin", "liji@cstnet.cn");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="embedConfigService">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>科信公共服务配置</title>
    <link rel="icon" href="http://www.escience.cn/dface/images/favicon.ico" 
	mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
	<link rel="shortcut icon" href="http://www.escience.cn/dface/images/favicon.ico" 
		mce_href="http://www.escience.cn/dface/images/favicon.ico" type="image/x-icon">
    <link href="${path}/resource/thirdparty/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="${path}/scripts/jquery-1.10.2.min.js"></script>
    <script type="text/javascript" src="${path}/scripts/angular.min.js"></script>
    <script type="text/javascript" src="${path}/scripts/angular-resource.min.js"></script>
    <script type="text/javascript">
    	var domainName = '${domain}/dchat/rest';
    </script>
    <script type="text/javascript" src="${path}/scripts/embed-controller.js"></script>
    <script type="text/javascript" src="${path}/resource/thirdparty/bootstrap/js/bootstrap.min.js"></script>
    <style>
    	p.newSetting {text-align:right; margin:2em 0 1.5em 0}
    	label.radio-inline {display:inline-block; margin-top:5px;}
    	label.radio-inline input {margin-bottom:5px;}
    	.form-horizontal .control-group {margin-bottom:10px;}
    	ul.deleteSetting {list-style:none}
    	ul.deleteSetting li {padding:5px;}
    </style>
</head>
<body ng-controller="embedConfigController">
<div class="container">
    <p class="newSetting"><button class="btn btn-primary btn-lg" data-toggle="modal" data-target="#createModal" ng-click="createModalShow()">
        新建配置
    </button></p>
    <div id="configList">
        <table class="table">
            <tr>
                <th>序号</th>
                <th>id</th>
                <th>单位名称</th>
                <th>服务账号</th>
                <th>服务内容</th>
                <th>服务类型</th>
                <th>服务主页</th>
                <th>服务电话</th>
                <th>操作</th>
            </tr>
            <tr ng-repeat="conf in mydata" ng-class-even="'even'" ng-class-odd="'odd'">
                <td>{{$index + 1}}</td>
                <td>{{conf.id}}</td>
                <td>{{conf.unitName}}</td>
                <td>{{conf.account}}</td>
                <td>{{conf.displayName}}</td>
                <td>{{conf.duration}}</td>
                <td>{{conf.indexURL}}</td>
                <td>{{conf.telephone}}</td>
                <td>
                    <button class="btn btn-primary btn-lg" data-toggle="modal" ng-click="updateModalShow(conf)" data-target="#updateModal">
                        	修改配置
                    </button>&nbsp;
                    <button class="btn"  data-toggle="modal"  ng-click="deleteModalShow(conf)" data-target="#deleteModal">删除配置</button>
                </td>
            </tr>
        </table>
    </div>
</div>



<!-- Create Config Modal -->
<div class="modal fade hide" id="createModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">创建新配置</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" role="form" name="createConfigForm">
                    <div class="control-group">
                        <label for="inputAccount" class="col-sm-2 control-label">服务账号</label>
                        <div class="controls">
                            <input type="text"
                                   class="form-control"
                                   id="inputAccount"
                                   placeholder="Account"
                                   required
                                   ng-minlength="3"
                                   name="config.account"
                                   ng-model="saveConfig.account">
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputUnitName" class="col-sm-2 control-label">单位名称</label>
                        <div class="controls">
                            <input type="text"
                                   class="form-control"
                                   id="inputUnitName"
                                   required
                                   ng-minlength="3"
                                   placeholder="UnitName"
                                   name="config.unitName"
                                   ng-model="saveConfig.unitName"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputName" class="col-sm-2 control-label">服务内容</label>
                        <div class="controls">
                            <input type="text"
                                   class="form-control"
                                   id="inputName"
                                   required
                                   ng-minlength="3"
                                   placeholder="Name"
                                   name="config.displayName"
                                   ng-model="saveConfig.displayName"/>
                        </div>
                    </div>
                    <div class="control-group">
                            <label for="inputDuration1" class="col-sm-2 control-label">服务类型</label>
                            <div class="controls">
                                <label class="radio-inline">
                                    <input type="radio" id="inputDuration1" name="config.duration" value="7x24" ng-model="saveConfig.duration" > 7x24小时
                                </label>
                                <label class="radio-inline">
                                    <input type="radio" id="inputDuration2"  name="config.duration" value="5x8" ng-model="saveConfig.duration" > 5x8小时
                                </label>
                            </div>
                    </div>
                    <div class="control-group">
                        <label for="inputURL" class="col-sm-2 control-label">服务主页</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputURL" placeholder="IndexURL" name="config.indexURL" ng-model="saveConfig.indexURL" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputTelephone" class="col-sm-2 control-label">服务电话</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputTelephone" placeholder="Telephone" name="config.telephone" ng-model="saveConfig.telephone"/>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal" aria-hidden="true">关闭</button>
                <button type="submit" class="btn btn-primary" ng-disabled="createConfigForm.$invalid" ng-click="createConfigSubmit()">提交</button>
            </div>
        </div>
    </div>
</div>


<!-- Update Config Modal -->
<div class="modal fade hide" id="updateModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="updateModalLabel">更新配置</h4>
            </div>
            <div id="update" class="modal-body">
                <form class="form-horizontal" role="form">
                    <div class="control-group">
                        <label for="inputAccount2" class="col-sm-2 control-label">服务账号</label>
                        <div class="controls">
                            <input type="hidden" class="form-control" id="hiddenId" name="config.id" ng-model="modifyConfig.id" />
                            <input type="text" class="form-control" id="inputAccount2" name="config.account" ng-model="modifyConfig.account" />
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputName2" class="col-sm-2 control-label">服务内容</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputName2" placeholder="Name" name="config.displayName" ng-model="modifyConfig.displayName"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputUnitName2" class="col-sm-2 control-label">单位名称</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputUnitName2" placeholder="UnitName" name="config.unitName" ng-model="modifyConfig.unitName"/>
                        </div>
                    </div>
                    <div class="control-group">
                            <label class="col-sm-2 control-label">服务类型</label>
                            <div class="controls">
                                <label class="radio-inline">
                                    <input type="radio" id="inputDuration11" name="config.duration" value="7x24" ng-model="modifyConfig.duration" > 7x24小时
                                </label>
                                <label class="radio-inline">
                                    <input type="radio" id="inputDuration12" name="config.duration" value="5x8" ng-model="modifyConfig.duration" > 5x8小时
                                </label>
                            </div>
                    </div>
                    <div class="control-group">
                        <label for="inputURL2" class="col-sm-2 control-label">服务主页</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputURL2" placeholder="IndexURL" name="config.indexURL" ng-model="modifyConfig.indexURL"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label for="inputTelephone2" class="col-sm-2 control-label">服务电话</label>
                        <div class="controls">
                            <input type="text" class="form-control" id="inputTelephone2" placeholder="Telephone" name="config.telephone" ng-model="modifyConfig.telephone"/>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button type="button" class="btn btn-primary" ng-click="updateConfigSubmit()">提交</button>
            </div>
        </div>
    </div>
</div>

<!-- Delete Config Modal -->
<div class="modal fade hide" id="deleteModal" tabindex="-1" role="dialog" aria-labelledby="deleteLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="deleteLabel">删除配置</h4>
            </div>
            <div class="modal-body">
                <h5>确定删除此条配置?</h5>
                <ul class="deleteSetting">
                    <li>ID:{{deleteConfig.id}}</li>
                    <li>服务账号:{{deleteConfig.account}}</li>
                    <li>单位名称:{{deleteConfig.unitName}}</li>
                    <li>服务内容:{{deleteConfig.displayName}}</li>
                    <li>服务类型:{{deleteConfig.duration}}</li>
                    <li>服务主页:{{deleteConfig.indexURL}}</li>
                    <li>服务电话:{{deleteConfig.telephone}}</li>
                </ul>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button type="submit" class="btn btn-danger" ng-click="deleteConfigSubmit()">删除</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>