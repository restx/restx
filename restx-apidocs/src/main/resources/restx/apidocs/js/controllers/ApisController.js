'use strict';

adminApp.controller('ApisController', function($rootScope, $scope, $location, ApiDoc, Api) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    })
    $scope.groups = [];

    var defaultSelectedGroups = ($location.search()).groups ? ($location.search()).groups.split(',') : null;
    function isGroupSelectedByDefault(group) {
        return defaultSelectedGroups ? defaultSelectedGroups.indexOf(group) !== -1 : group !== 'restx-admin';
    }

    $scope.doc = ApiDoc.get({}, function() {
        angular.forEach($scope.doc.apis, function(api, key) {
            api.filtered = function() {
              return !_.findWhere($scope.groups, {name: api.group, selected: true})
                  || _.every(api.model.apis, function(opApi) { return opApi.filtered() })
            };
            if (!_.findWhere($scope.groups, {name: api.group})) {
                $scope.groups.push({name: api.group, selected: isGroupSelectedByDefault(api.group)});
            }
            api.model = Api.get({name: api.name}, function(data) {
                data.apis.forEach(function(opApi) {
                    opApi.encodedPath = opApi.path.replace(/\//g, '___');
                    opApi.filtered = function() {
                        return $scope.searchQuery && opApi.path.indexOf($scope.searchQuery) === -1;
                    }
                })
            });
        });
        $scope.$watch('groups', function() {
            var selectedGroups = _.chain($scope.groups)
                .filter(function(g) { return g.selected })
                .pluck('name')
                .value().join(',');
            $location.search({groups: selectedGroups });
        }, true);
    });

    $scope.scrollTo = function(name) {
        const element = document.getElementById(name);
        element.scrollIntoView();
        return false;
    }
});
