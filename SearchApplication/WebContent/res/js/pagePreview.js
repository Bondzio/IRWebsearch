/**
 * When the user hovers over a page, this will create a preview of that page.
 */

initPagePreview();

var previewDisplayed = false;
var previewPage;

function initPagePreview() {
	var turnPreviewPageOff = true;

	var mq = window.matchMedia("(min-width: 1100px)");

	if(mq.matches && !turnPreviewPageOff) {
		$('.result-title a').hover(mouseOverTitle, mouseLeftTitle);
		displayHint();
	}
	
}

function displayHint() {
	if(!JSON.parse(localStorage.getItem("keyDisplayed"))) {
		localStorage.setItem("keyDisplayed", JSON.stringify({value: true}));
		$hint = $("<div style='padding: 50px; font-size: 20px; line-height: 30px; position: absolute; left: 5%; top: 50px; width: 50%; height: 80px; border: 4px solid #333333; background-color: rgba(255,255,255,0.9);'>Neu: Wenn Sie mit der Maus eine Sekunde lang über dem Titel einer Seite bleiben, wird rechts eine Vorschau der Seite angezeigt.</div>");
		$hint.appendTo("body");
		$hint.delay(5000).fadeOut();
	}
}

function mouseOverTitle(event) {
	previewPage = setTimeout(function() {
		showPreview(event.currentTarget.href);
	}, 1000);
}

function showPreview(url) {
	previewDisplayed = true;
	$("#previewFrame").empty();
	$previewContent = $("<iframe width='100%' height='500px' src='"+url+"' class='preview_frame'></iframe>");
	$previewContent.appendTo('#previewFrame');
	$abovePreviewContent = $("<div class='preview_frame' style='width:500px; height:500px; z-index: 10000; color: rgba(0,0,0,0); background-color: rgba(0,0,0,0); position: absolute; margin-top:-510px;'>leer</div>");
	$abovePreviewContent.on("click", function(){window.location=url;});	
	$abovePreviewContent.appendTo('#previewFrame');
	$("#previewFrame").removeClass("hidden");
}

function mouseLeftTitle() {
	clearTimeout(previewPage);
	doPreview = false;
}