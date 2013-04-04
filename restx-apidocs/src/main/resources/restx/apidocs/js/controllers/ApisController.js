'use strict';

adminApp.controller('ApisController', function($rootScope, $scope, ApiDoc, Api) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    })
    $scope.doc = ApiDoc.get({}, function() {
        angular.forEach($scope.doc.apis, function(api, key) {
            api.filtered = function() {
              return _.every(api.model.apis, function(opApi) { return opApi.filtered() })
            };
            api.model = Api.get({name: api.name}, function(data) {
                data.apis.forEach(function(opApi) {
                    opApi.encodedPath = opApi.path.replace(/\//g, '___');
                    opApi.filtered = function() {
                        return $scope.searchQuery && opApi.path.indexOf($scope.searchQuery) === -1;
                    }
                })
            });
        });
    });

    $scope.scrollTo = function(name) {
        $('#' + name).ScrollTo();
        return false;
    }
});
