var adminApp = angular.module('admin', ['ngResource', 'ngRoute']);

adminApp.config(function ($routeProvider, $locationProvider) {
    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});

adminApp.config(function($routeProvider) {
  $routeProvider
      .when('/', {
        controller: 'HomeController',
        templateUrl: 'views/home.html'
      })
      .when('/testResult/:testResultKey', {
              controller: 'TestResultController',
              templateUrl: 'views/testResult.html'
            })
      ;
});