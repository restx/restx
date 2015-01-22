/*global requirejs */

// Ensure any request for this webjar brings in jQuery.
requirejs.config({
    shim: {
        'angular-ui': [ 'webjars!angular.js', 'webjars!jquery.js' ]
    }
});
