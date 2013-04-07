angular.module('admin', ['ngResource']);

angular.module('admin').config(function($routeProvider) {
  $routeProvider.
      when('/', {
        controller: 'FactoryController',
        templateUrl: 'views/factory.html'
      }).
      when('/warehouse', {
        controller: 'WarehouseController',
        templateUrl: 'views/warehouse.html'
      });
});
