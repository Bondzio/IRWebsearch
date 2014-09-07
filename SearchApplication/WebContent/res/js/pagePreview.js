/**
 * When the user hovers over a page, this will create a preview of that page.
 */

initPagePreview();

var previewDisplayed = false;
var previewPage;

function initPagePreview() {
	var mq = window.matchMedia("(min-width: 1100px)");
	if(mq.matches) {
		$('.result-title').hover(mouseOverTitle, mouseLeftTitle);
	}
	
}

function mouseOverTitle(event) {
	previewPage = setTimeout(function() {
		showPreview(event.currentTarget.firstChild.href);
	}, 1000);
}

function showPreview(url) {
	previewDisplayed = true;
	$("#previewFrame").empty();
	$("<iframe width='100%' height='500px' src='"+url+"' class='preview_frame'></iframe>").appendTo('#previewFrame');
	$("#previewFrame").removeClass("hidden");
}

function mouseLeftTitle() {
	clearTimeout(previewPage);
	doPreview = false;
}