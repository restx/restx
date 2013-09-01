'use strict';

adminApp.controller('TestResultController', function($rootScope, $scope, $routeParams, TestResult) {
    $scope.testResult = TestResult.get({key: $routeParams.testResultKey});
});
