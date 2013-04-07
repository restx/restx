'use strict';

adminApp.controller('OperationController', function OperationController($scope, $routeParams, $http, ApiDoc, Api) {
    var path = $routeParams.path.replace(/___/g, '/');
    $scope.doc = ApiDoc.get();
    $scope.try = false;
    $scope.tryButtonLabel = 'Try it out';

    function setTry(tryMode) {
        $scope.try = tryMode;
        $scope.tryButtonLabel = $scope.try ? 'Hide try' : 'Try it out!';
        if (!$scope.try) {
            $scope.request = undefined;
        }
    }

    $scope.toggleTry = function() {
        setTry(!$scope.try);
    }

    function bindParams(path) {
        var queryParams = [];
        _.each($scope.operation.parameters, function(p) {
            if (p.paramType == 'path') {
                path = path.replace('{' + p.name + '}', encodeURIComponent(p.value));
            } else if (p.paramType == 'query') {
                queryParams.push(encodeURIComponent(p.name) + '=' + encodeURIComponent(p.value));
            }
        });
        if (queryParams.length) {
            path = path + '?' + queryParams.join('&');
        }
        return path;
    }

    function bodyParamValue(value) {
        var bodyParam = _.find($scope.operation.parameters, function(p) { return p.paramType === 'body' });
        if (value) {
            if (bodyParam) bodyParam.value = value;
        } else {
            return bodyParam ? bodyParam.value : '';
        }
    }

    function sendRequest() {
        $http({method: $scope.request.httpMethod, url: $scope.doc.basePath + $scope.request.path, data: $scope.request.body,
            transformResponse: function (r) {
                return r
            }})
            .success(function (data, status, headers, config) {
                $scope.request.response.status = status;
                $scope.request.response.body = data;
            }).
            error(function (data, status, headers, config) {
                $scope.request.response.status = status;
                $scope.request.response.body = data;
            });
    }

    $scope.send = function() {
        $scope.request = {
            httpMethod: $scope.operation.httpMethod,
            path: bindParams($scope.opApi.path),
            body: bodyParamValue(),
            response: { status: '', body: '' }
        };
        sendRequest();
    }

    $scope.api = Api.get({name: $routeParams.name}, function() {
        var findOpByHttpMethod = function(op) { return op.httpMethod ===  $routeParams.httpMethod}
        $scope.opApi = _.find($scope.api.apis, function(o) { return o.path === path && _.any(o.operations, findOpByHttpMethod)}) || { operations: []};
        $scope.operation = _.find($scope.opApi.operations, findOpByHttpMethod) || {};

        $scope.specs = [];
        $http.get('../../specs', {params: {httpMethod: $routeParams.httpMethod, path: path}}).
              success(function(data) {
                  data.forEach(function(spec) {
                      $http.get('../../specs/' + encodeURIComponent(spec)).
                          success(function(data) {
                              spec = { title: data.title, requests: [] };
                              data.whens.forEach(function(when) {
                                  spec.requests.push({
                                      httpMethod: when.method, path: '/' + when.path, body: when.body, showBody: when.body.trim() !== '',
                                      response: { body: when.then.expected, status: when.then.expectedCode }});
                              });
                              $scope.specs.push(spec);
                          });
                  });
              });
    });

    $scope.tryExample = function(request) {
        setTry(true);
        bodyParamValue(request.body);
        $scope.request = { httpMethod: request.httpMethod, path: request.path, body: request.body, response: {status: '', body: ''} };
        sendRequest();
    }
});
