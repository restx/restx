'use strict';

angular.module('admin').factory('baseUri', function() {
   return location.href.replace(/^https?:\/\/[^\/]+\//, '/').replace(/\/@.*/, '');
});

angular.module('admin').factory('AdminPages', function($resource, baseUri) {
  return $resource(baseUri + '/@/pages');
});

angular.module('admin').factory('Sessions', function($resource, baseUri) {
  return $resource(baseUri + '/sessions/:sessionKey', {sessionKey: 'current'});
});

angular.module('admin').controller('AdminController', function($scope, $http, baseUri, AdminPages, Sessions) {
    $scope.pages = AdminPages.query();

    $scope.session = Sessions.get()

    $scope.logout = function() {
        Sessions.delete(function() { location.reload() });
    }
});

angular.module('admin').config(function($httpProvider) {
    $httpProvider.interceptors.push(function($q, baseUri) {
        return {
            response: function(response) {
                return response;
            },
            responseError: function(response) {
                if (response.status === 401 || response.status === 403) {
                    // onSecurityException should be loaded by /@/ui/js/securityHandling.js
                    var backTo = location;
                    if (window.onSecurityException) {
                        window.onSecurityException(baseUri, response, backTo);
                    } else {
                        // default implementation
                        if (response.config.headers && response.config.headers.RestxSu
                            && (response.status === 401 || response.status === 403)) {
                            // do nothing, the forbidden was sent while sudoing, probably in api docs
                        } else {
                            window.location = baseUri + '/@/ui/login.html?backTo=' + backTo;
                        }
                    }
                }
                return $q.reject(response);
            }
        };
    });
});