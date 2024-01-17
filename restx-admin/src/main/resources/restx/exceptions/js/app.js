var adminApp = angular.module('admin', ['ngResource']);

adminApp.config(function ($locationProvider) {
    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});

adminApp.factory('ErrorDescriptors', function($resource) {
    return $resource('../../errors/descriptors');
});

adminApp.controller('ErrorDescriptorsController', function($scope, ErrorDescriptors) {
    $scope.errors = ErrorDescriptors.query();
});
