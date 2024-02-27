angular.module('admin').controller('FactoryController', function($scope, $http) {
    $scope.dump = '';

    $http({method: 'GET', url: '../../factory',
        transformResponse: function (r) {
            return r
        }})
        .then(function (response) {
            $scope.dump = response.data;
        })
})