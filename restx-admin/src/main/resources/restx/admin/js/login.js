'use strict';

angular.module('admin').factory('baseUri', function() {
   return document.location.href.replace(/^https?:\/\/[^\/]+\//, '/').replace(/\/@.*/, '');
});

angular.module('admin').controller('LoginController', function($scope, baseUri, $http) {
    $scope.authenticate = function(username, password) {
        $http.post(baseUri + '/sessions', {principal: {name: username, passwordHash: SparkMD5.hash(password)}})
            .success(function(data, status, headers, config) {
                console.log('authenticated', data, status);
                window.location = $.querystring('backTo') || (baseUri + '/@/ui/');
            }).error(function(data, status, headers, config) {
                console.log('error', data, status);
                alertify.success("Authentication error, please try again.");
            });;
    }
});