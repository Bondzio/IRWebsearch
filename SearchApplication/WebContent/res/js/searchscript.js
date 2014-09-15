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
        	parts[1] = parts[1].split("%2B").join(" ");
        	parts[1] = parts[1].split("+").join(" ");
        	return decodeURIComponent(unescape(parts[1]));
        }
    }
    
};

function encode_utf8(rohtext) {   
           // dient der Normalisierung des Zeilenumbruchs
            rohtext = rohtext.replace(/\r\n/g,"\n");
            var utftext = "";
             for(var n=0; n<rohtext.length; n++)
                 {
                 // ermitteln des Unicodes des  aktuellen Zeichens
                 var c=rohtext.charCodeAt(n);
                 // alle Zeichen von 0-127 => 1byte
                 if (c<128)
                     utftext += String.fromCharCode(c);
                 // alle Zeichen von 127 bis 2047 => 2byte
                 else if((c>127) && (c<2048)) {
                     utftext += String.fromCharCode((c>>6)|192);
                     utftext += String.fromCharCode((c&63)|128);}
                 // alle Zeichen von 2048 bis 66536 => 3byte
                 else {
                     utftext += String.fromCharCode((c>>12)|224);
                     utftext += String.fromCharCode(((c>>6)&63)|128);
                     utftext += String.fromCharCode((c&63)|128);}
                 }
             return utftext;
         }


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