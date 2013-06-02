'use strict';

adminApp.controller('OperationController', function OperationController(
        $rootScope, $scope, $routeParams, $http, $filter, ApiDoc, Api) {
    var path = $routeParams.path.replace(/___/g, '/');
    $scope.doc = ApiDoc.get();

    function setTry(tryMode) {
        $scope.try = $rootScope.try = tryMode;
        $scope.tryButtonLabel = $scope.try ? 'Hide try' : 'Try it out!';
        if (!$scope.try) {
            $scope.request = undefined;
        }
    }

    setTry($rootScope.try || false);

    $scope.toggleTry = function() {
        setTry(!$scope.try);
    }

    function bindParams(path) {
        var queryParams = [];
        _.each($scope.operation.parameters, function(p) {
            if (p.paramType == 'path') {
                path = path.replace('{' + p.name + '}', encodeURIComponent(p.value));
            } else if (p.paramType == 'query') {
                if (p.value !== undefined && p.value !== '') {
                    queryParams.push(encodeURIComponent(p.name) + '=' + encodeURIComponent(p.value));
                }
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
        $http(
            {
                method: $scope.request.httpMethod,
                url: $scope.doc.basePath + $scope.request.path,
                data: $scope.request.body,
                headers: $scope.request.headers,
                transformResponse: function (r) {
                    return r
                }
            })
            .success(function (data, status, headers, config) {
                $scope.request.response.status = status;
                $scope.request.response.body = data;
            }).
            error(function (data, status, headers, config) {
                $scope.request.response.status = status;
                $scope.request.response.body = data;
            });
    }

    function prepareRequest() {
        return {
            httpMethod: $scope.operation.httpMethod,
            path: bindParams($scope.opApi.path),
            headers: {},
            body: bodyParamValue(),
            response: { status: '', body: '' }
        };
    }

    $scope.send = function() {
        $scope.request = prepareRequest();
        sendRequest();
    }

    $scope.sendAndRecord = function() {
        $scope.request = prepareRequest();
        $scope.request.headers['RestxMode'] = 'recording';
        $scope.request.headers['RestxRecordPath'] = $routeParams.name;
        $scope.request.headers['RestxRecordTitle'] = _.str.humanize($scope.operation.nickname) + ' '
            + $filter('date')(new Date(), 'yyyyMMdd HHmm');
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
