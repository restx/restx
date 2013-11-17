angular.module('admin').controller('ConfigureLogsController', function($scope, $rootScope, Loggers) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    })

    $scope.levels = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'];
    var levelClasses = {
        TRACE: 'btn-info',
        DEBUG: 'btn-primary',
        INFO: 'btn-success',
        WARN: 'btn-warning',
        ERROR: 'btn-danger'
    };

    $scope.loggers = Loggers.query();
    $scope.changeLevel = function(logger, level) {
        logger.level = level;
        logger.$update(function() {$scope.loggers = Loggers.query();});
    }

    $scope.classFor = function(logger, level) {
        if (logger.level === level) {
            return levelClasses[level];
        } else {
            return 'btn-default';
        }
    }
})