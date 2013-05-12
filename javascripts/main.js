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

	var tabs = $('a.tab-head');
	if (tabs.size() > 0) {
		var tabsEl = $('<div class="tab-container"></div>').insertBefore($(tabs[0]));
		var ul = $('<ul class="etabs"></ul>').appendTo(tabsEl);
	  tabs.each(function() { 
			var id = $(this).attr('href').substring(1);
			var title = $(this).text();
			$('<div></div>').attr('id', id).append($(this).nextUntil('.tab-head').detach()).appendTo(tabsEl);
			$('<li class="tab"></li>').append($('<a href="#'+ id +'"></a>').html($(this).html())).appendTo(ul);
			$(this).remove();
		});
		tabsEl.easytabs();
	}
	
})
