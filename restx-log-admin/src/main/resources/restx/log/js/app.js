angular.module('admin', ['ngResource', 'ngRoute']);

angular.config(function ($routeProvider, $locationProvider) {
    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});

angular.module('admin').config(function($routeProvider) {
  $routeProvider.
      when('/', {
        controller: 'ViewLogsController',
        templateUrl: 'views/view-logs.html'
      }).
      when('/configure', {
        controller: 'ConfigureLogsController',
        templateUrl: 'views/configure-logs.html'
      });
});

angular.module('admin').factory('Loggers', function($resource) {
    return $resource('../../loggers/:name', {name: '@name'}, {
        update: {method:'PUT'}
    });
});

