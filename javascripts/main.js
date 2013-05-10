$(function() {
	$('.docs img').click(function() { $(this).toggleClass('large') });
	
	if ($('.docs').size() > 0) {
		$('#toc').show().toc();
		$('.inner').addClass('docs');
	} 
	
	$('.banner').each(function(){
		var delay = $(this).data('delay') || 3000;
		var init = $(this).data('init') || 0;
		var loop = $(this).data('loop') || false;
		$(this).unslider({delay: delay, init: init, loop: loop, dots: true, keys:false});
	});
	
	$('.unslider-arrow').click(function() {
      var fn = this.className.split(' ')[1];
  
      //  Either do unslider.data('unslider').next() or .prev() depending on the className
			$(this).closest('.banner').data('unslider')[fn]();
			
			return false;
  });
	
})
