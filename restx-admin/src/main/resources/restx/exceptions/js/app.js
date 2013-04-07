angular.module('admin', ['ngResource']);


angular.module('admin').factory('ErrorDescriptors', function($resource) {
    return $resource('../../errors/descriptors');
});

angular.module('admin').controller('ErrorDescriptorsController', function($scope, ErrorDescriptors) {
    $scope.errors = ErrorDescriptors.query();
});
