let factoryAdminApp = angular.module('admin', ['ngResource', 'ngRoute']);

factoryAdminApp.config(function($routeProvider, $locationProvider) {
  $routeProvider.
      when('/', {
        controller: 'FactoryController',
        templateUrl: 'views/factory.html'
      }).
      when('/warehouse', {
        controller: 'WarehouseController',
        templateUrl: 'views/warehouse.html'
      });

    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});
