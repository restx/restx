'use strict';

adminApp.controller('EntityController', function EntityController(
        $rootScope, $scope, $routeParams, $http) {
    $scope.fqcn = $routeParams.fqcn;
    $http.get('../../api-docs/schemas/' + $scope.fqcn).then(function(response) {
        $scope.schema = response.schema;
    });
});
