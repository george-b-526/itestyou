/*
	ITestYou JS Library 1.0
	Copyright (C) 2012 www.itestyou.com
	All Rights Reserved
*/

function ityIframeAutoResize(id){
    if(document.getElementById){
		var frm = document.getElementById(id);
		var h = 0;

		if(frm.contentDocument) {
			h = frm.contentDocument.body.scrollHeight;
		} else  {
			h = frm.contentWindow.document.body.scrollHeight;
		}
		
		if (!frm.ityMinHeight || (h > frm.ityMinHeight)){
			frm.height = "" + h + "px";
			frm.ityMinHeight = h;
		}
	}
}

(function() {
 
	// anchor uid
	if (typeof(window['itesty_iframe_uid']) == "undefined"){
		window['itesty_iframe_uid'] = 0;
	} else { 
		itesty_iframe_uid = parseInt(itesty_iframe_uid) + 1;
	} 

	// custom style
	var style = "border: 1px solid rgb(128, 128, 128); margin: 0px; padding: 0px; width: 336px; height: 280px;";
	if (typeof(window['itesty_iframe_style']) != "undefined"){
		style = itesty_iframe_style;
	}

    // sanitize
	{
		var newStyle = "";
		var len = style.length;
		for (i = 0; i < len; i++) {
			var c = style.charAt(i);
			if ('a' <= c && c <= 'z' ||
				'A' <= c && c <= 'Z' ||
				'0' <= c && c <= '9' ||
				':' == c || ';' == c ||
				'-' == c || '#' == c ||
				'{' == c || '}' == c ||
				' ' == c || '.' == c
				){
				newStyle = newStyle + c;
			} else {
				newStyle = newStyle + "*";
			}
		}
		style = newStyle;
	}

	// engine
	var engine = "wdgt";     
	if (typeof(window['itesty_engine']) != "undefined" && itesty_engine == "vocb"){
		engine = "vocb";	
	}

	// unit
	var uid = "";     
	if ((typeof(window['itesty_unit_id']) != "undefined") && (itesty_unit_id != "undefined")){
		uid = "&amp;inUnitId=" + parseInt(itesty_unit_id);	
	}

	// locale
	var locale = "";     
	if ((typeof(window['itesty_locale']) != "undefined") && (itesty_locale != "undefined")){
		locale = "&amp;inLocale=" + escape(itesty_locale);	
	}

	// grade
	var gid = "";
	if ((typeof(window['itesty_grade_id']) != "undefined") && (itesty_grade_id != "undefined")){
		gid = "&amp;inGradeId=" + parseInt(itesty_grade_id);
	}

	// mode {0, 1, 2}
	var mode = "";
	if ((typeof(window['itesty_mode']) != "undefined") && (itesty_mode != "undefined")){
		gid = "&amp;inMode=" + parseInt(itesty_mode);
	}

	// returnto
	var ref = "";
	var curr = window.location.href;
	if (curr.indexOf("#ity") == -1){
		ref = "&amp;inReferer=" + escape(curr + "#ity" + itesty_iframe_uid);
	}

	// embed
	var frameId = "ityFrm" + itesty_iframe_uid;
	var src = (("https:" == document.location.protocol) ? "https" : "http") + "://www.itestyou.com/test/" + engine + "?action_id=0" + gid + uid + locale + mode + ref;
	var body = "<a name='ity" + itesty_iframe_uid + "'></a><iframe src='" + src + "' style='" + style + "' frameborder='0' id='" + frameId + "' onLoad='ityIframeAutoResize(\"" + frameId + "\");'></iframe>";

    // wipe the variables
	window['itesty_unit_id'] = "undefined";
	window['itesty_grade_id'] = "undefined";
	window['itesty_iframe_style'] = "undefined";

	// done
	document.write(body);

})();