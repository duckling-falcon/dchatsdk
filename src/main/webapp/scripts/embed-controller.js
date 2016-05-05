
var ecUrl = {
	'create' : domainName + '/ec/create',
	'delete' : domainName + '/ec/delete',
	'update' : domainName + '/ec/update',
	'query' : domainName + '/ec/read'
};

var ec = angular.module('embedConfigService', ['ngResource'], angular.noop);

ec.controller('embedConfigController', function($scope, $resource) {
	var actions = {
		'create' : {
			method : 'PUT',
			isArray : true,
			headers : {
				'Content-Type' : 'application/json'
			}
		},
		'delete' : {
			method : 'DELETE',
			isArray : true
		},
		'query' : {
			method : 'GET',
			isArray : true
		},
		'update' : {
			method : 'POST',
			isArray : true,
			headers : {
				'Content-Type' : 'application/json'
			}
		}
	};
	
	var getConfigList = $resource(ecUrl.query, {
		page : 1,
		pageSize : 20
	}, actions);
	
	getConfigList.query({}, function(data) {
		subobj = data;
		$scope.mydata = data;
	});
	
	var ecCreate = $resource(ecUrl.create, {
		page : 1,
		pageSize : 20
	}, actions);
	
	$scope.createConfigSubmit = function() {
        ecCreate.create($scope.saveConfig, function(data) {
			subobj = data;
			$scope.mydata = data;
		});
        $('#createModal').modal('hide');
	};
	
	var ecUpdate = $resource(ecUrl.update, {
		page : 1,
		pageSize : 20
	}, actions);

    $scope.updateModalShow = function(config) {
        $scope.modifyConfig = config;
    };
	
	$scope.updateConfigSubmit = function() {
		ecUpdate.update($scope.modifyConfig, function(data) {
			subobj = data;
			$scope.mydata = data;
		});
        $('#updateModal').modal('hide');
	};
	
	var ecDelete = $resource(ecUrl.delete, {
		page : 1,
		pageSize : 20,
		id : ':id'
	}, actions);
	
	$scope.deleteModalShow = function(config) {
		$scope.deleteConfig = config;
	};

    $scope.deleteConfigSubmit = function () {
        ecDelete['delete']({
            id: $scope.deleteConfig.id,
            token: "testtoken"
        }, {}, function (data) {
            subobj = data;
            $scope.mydata = data;
        });
        $('#deleteModal').modal('hide');
    };

    $scope.createModalShow = function() {
        $scope.saveConfig = {};
    };


});


