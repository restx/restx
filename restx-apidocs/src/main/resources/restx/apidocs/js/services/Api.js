'use strict';

adminApp.factory('ApiDoc', function($resource) {
  return $resource('../../api-docs');
});

adminApp.factory('Api', function($resource) {
  return $resource('../../api-docs/:name');
});
