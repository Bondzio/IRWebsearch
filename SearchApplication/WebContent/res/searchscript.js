/**
 * 
 */

// http://javascript.jstruebig.de/javascript/59
function getQuery(){
    var s = window.location.search.substring(1).split('&');
    if(!s.length) return;
    for(var i  = 0; i < s.length; i++) {
        var parts = s[i].split('=');
        if(parts[0] == "q") {
        	return parts[1].replace("+", " ");
        }
    }
    
};


document.getElementById("quicksearch-input").value = getQuery();

/*
 * Shows 
 */
function onSearchboxClick() {
	var url = "../../IRWebsearch/WebAppTest";

    $.ajax({
        url: url,
        type: "GET",
        contentType: "text/javascript",
        dataType: "json",
        success: callback,
    });
    
    
}

function callback(data) {
	console.log(data.suggestions);
	if(data.suggestions.length > 6) data.suggestions.length = 6;	
	$searchbox.autocomplete({source: data.suggestions});
}

/*
 *
 */
function onSearchboxInput(event) {
	var url = "../../IRWebsearch/WebAppTest";

    $.ajax({
        url: url,
        type: "GET",
        data: {"query" : event.target.value},
        contentType: "text/javascript",
        dataType: "json",
        success: callback
    });
    
}