var adminApp = angular.module('admin', ['ngResource']);

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