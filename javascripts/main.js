$(function() {
	$('.docs img').click(function() { $(this).toggleClass('large') });
	
	if ($('.docs').size() > 0) {
		$('#toc').show().toc();
		$('.inner').addClass('docs');
	} 
})
