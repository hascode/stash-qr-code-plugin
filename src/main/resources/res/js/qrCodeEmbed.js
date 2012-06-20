(function($) {
	var template = AJS.template('<img src="{url}" alt="QR Code Link"/>');

	$(document).ready(function() {
		var $tabLink = $('#repository-qr-code-item-link');
		if($tabLink){
			var imgUrl = $tabLink.attr('href');
	    	var dialog = new AJS.Dialog({
	    	    width:280, 
	    	    height:320, 
	    	    id:"qr-code-link-overlay", 
	    	    closeOnOutsideClick: true
	    	});
	    	dialog.addHeader("QR Code Link", "qr-code-header");
	    	dialog.addPanel("QR Code", template.fill({url:imgUrl}).toString());
	    	
	    	$tabLink.click(function(){
				dialog.show();
	        	return false;
	        });
		}
    });
})(jQuery);
