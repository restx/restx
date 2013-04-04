'use strict';

$.ScrollTo.config.duration = 200;
$.ScrollTo.config.offsetTop = 40;

var adminApp = angular.module('admin', ['ngResource']);

adminApp.config(function($routeProvider) {
  $routeProvider.
      when('/', {
        controller: 'ApisController',
        templateUrl: 'views/apis.html'
      }).
      when('/operation/:name/:httpMethod/:path', {
        controller: 'OperationController',
        templateUrl: 'views/operation.html'
      });
});

adminApp.directive('eatClick', function() {
    return function(scope, element, attrs) {
        $(element).click(function(event) {
            event.preventDefault();
        });
    }
});