'use strict';

adminApp.factory('TestResultSummary', function($resource) {
  return $resource('../../tests/results/summaries');
});

adminApp.factory('TestResult', function($resource) {
  return $resource('../../tests/results/:key');
});

adminApp.factory('TestRequest', function($resource) {
  return $resource('../../tests/requests/:key');
});
