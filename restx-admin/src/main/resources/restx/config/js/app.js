let adminApp = angular.module('admin', ['ngResource']);

adminApp.config(function ($locationProvider) {
    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});

adminApp.factory('ConfigElements', function($resource) {
    return $resource('../../config/elements');
});

adminApp.controller('ConfigController', function($scope, ConfigElements) {
    $scope.elements = ConfigElements.query();
});
