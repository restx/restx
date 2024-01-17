adminApp = angular.module('admin', ['ngResource', 'ngRoute']);

adminApp
    .config(function($routeProvider) {
        $routeProvider.
            when('/', {
                controller: 'StatsController',
                templateUrl: 'views/stats.html'
            })
        ;
    })
    .factory('RestxStats', function($resource) {
        return $resource('../../restx-stats');
    })
    .controller('StatsController', function($rootScope, $scope, RestxStats) {

        $scope.init = function() {
            $scope.stats = "Loading...";
            RestxStats.get(function(data) {
                $scope.stats = JSON.stringify(data, null, 2);
            });
        }
    })
;