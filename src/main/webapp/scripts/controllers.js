var projectName = '/' + window.location.pathname.split('/')[1];
var userUrl = {
    'addUrl' : projectName + '/user/add',
    'deleteUrl' : projectName + '/user/delete',
    'updateUrl' : projectName + '/user/update',
    'queryUrl' : projectName + '/user/userList'
};
var user = angular.module('userService', [ 'ngResource' ], angular.noop);
user.controller('userController', function($scope, $resource) {
    var actions = {
        'add' : {
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
    var getUserList = $resource(userUrl.queryUrl, {
        page : 1,
        pageSize : 20
    }, actions);
    getUserList.query({}, function(data) {
        subobj = data;
        $scope.mydata = data;
    });
    var userAdd = $resource(userUrl.addUrl, {
        page : 1,
        pageSize : 20
    }, actions);
    $scope.addUserClick = function() {
        userAdd.add($scope.saveUser, function(data) {
            subobj = data;
            $scope.mydata = data;
        });
    };
    var userUpdate = $resource(userUrl.updateUrl, {
        page : 1,
        pageSize : 20
    }, actions);
    $scope.updateUserClick = function() {
        userUpdate.update($scope.modifyUser, function(data) {
            subobj = data;
            $scope.mydata = data;
        });
    };
    var userDelete = $resource(userUrl.deleteUrl, {
        page : 1,
        pageSize : 20,
        id : ':id'
    }, actions);
    $scope.deleteUser = function(user) {
        userDelete['delete']({
            id : user.id
        }, {}, function(data) {
            subobj = data;
            $scope.mydata = data;
        });
    };
    $scope.updateUser = function(user) {
        $scope.modifyUser = user;
    };
});