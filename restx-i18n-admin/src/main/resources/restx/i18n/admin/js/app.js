angular.module('admin', ['ngResource', 'ngRoute']);


angular.module('admin')
    .directive('contenteditable', function() {
        return {
            require: 'ngModel',
            link: function(scope, elm, attrs, ctrl) {
                elm.bind('blur', function() {
                    scope.$apply(function() {
                        ctrl.$setViewValue(elm.html());
                    });
                });

                ctrl.$render = function() {
                    elm.html(ctrl.$viewValue);
                };

                ctrl.$setViewValue(elm.html());
            }
        };
    })
    .factory('I18nMessages', function($resource) {
        return $resource('../../i18n/messages/:locale');
    })
    .controller('I18nController', function($scope, $http, I18nMessages) {
        $scope.messages = {};

        $http.get('../../i18n/keys').success(function(data) { $scope.keys = data; });
        $http.get('../../i18n/locales').success(function(data) {
            $scope.locales =  data;
            if (data.length) {
                $scope.setLocale(data[0]);
            }
        });

        $scope.setLocale = function(locale) {
            $scope.locale = locale;
            $scope.messages = I18nMessages.get({locale: locale});
        };
        $scope.updateMessage = function(key) {
            if ('{{messages[key]}}' !== $scope.messages[key]) {
                var msg = {};
                msg[key] = $scope.messages[key];
                $http.post('../../i18n/messages/' + $scope.locale, msg).success(function() {
                    console.log('updated', key, $scope.messages[key], $scope.locale);
                });
            }
        }
    })
;