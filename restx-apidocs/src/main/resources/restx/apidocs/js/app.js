'use strict';

$.ScrollTo.config.duration = 200;
$.ScrollTo.config.offsetTop = 40;

var adminApp = angular.module('admin', ['ngResource']);

adminApp.config(function($routeProvider) {
  $routeProvider.
      when('/', {
        controller: 'ApisController',
        templateUrl: 'views/apis.html',
        reloadOnSearch: false
      })
      .when('/operation/:name/:httpMethod/:path', {
        controller: 'OperationController',
        templateUrl: 'views/operation.html'
      })
      .when('/entity/:fqcn', {
        controller: 'EntityController',
        templateUrl: 'views/entity.html'
      })
  ;
});

adminApp.directive('eatClick', function() {
        return function(scope, element, attrs) {
            $(element).click(function(event) {
                event.preventDefault();
            });
        }
    }).filter('simpleName', function() {
        return function(input) {
            return input && input
                .replace(/#.+/, '')
                .replace(/\$.+/, '')
                .replace(/.+\./, '')
        };
    })
    .filter('sourceLink', function() {
        return function(input) {
            return input && ('../sources/#/main/'
                + input
                .replace(/#.+/, '')
                .replace(/\$.+/, '') // inner class, remove everything after the dollar
                .replace(/\./g, '/')
                + '.java');
        };
    })
;