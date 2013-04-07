angular.module('admin', ['ngResource']);



$(function () {
    $.get('../../factory', function(data) {
        $('#factory').text(data);
    });
})
