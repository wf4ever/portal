function test_tree_reload() {
	    $( ".wicket-tree  a" ).each(function(){
	 	   var resourceFolder = $(this);
	 	   $(this).droppable({
	 		   drop:function(event, ui_element) {
	 			  $("#shift-form-file-uri").val($(event.target).parent().attr("uri"));
	 			  $("#shift-form-folder-uri").val(resourceFolder.parent().parent().attr('uri'));
	 			  $("#hidden-files-shift-form-submit-link").click();
				}
			});
	    });	    
}

function test_tree_reload_sortable() {
	$( "#sortable" ).sortable();
    $( "#sortable" ).disableSelection();
    
    $( ".tailLabel" ).each(function(){
    	$(this).click(function(){
    
    		
    		
        	if($(this).hasClass("tailLabelSelected")) {
        		$(this).removeClass("tailLabelSelected");
        		return true;
        	}
        	
        	$( ".tailLabel" ).each(function(){
        		if($(this).hasClass("tailLabelSelected")) {
            		$(this).removeClass("tailLabelSelected");
            	}
            		
        	});
        	
        	$(this).addClass("tailLabelSelected");
        	return true;
    	});
    });
}