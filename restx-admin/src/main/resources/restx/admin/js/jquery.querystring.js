// see https://github.com/gehsekky/jquery-querystring/blob/master/jquery.querystring.js
(function($){
	$.extend({
		querystring: function(name) {
			var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
			return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
		}
	});
})(jQuery);