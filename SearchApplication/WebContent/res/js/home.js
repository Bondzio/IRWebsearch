(function(jQuery) {
	$("html:first").removeClass("no-js");

	var carousel = $('.carousel');
	carousel.carousel({
		interval: carousel.data('interval-sec') * 1000 || 5000
	});


	if(window.ur == null || typeof(window.ur) == 'undefined')
		window.ur = new Object();

	if (typeof(window.ur.start) == "undefined" || window.ur.start == null)
		window.ur.start = new Object();

	jQuery(function() {
		// Objekt für Datepicker deklarieren und initialisieren
		window.ur.start.DatePickerStart = window.ur.start.DatePicker;
		window.ur.start.DatePickerStart.initialize('#datepicker_home', 'input[name=startdate]');


		// Kalender einblenden
		jQuery('#datepicker .box').hide();
		jQuery('#datepicker').show();
		jQuery('#calendarPopup ul').show();

		// Handler für alle Events (click, keypress,...)
		window.ur.start.handleEvents();

		// Style-Anpassungen für IE < 8
		if (window.ur.start.isSmallerIE8())
		{
			jQuery('#calendarPopup ul').css({'margin-left':'0px', 'left':'-522px'});
		}
	});

	// eds: IE-Version prüfen
	window.ur.start.isSmallerIE8 = function() {
		var isIE = navigator.appVersion.match(/MSIE \d+/);
		return isIE != null && isIE.toString().match(/\d+/) <= 7;
	}

	// Objekt für Datepicker deklarieren
	window.ur.start.DatePicker = {
		initialize: function(container, date_field) {
			this.container = jQuery(container);
			this.form = this.container.parent().children('form');
			this.activeDates = '';
			this.systemid = jQuery('input[name=calendar_db_systemid]').val();
			this.calendarid = jQuery('input[name=calendar_id]').val();

			// Tage des aktuellen Monats, welche Veranstaltungen enthalten, aus Datenbank ermitteln
			var date = new Date();
			this.getDays(date.getFullYear(), date.getMonth() + 1);

			this.container.datepicker({
				dateFormat: jQuery.datepicker.ISO_8601,
				beforeShowDay: jQuery.proxy(this.filterDatesAndMakeTodayBehave, this),
				onSelect: jQuery.proxy(this.toogleDate, this),
				prevText: "zurück",
				nextText: "vor",
				onChangeMonthYear: jQuery.proxy(this.toggleMonth, this)
			});
		},

		// Wechsel der Monatsansicht --> Tage des neuen Monats, welche Veranstaltungen enthalten, aus Datenbank ermitteln
		toggleMonth: function(year, month) {
			this.getDays(year, month);
		},

		// Tage eines Zeitbereichs, welche Veranstaltungen enthalten, aus Datenbank ermitteln
		getDays: function(year, month) {
			var thisObj = this;
			jQuery.ajax(
				{
					url: '/res/ur_modules/cmsDatabase/home/readCalendarDatabase.php', // Seiten-IDs ermitteln
					type: 'GET',
					async: false,
					data: { 'days': true,
						'systemid': thisObj.systemid,
						'calendarid': thisObj.calendarid,
						'year': year,
						'month': month
					},
					dataType: 'jsonp',
					jsonp: 'databaseCallback',
					success: function (data) {
						if(data.status) {
							thisObj.activeDates = data.days;
						}
						else {
							// Fehler bei Datenbankabfrage
							thisObj.errorWorkaround(false);
						}
					},
					error: function () {
						// Fehler beim Aufruf des PHP-Sktipts
						thisObj.errorWorkaround(false);
					}
				});
		},

		// Klick auf Tag mit Veranstaltungen --> Veranstaltungen aus Datenbank laden und im Popup zur Anzeige bringen
		toogleDate: function(dateText) {
			var thisObj = this;
			jQuery.ajax(
				{
					url: '/res/ur_modules/cmsDatabase/home/readCalendarDatabase.php', // Seiten-IDs ermitteln
					type: 'GET',

					async: false,

					data: { 'startsiteoverview': true,
						'systemid': this.systemid,
						'calendarid': this.calendarid,
						'selectedday': dateText
					},
					dataType: 'jsonp',
					jsonp: 'databaseCallback',
					success: function (data) {
						if(data.status) {
							// Veranstaltungen im Popup anzeigen
							var link = '/cgi-bin/site_active.pl?TEMPLATE=' + jQuery('input[name=TEMPLATE]').val();
							var popUpBody = '<li><a href="' + link + '&datetype=span&startdate=' + dateText + '&enddate=' + dateText + '"><h1>Veranstaltungen am ' + window.ur.start.formatDateFrontend(dateText) + '</h1></a></li>';
							for (index = 0; index < data.events.length; index++) {
								popUpBody += '<li><a class="message" href="' + data.events[index].url + '"><h2>' + data.events[index].title + '</h2>' + data.events[index].teaser + ' …</a></li>';
							}
							popUpBody += '<li><a href="' + link + '"><h1>Alle Veranstaltungen</h1></a></li>';
							// popUpBody +='<li id="closePopUp"><a>Schließen</a></li>';
							jQuery('#calendarPopup ul').html(popUpBody);
							jQuery('#calendarPopup').show();
							window.ur.start.overlay(true);
						}
						else {
							// Fehler bei Datenbankaktion
							thisObj.errorWorkaround(true);
						}
					},
					error: function () {
						// Fehler beim Aufruf des PHP-Sktipts
						thisObj.errorWorkaround(true);
					}
				});
		},

		// Link auf Veranstaltungskalender, falls Fehler bei Ajax-Aufruf auftritt
		errorWorkaround: function(jump) {
			if(jump) {
				// direkter Absprung zum Veranstaltungskalender
				window.open('/cgi-bin/site_active.pl?TEMPLATE=' + jQuery('input[name=TEMPLATE]').val());
			}
			else {
				// Kalender als Link auf Veranstaltungskalender
				jQuery('#datepicker').append('<a id="datepickerLink" title="Link zum Veranstaltungskalender" target="_blank" href="/cgi-bin/site_active.pl?TEMPLATE=' +
					jQuery('input[name=TEMPLATE]').val() + '"></a>');
			}
		},

		filterDatesAndMakeTodayBehave: function(date) {
			var result = this.daysWithEventsOnly(date),
				isToday = function(d) {
					return jQuery.datepicker.formatDate(jQuery.datepicker.ISO_8601, d) ===
						jQuery.datepicker.formatDate(jQuery.datepicker.ISO_8601, new Date());
				};

			return result;
		},
		daysWithEventsOnly: function(date) {
			for (var i = 0; i < this.activeDates.length; i++) {
				if (jQuery.datepicker.formatDate(jQuery.datepicker.ISO_8601, date) == this.activeDates[i]) return [true, ''];
			}
			return [false, '', 'Keine Veranstaltungen'];
		}
	};

	// Datum von engl. Format ins deutsche konvertieren (yyyy-mm-dd -> dd.mm.yyyy)
	window.ur.start.formatDateFrontend = function(date) {
		if( date.search(/\d+-\d+-\d+/)  >= 0) {
			var year = date.replace(/-\d+-\d+$/, '');
			var month = date.replace(/^\d+-/, '').replace(/-\d+$/, '');
			var day = date.replace(/^\d+-\d+-/, '');
			return day + '.' + month + '.' + year;
		}

		return date;
	}

	// Handler für alle Events (click, keypress,...)
	window.ur.start.handleEvents = function() {

		// Popup bei beliebigem Klick schließen
		jQuery('body').click(function(event) {
			jQuery('#calendarPopup').hide();
			if(!jQuery('div#universityPopup').hasClass('open') &&
				(jQuery(event.target).parents().attr('id') == 'university' || jQuery(event.target).parents().attr('id') == 'universityPopup')) {
					// Klick auf "universityPopup" zum Öffnen
					window.ur.start.overlay(true);
			}
			else {
				jQuery('div#universityPopup table td').removeClass('active');
				jQuery('div#universityPopup table tr').removeClass('active');
				window.ur.start.overlay(false);
			}
		});

		/**************************************************
		 * Event-Handler für Kalender
		 **************************************************/
		// Bei Klick im Kalender bzw. im Popup, das Popup nicht schließen (den Klick aus "jQuery('body').click" (siehe oben) in diesem Fall verhindern)
		jQuery('#datepicker').click(function(event){
			event.stopPropagation();
			jQuery('.faculty-arrow-big').removeClass('open');
			jQuery('#universityPopup').removeClass('open');
			if(!jQuery(event.target).is('a')) {
				jQuery('#calendarPopup').hide();
				window.ur.start.overlay(false);
			}
		});

		// Popup bei Klick ins Popup schließen
		jQuery('#calendarPopup').click(function() {
			jQuery('#calendarPopup').hide();
			window.ur.start.overlay(false);
		});



		/**************************************************
		 * Event-Handler für Kachel "Universität von A-Z"
		 **************************************************/
		jQuery('div#universityPopup table td h2').click(function(event) {
			event.stopPropagation();
			var show = true;
			if(jQuery(this).parents('td').hasClass('active'))
				show = false;

			jQuery('div#universityPopup table tr').removeClass('active');
			jQuery('div#universityPopup table td').removeClass('active');

			if (window.ur.start.isSmallerIE8())
				jQuery(this).parents('td').parents('tr').siblings('tr.links').css('display','none')

			if(show) {
				var row = jQuery(this).parents('td').parents('tr').attr('class');
				var col = jQuery(this).parents('td').attr('class');
				jQuery(this).parents('td').parents('tr').siblings('tr.links.' + row + '.' + col).addClass('active');
				jQuery(this).parents('td').addClass('active');

				if (window.ur.start.isSmallerIE8())
					jQuery(this).parents('td').parents('tr').siblings('tr.links.' + row + '.' + col).css('display','block')
			}
		});

		jQuery('div#universityPopup table tbody').click(function(event) {
			event.stopPropagation();
		});

		jQuery('div#universityPopup table tbody a').click(function(event) {
			jQuery('div#universityPopup').removeClass('open');
			jQuery('div#universityPopup table tr').removeClass('active');
			jQuery('div#universityPopup table td').removeClass('active');
			window.ur.start.overlay(false);
		});
	}

	window.ur.start.overlay = function(setOverlay) {
		var boxes = jQuery('.span1.newsblock div');
		jQuery.each(boxes, function() {
			if(setOverlay)
				jQuery(this).css({'opacity': '0.25', 'background-color': '#ffffff'});
			else
				jQuery(this).removeAttr('style');
		});

		var links = jQuery('.span1.newsblock a');
		jQuery.each(links, function() {
			if(setOverlay)
				jQuery(this).css('cursor', 'default');
			else
				jQuery(this).css('cursor', 'pointer');

			if(setOverlay) {
				jQuery(this).click(function(e) {
					e.preventDefault();
				});
			}
			else {
				jQuery(this).unbind('click');
			}
		});
	}

})(jQuery);

