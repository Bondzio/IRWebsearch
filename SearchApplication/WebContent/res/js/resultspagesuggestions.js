/**
 * 
 */
var pastQueries = [];
initAutocomplete();
function initAutocomplete() {
	initializePastQueries();
	$searchbox = $("#quicksearch-input");
	$searchbox.autocomplete();  
	$searchbox.autocomplete("enable");  
	$searchbox.on("click", onSearchboxClick);
	$searchbox.on("input", onSearchboxInput);
}

function initializePastQueries() {
	var cookies = $.cookie();
	for(var c in cookies) {
		pastQueries.push(cookies[c]);
	}	
	pastQueries = sortAndTrim(pastQueries);
}
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

function sortAndTrim(a) {
	var c = [];
	for(var i = 0; i < a.length; i++) {
		var br = false;
		for(var j = 0; j < c.length; j++) {
			if(c[j].value == a[i]) {
				c[j].count++;
				br = true;
				break;;
			}
		}
		if(!br) c.push({value: a[i], count: 1});
	}
	c = c.sort(function compare(b, d) {
		return d.count - b.count;
	});
	console.log(c);
	var arr = [];
	for(var i = 0; i < c.length; i++) {
		arr.push(c[i].value);
	}
	return arr;
}

function callback(data) {
	if(data.suggestions.length > 6) data.suggestions.length = 6;	
	
	suggestionsArray = [];
	for(var i = 0; i < pastQueries.length; i++) {
		if(suggestionsArray.length <= 3 && beginsWith(pastQueries[i], $searchbox.val())) {
			suggestionsArray.push(pastQueries[i]);
		}
	}
	
	for(var i = 0; i < data.suggestions.length; i++) {
		if(suggestionsArray.length <= 6 && !contains(suggestionsArray, data.suggestions[i])) {
			suggestionsArray.push(data.suggestions[i]);
		}
	}
	
	$searchbox.autocomplete({source: suggestionsArray});
}

function beginsWith(s1, s2){
    return (s1.substr(0, s2.length) == s2);
}

function contains(a, s) {
	for(var i = 0; i < a.length; i++) {
		if(a[i] == s) return true;
	}
	return false;
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