/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.  
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

$().ready(function() {
	window.countSubmitted = 0;
	$("button#submit").click(function() {
		$("#loading").css("display", "inline");
		var elems = document.getElementsByTagName("input");
		$.each(elems, function() {
			if (this.checked) {
				window.countSubmitted++;
				$.ajax({url:"/GetDataFromURL?url="+this.value.replace('&', 'AND').replace('AMP', '&'), async: false, success:function() {
					$.get("/updatecount");
					window.countSubmitted--;
					if (window.countSubmitted === 0) {
						$("#loading").hide();
					}
				}, error: function() {
					$.get("/updatecount");
					window.countSubmitted--;
					if (window.countSubmitted === 0) {
						$("#loading").hide();
					}
					alert("Error");
				}});
			}
		});
		alert("Complete");
	});
})