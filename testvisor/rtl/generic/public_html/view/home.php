<?php

	function ity_home_btn($caption, $ns, $param, $value, $app_session, $mode = ""){		
		$referer = "";
		if (isset($_GET["inReferer"])){
			$referer = $_GET["inReferer"];
		}
		
		return 
			"<form method='GET' id='".$ns.$value."' action='/test/".$ns."'>
				<input type='hidden' name='app_session' value='".$app_session."' />
				<input type='hidden' name='action_id' value='0' />
				<input type='hidden' name='inLocale' value='en' />
				".($mode ? "<input type='hidden' name='inMode' value='".$mode."' />" : "")."
				<input type='hidden' name='inReferer' value='".htmlentities($referer)."' />
				<input type='hidden' name='".htmlentities($param)."' value='".htmlentities($value)."' />
				<input class='vote' style='width: 45%; height: 32px; margin: 4px;' type='submit' value='".$caption."' onclick='itySubmitForm(\"".$ns.$value."\"); return false;' />
			</form>";
	}

	function ity_home_btn_ex(&$top, &$bottom, $recent, $caption, $ns, $param, $value, $app_session, $mode = ""){
		$item = ity_home_btn($caption, $ns, $param, $value, $app_session, $mode);
		if (in_array($value, $recent)){
			array_push($top, $item);
		} else {
			array_push($bottom, $item);
		}
	}

	function ity_itemize_for_lang($user_id, &$top, &$bottom, $app_session){
		$now = ity_pst_time();
		ity_date_wy($now, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);

		// get recent
		$recent = array();
		$dbh = ity_db();
		$qry = ity_qry($dbh, 
			"SELECT DISTINCT(UUR_UNI_ID), UNI_TYPE FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY
			LEFT JOIN ITY_ADMIN.UNI_UNIT ON UNI_ID = UUR_UNI_ID
			WHERE UUR_USR_ID = %s AND UNI_TYPE = %s AND ((UUR_YEAR = %s AND UUR_WEEK = %s) OR ((UUR_YEAR = %s AND UUR_WEEK = %s)))
			ORDER BY UNI_TITLE", 
			array($user_id, "2", $now_year, $now_week, $prior_year, $prior_week)
		);
		while($row = mysql_fetch_assoc($qry)){
			array_push($recent, "".$row['UUR_UNI_ID']);
		}

		// itemize
		ity_home_btn_ex($top, $bottom, $recent, "العربية -- الانجليزية", "vocb", "inUnitId", "122", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Arabic", "vocb", "inUnitId", "122", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Հայերեն - անգլերեն", "vocb", "inUnitId", "158", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "Armenian-English", "vocb", "inUnitId", "158", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Българо-Английски", "vocb", "inUnitId", "155", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Bulgarian", "vocb", "inUnitId", "155", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Català-Anglès", "vocb", "inUnitId", "132", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Catalan", "vocb", "inUnitId", "132", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "中英文", "vocb", "inUnitId", "134", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Chinese", "vocb", "inUnitId", "134", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Hrvatsko-Engleski", "vocb", "inUnitId", "126", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Croatian", "vocb", "inUnitId", "126", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Čeština-Angličtina", "vocb", "inUnitId", "145", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Czech", "vocb", "inUnitId", "145", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Dansk-Engelsk", "vocb", "inUnitId", "154", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Danish", "vocb", "inUnitId", "154", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Nederlands-Engels", "vocb", "inUnitId", "138", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Dutch", "vocb", "inUnitId", "138", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Esperanto-English", "vocb", "inUnitId", "151", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Esperanto", "vocb", "inUnitId", "151", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Suomi-Englanti", "vocb", "inUnitId", "124", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Finnish", "vocb", "inUnitId", "124", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Français-Anglais", "vocb", "inUnitId", "133", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-French", "vocb", "inUnitId", "133", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Deutsch-Englisch", "vocb", "inUnitId", "144", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-German", "vocb", "inUnitId", "144", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Ελληνικά-Αγγλικά", "vocb", "inUnitId", "141", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Greek", "vocb", "inUnitId", "141", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "עברית, אנגלית", "vocb", "inUnitId", "128", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Hebrew", "vocb", "inUnitId", "128", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "हिंदी - अंग्रेज़ी", "vocb", "inUnitId", "139", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Hindi", "vocb", "inUnitId", "139", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Magyar-Angol", "vocb", "inUnitId", "148", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Hungarian", "vocb", "inUnitId", "148", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Indonesia-Inggris", "vocb", "inUnitId", "123", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Indonesian", "vocb", "inUnitId", "123", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Italiano-Inglese", "vocb", "inUnitId", "125", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Italian", "vocb", "inUnitId", "125", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "日本語 - 英語", "vocb", "inUnitId", "146", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Japanese", "vocb", "inUnitId", "146", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "한국어 - 영어", "vocb", "inUnitId", "149", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Korean", "vocb", "inUnitId", "149", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Latin-English", "vocb", "inUnitId", "152", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Latin", "vocb", "inUnitId", "152", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Lietuvių-Anglų", "vocb", "inUnitId", "136", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Lithuanian", "vocb", "inUnitId", "136", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Malti-Ingliż", "vocb", "inUnitId", "127", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Maltese", "vocb", "inUnitId", "127", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Norsk-Engelsk", "vocb", "inUnitId", "137", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Norwegian", "vocb", "inUnitId", "137", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Polsko-Angielski", "vocb", "inUnitId", "156", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Polish", "vocb", "inUnitId", "156", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Português-Inglês", "vocb", "inUnitId", "135", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Portuguese", "vocb", "inUnitId", "135", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Română-Engleză", "vocb", "inUnitId", "143", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Romanian", "vocb", "inUnitId", "143", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Русско-Английский", "vocb", "inUnitId", "150", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Russian", "vocb", "inUnitId", "150", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Српски-Енглески", "vocb", "inUnitId", "153", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Serbian", "vocb", "inUnitId", "153", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Slovenská-Anglickom", "vocb", "inUnitId", "131", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Slovak", "vocb", "inUnitId", "131", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Español-Inglés", "vocb", "inUnitId", "140", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Spanish", "vocb", "inUnitId", "140", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Svensk-Engelsk", "vocb", "inUnitId", "142", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Swedish", "vocb", "inUnitId", "142", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "ไทยอังกฤษ", "vocb", "inUnitId", "130", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Thai", "vocb", "inUnitId", "130", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Türkçe-İngilizce", "vocb", "inUnitId", "147", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Turkish", "vocb", "inUnitId", "147", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Український-Aнглійський", "vocb", "inUnitId", "157", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Ukrainian", "vocb", "inUnitId", "157", $app_session, "1");

		ity_home_btn_ex($top, $bottom, $recent, "Việt-Anh", "vocb", "inUnitId", "129", $app_session, "2");
		ity_home_btn_ex($top, $bottom, $recent, "English-Vietnamese", "vocb", "inUnitId", "129", $app_session, "1");
	}

	include_once "../lib/dao.php";
	include_once "../lib/cache.php";
	include_once "common.php";

	// bind to user
	$reason = null;  
	$name = null;
	$is_pro = false;
	$app_session = $_GET["app_session"];
	if ($app_session == null){
		echo "Access denied (no application session).";
		return;
	}
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	if ($user_id == null){
		echo $reason;
		return;
	}

	// ads
	$ads = "";
	if (!$is_pro){
		// $ads = "<br />".ity_ad_mobile();
	}

	// itemize lang favorites
	$top = array(); $bottom = array();
	ity_itemize_for_lang($user_id, $top, $bottom, $app_session);

    // derive cookie name from user id
	$lang = "en";
	$cookie_name = "lang".md5($user_id);
	if (isSet($_COOKIE[$cookie_name])) {
		$lang = $_COOKIE[$cookie_name];
	}

	// math
	$math_home = 
	    "<h2 align='center'>Math Worksheets</h2>
		<select onchange='itySetLocale(this.options[this.selectedIndex].value);' style='font-size: 125%; font-weight: bold; padding: 4px; background-color: #88FFFF;'>
			<option value='en' ".(($lang == "en") ? "selected" : "").">&nbsp;&nbsp;English (EN)&nbsp;&nbsp;</option>
			<option value='es' ".(($lang == "es") ? "selected" : "").">&nbsp;&nbsp;Español (ES)&nbsp;&nbsp;</option>
		    <option value='nl' ".(($lang == "nl") ? "selected" : "").">&nbsp;&nbsp;Nederlands (NL)&nbsp;&nbsp;</option>
			<option value='ru' ".(($lang == "ru") ? "selected" : "").">&nbsp;&nbsp;Русский (RU)&nbsp;&nbsp;</option>
			<option value='zh' ".(($lang == "zh") ? "selected" : "").">&nbsp;&nbsp;中文 (ZH)&nbsp;&nbsp;</option>
		</select>
		<div style='text-align: center; padding-top: 8px;'>
		".
			ity_home_btn("Grade 1", "wdgt", "inGradeId", "1", $app_session).
			ity_home_btn("Grade 2", "wdgt", "inGradeId", "2", $app_session).
			ity_home_btn("Grade 3", "wdgt", "inGradeId", "3", $app_session).
			ity_home_btn("Grade 4", "wdgt", "inGradeId", "4", $app_session).
			ity_home_btn("Grade 5", "wdgt", "inGradeId", "5", $app_session).
			ity_home_btn("Grade 6", "wdgt", "inGradeId", "6", $app_session).
			ity_home_btn("Grade 7", "wdgt", "inGradeId", "7", $app_session).
			ity_home_btn("Grade 8", "wdgt", "inGradeId", "8", $app_session).
		"</div>";

	// vocb

	$items = "";
	$dbh = ity_db();
	$qry = ity_qry($dbh, "SELECT UNI_ID, UNI_TITLE FROM ITY_ADMIN.UNI_UNIT WHERE UNI_TYPE = 2 AND UNI_STATE = 1 AND UNI_OWNER_USR_ID = %s", array($user_id));
	while($row = mysql_fetch_assoc($qry)){
		$items .= ity_home_btn($row["UNI_TITLE"], "vocb", "inUnitId", $row["UNI_ID"], $app_session);
	}
	if ($items == "") {
		$items = "<p>Login to <b>www.itestyou.com</b> and create your own vocabulary.<br />It will be shown here automatically!</p>";
	}

	$vocb_home = 
		"
		<h2 align='center'>My Vocabulary</h2>
		<div style='text-align: center;'>
		".$items."
		</div>
		<h2 align='center'>English Vocabulary</h2>
		<div style='text-align: center;'>".
			ity_home_btn("SAT 5000", "vocb", "inUnitId", "115", $app_session).
			ity_home_btn("GRE 5000", "vocb", "inUnitId", "116", $app_session).
			ity_home_btn("GMAT 1500", "vocb", "inUnitId", "121", $app_session).
			ity_home_btn("TOEFL 2000", "vocb", "inUnitId", "117", $app_session).
			ity_home_btn("SMS & Chat Vocabulary", "vocb", "inUnitId", "164", $app_session).
			ity_home_btn("Business Slang", "vocb", "inUnitId", "231", $app_session).
		"</div>";

	// render favorites
	$lang_home = "";
	if (count($top) != 0){
		$lang_home .= 
			"<h2 align='center'>Your Favorites</h2>
			<div style='text-align: center;'>";
		foreach($top as $item){
			$lang_home .= $item;
		}
		$lang_home .= "</div>".$ads."<hr />";
	}

	// render remainder items
	$lang_home .= 
		"<h2 align='center'>Foreign Languages</h2>
		<div style='text-align: center;'>";
	foreach($bottom as $item){
		$lang_home .= $item;
	}
	$lang_home .= "</div>";

	// choose home page
	$home = $math_home;
	$has_edition = false;
	$edition = ity_get_edition($has_edition);
	if($edition == "vocb"){
		$home = $vocb_home;
	} else {
		if($edition == "lang"){
			$home = $lang_home;
		}
	}

	// body
	$body = 
		"<html>
			<head>
				<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
				<link type='text/css' href='http://www.itestyou.com/view/normal.css' rel='stylesheet' />
				<script type='text/javascript'><!--
				    document.ityLocale='".htmlentities($lang)."';
					function itySetLocale(lang) {
					  var date = new Date();
					  date.setTime(date.getTime()+(90*24*60*60*1000));
					  document.cookie = '".$cookie_name."=' + lang + '; expires=' + date.toGMTString() + '; path=/; domain=.itestyou.com';
					  document.ityLocale = lang;
					}

					function itySubmitForm(id) {
					  var frm = document.forms[id]; 
					  frm.elements['inLocale'].value = document.ityLocale; 
					  frm.submit();
					}
				--></script>
			</head>
			<body style='padding: 0px; margin: 0px;'>"
				.ity_mobile_tools_menu($user_id, $app_session, $_SERVER["SCRIPT_NAME"]).
				"<div style='width: 100%; vertical-align: top;' align='center'>".$home."</div>"
				.$ads
				."<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>"
				.ity_render_tracking().
			"</body>
		</html>";

	echo $body;
?>