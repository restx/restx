angular.module('admin', ['ngResource']);


angular.module('admin').factory('ConfigElements', function($resource) {
    return $resource('../../config/elements');
});

angular.module('admin').controller('ConfigController', function($scope, ConfigElements) {
    $scope.elements = ConfigElements.query();
});
