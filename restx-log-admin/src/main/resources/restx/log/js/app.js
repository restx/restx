angular.module('admin', ['ngResource', 'ngRoute']);

angular.module('admin').config(function($routeProvider, $locationProvider) {
  $routeProvider.
      when('/', {
        controller: 'ViewLogsController',
        templateUrl: 'views/view-logs.html'
      }).
      when('/configure', {
        controller: 'ConfigureLogsController',
        templateUrl: 'views/configure-logs.html'
      });

  // undo the default ('!') to avoid breaking change from angularjs 1.6
  $locationProvider.hashPrefix('');
});

angular.module('admin').factory('Loggers', function($resource) {
    return $resource('../../loggers/:name', {name: '@name'}, {
        update: {method:'PUT'}
    });
});

