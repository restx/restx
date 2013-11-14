'use strict';

adminApp.run(function($rootScope) {
    key.filter = function(event){
        // always process shortcuts
        return true;
    }
    key('⌘+enter, ctrl+enter', function() {
         $rootScope.$broadcast('ctrl+enter');
    });
})

adminApp.filter('encodePath', function() {
    return function(input) {
        return input.replace(/\//g, '___');
    };
});

adminApp.controller('OperationController', function OperationController(
        $rootScope, $scope, $routeParams, $http, $filter, $location, ApiDoc, Api) {
    var path = $routeParams.path.replace(/___/g, '/');
    $scope.doc = ApiDoc.get();
    $scope.su = $rootScope.su;
    $scope.responseActive = true; // default

    function setTry(tryMode) {
        $scope.try = $rootScope.try = tryMode;
        $scope.tryButtonLabel = $scope.try ? 'Hide try' : 'Try it out!';
        if (!$scope.try) {
            $scope.request = undefined;
        } else {
            // do when dom has been updated
            setTimeout(function() {
                $('.parameters td.value :input').first().focus();
            }, 250);
        }
    }

    $scope.$on('ctrl+enter', function() {
        if ($scope.try) {
            $scope.send();
        }
    });

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

    function sendRequest(onSuccess) {
        $rootScope.su = $scope.su;
        $rootScope.contextData = null; // clear context data
        $scope.request.headers['RestxSu'] = $scope.su ? '{ "principal": "'+ $scope.su +'" }' : "";
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
                $rootScope.contextData = data;
                $rootScope.contextDataType = $scope.operation.responseClass;

                try {
                    $scope.request.response.bodyObj = JSON.parse(data);
                    $scope.request.response.bodyType =
                        Array.isArray($scope.request.response.bodyObj)
                        ? "array"
                        : typeof $scope.request.response.bodyObj;
                } catch (e) {
                    $scope.request.response.bodyObj = null;
                    $scope.request.response.bodyType = "raw";
                }
                if (onSuccess) {
                    onSuccess(data, status, headers, config);
                }
            }).
            error(function (data, status, headers, config) {
                $scope.request.response.status = status;
                $scope.request.response.body = data;
            });
    }

    $scope.goWithContext = function(rel, el) {
        $rootScope.contextData = angular.toJson(el, true);
        $rootScope.contextDataType = $scope.operation.responseClass.replace(/LIST\[(.+)\]/, '$1');

        $location.path('/operation/'+rel.apiDocName+'/' + rel.httpMethod + '/' + rel.path.replace(/\//g, '___'));
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

    function prepareRequestForRecord() {
        $scope.request = prepareRequest();
        $scope.request.headers['RestxMode'] = 'recording';
        $scope.request.headers['RestxRecordPath'] = 'specs/' + $routeParams.name;
        $scope.request.headers['RestxRecordTitle'] = _.str.humanize($scope.operation.nickname) + ' '
            + $filter('date')(new Date(), 'yyyyMMdd HHmmss');
    }

    $scope.send = function() {
        $scope.request = prepareRequest();
        sendRequest();
    }


    $scope.sendAndRecord = function() {
        prepareRequestForRecord();
        sendRequest(function() { loadAllSpecs(); });
    }

    $scope.sendAndFix = function() {
        prepareRequestForRecord();
        sendRequest(function(data, status, headers) {
            var spec = headers('RestxSpecPath');

            $scope.expect = {
                specPath: spec,
                status: status,
                body: data
            };
        });
    }

    $scope.fix = function() {
        var spec = $scope.expect.specPath;
        $http.put('../../specs/' + encodeURIComponent(spec) + '/wts/0/then',
            {expectedCode: $scope.expect.status, expected: $scope.expect.body }).
            success(function () {
                loadSpec(spec);
            });
        delete $scope.expect;
    }

    function loadSpec(spec) {
        $http.get('../../specs/' + encodeURIComponent(spec)).
            success(function (data) {
                spec = { title: data.title, requests: [] };
                data.whens.forEach(function (when) {
                    spec.requests.push({
                        httpMethod: when.method, path: '/' + when.path, body: when.body, showBody: when.body.trim() !== '',
                        response: { body: when.then.expected, status: when.then.expectedCode }});
                });
                $scope.specs.push(spec);
            });
    }

    function loadAllSpecs() {
        $scope.specs = [];
        $http.get('../../specs', {params: {httpMethod: $routeParams.httpMethod, path: path}}).
            success(function (data) {
                data.forEach(function (spec) {
                    loadSpec(spec);
                });
            });
    }

    $scope.api = Api.get({name: $routeParams.name}, function() {
        var findOpByHttpMethod = function(op) { return op.httpMethod ===  $routeParams.httpMethod}
        $scope.opApi = _.find($scope.api.apis, function(o) { return o.path === path && _.any(o.operations, findOpByHttpMethod)}) || { operations: []};
        $scope.operation = _.find($scope.opApi.operations, findOpByHttpMethod) || {};

        if ($rootScope.contextData) {
            try {
                var o = JSON.parse($rootScope.contextData);

                if (!o.key && o._id) {o.key = o._id;}

                _.each($scope.operation.parameters, function(p) {
                    if (p.paramType === 'body') {
                        if ($rootScope.contextDataType === p.dataType) {
                            p.value = $rootScope.contextData;
                        }
                    } else if (o[p.name]) {
                        p.value = o[p.name];
                    }
                });

            } catch (e) {
                // ignore
            }
        }
        loadAllSpecs();
    });

    $scope.tryExample = function(request) {
        setTry(true);
        bodyParamValue(request.body);
        $scope.request = { httpMethod: request.httpMethod, path: request.path, body: request.body, response: {status: '', body: ''}, headers: {} };
        sendRequest();
    }
});
