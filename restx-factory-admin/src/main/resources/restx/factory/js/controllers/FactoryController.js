angular.module('admin').controller('FactoryController', function($scope, $http) {
    $scope.dump = '';

    $http({method: 'GET', url: '../../factory',
        transformResponse: function (r) {
            return r
        }})
        .success(function (data) {
            $scope.dump = data;
        })
})