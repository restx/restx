'use strict';

adminApp.controller('HomeController', function($rootScope, $scope, TestResultSummary, TestRequest) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    });
    $scope.testResultSummaries = TestResultSummary.query({});

    $scope.launchAll = function() {
        new TestRequest({test: "specs/*"}).$save();
    }

    var intervalId = setInterval(function() {
        // NOTE: this is not executed in angular apply, but this is fine, we want to update only when we get the data from http request
        $scope.testResultSummaries = TestResultSummary.query({});
    }, 1000);

    $scope.$on('$destroy', function(){
        clearInterval(intervalId);
    });
});

adminApp.filter('success', function() {
  return function(data) {
    return _.filter(data, function(i) { return i.status === 'SUCCESS' });
  };
}).filter('failure', function() {
  return function(data) {
    return _.filter(data, function(i) { return i.status === 'FAILURE' });
  };
}).filter('error', function() {
  return function(data) {
    return _.filter(data, function(i) { return i.status === 'ERROR' });
  };
}).filter('count', function() {
  return function(data) {
    return data.length;
  };
}).filter('toClassStatus', function() {
  return function(status) {
    return 'label-' + {'SUCCESS': 'success', 'ERROR': 'important', 'FAILURE': 'warning'}[status];
  };
});
