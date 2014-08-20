/**
 * 
 */


searchbox = $("#quicksearch-input");

searchbox.on("click", onSearchboxClick);
searchbox.on("input", onSearchboxInput);



searchbox.autocomplete("enable");




/*
 * Shows 
 */
function onSearchboxClick() {
	var url = "WebAppTest";

    $.ajax({
        url: url,
        type: "GET",
        contentType: "text/javascript",
        dataType: "json",
        success: callback
    });
    
    
}

function callback(data) {
	console.log(data.suggestions);
	if(data.suggestions.length > 6) data.suggestions.length = 6;	
	searchbox.autocomplete({source: data.suggestions});
}

/*
 *
 */
function onSearchboxInput(event) {
	var url = "WebAppTest";

    $.ajax({
        url: url,
        type: "GET",
        data: {"query" : event.target.value},
        contentType: "text/javascript",
        dataType: "json",
        success: callback
    });
    
}