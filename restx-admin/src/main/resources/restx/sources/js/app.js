angular.module('admin', ['ngResource', 'ui.codemirror']);

angular.module('admin').controller('SourcesController', function($scope, $http, $location) {

    var modes = {
        'js': 'javascript',
        'css': 'css',
        'xml': 'xml',
        'yaml': 'yaml',
        'java': 'clike'
    }

    function url(path) {
        return '../../sources' + path;
    }

    function httpOptions(path) {
        return {
            url: url(path),
            transformResponse: function (r) { return r }
        }
    }


    $scope.save = function() {
        $http(_.extend({method: 'PUT', data: $scope.source}, httpOptions($scope.fileName))).success(function() {
            alertify.success("Saved " + $scope.fileName);
        });
    }


    function load() {
        $scope.fileName = $location.path() || '/';

        var ext = $scope.fileName.substring($scope.fileName.lastIndexOf('.') + 1);
        var dir = $scope.fileName.substring(0, $scope.fileName.lastIndexOf('/') + 1);
        var dirs = dir.split('/').slice(1, -1);
        var parent = dirs.length == 0 ? null : '/' + dirs.slice(0, -1).join('/') + '/';

        $scope.editorOptions = {
            lineWrapping : true,
            lineNumbers: true,
            mode: modes[ext] || 'default'
        };

        $scope.isDirectory = !!$scope.fileName.match(/\/$/);

        if (!$scope.isDirectory) {
            $http(_.extend({method: 'GET'}, httpOptions($scope.fileName))).success(function(data) {
                $scope.source = data;
            });
        }

        if ($scope.fileName === '/') {
            $scope.dirListing = [
                {name: 'main', isDirectory: true, path: '/main/'},
                {name: 'resources', isDirectory: true, path: '/resources/'}
            ];
        } else {
            $http.get(url(dir)).success(function(data) {
                $scope.dirListing = _(data).map(function(f) { return {name: f, isDirectory: !!f.match(/\/$/), path: dir + f}; });
                if (parent) {
                    $scope.dirListing = [{name: '..', isDirectory:true, path: parent}].concat($scope.dirListing);
                }
            });
        }
    }

    load();
    $scope.$on('$locationChangeSuccess', load);
});
