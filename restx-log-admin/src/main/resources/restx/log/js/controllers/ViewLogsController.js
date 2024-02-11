angular.module('admin').controller('ViewLogsController', function($scope, $rootScope, $http) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    })
    $scope.lines = []

    $scope.init = function() {
        $http({method: 'GET', url: '../../logs',
            transformResponse: function (r) {
                return r
            }})
            .then(function (response) {
                var levels = [' TRACE ', ' WARN ', ' ERROR ', ' DEBUG '];
                $scope.lines = _.map(response.data.split('\n'), function(l) {
                    var level = 'INFO';
                    for (var lv in levels) {
                        if (l.indexOf(levels[lv]) !== -1) {
                            level = levels[lv];
                            break;
                        }
                    }
                    return { text: l, level: level};
                });
            })
    }
})