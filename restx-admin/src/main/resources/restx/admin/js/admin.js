'use strict';

angular.module('admin').factory('baseUri', function() {
   return document.location.href.replace(/^https?:\/\/[^\/]+\//, '/').replace(/\/@.*/, '');
});

angular.module('admin').factory('AdminPages', function($resource, baseUri) {
  return $resource(baseUri + '/@/pages');
});

angular.module('admin').controller('AdminController', function($scope, AdminPages) {
    $scope.pages = AdminPages.query();
});