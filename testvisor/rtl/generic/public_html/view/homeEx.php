<?php

	function ity_home_btn_m($caption, $ns, $param, $value){
		return 
			"<form method='GET' action='/cms/category/math-grade-".$value."'>
				<input style='width: 35%; height: 48px; margin: 8px;' type='submit' value='".$caption."' />
			</form>";
	}

	function ity_home_btn_v($caption, $ns, $param, $value){
		return 
			"<form method='GET' action='/cms/unit-".$value."'>
				<input style='width: 35%; height: 48px; margin: 8px;' type='submit' value='".$caption."' />
			</form>";
	}
 
	function ity_home_btn($caption, $ns, $param, $value, $app_session, $mode = ""){
		return 
			"<form method='GET' action='/cms/unit-".$value."'>
				<input style='width: 35%; height: 48px; margin: 8px;' type='submit' value='".$caption."' />
			</form>";
	}

	function ity_render_home($edition){
		$math_home = 
			"<div style='text-align: center;'>".
				ity_home_btn_m("Grade 1", "wdgt", "inGradeId", "1").
				ity_home_btn_m("Grade 2", "wdgt", "inGradeId", "2").
				ity_home_btn_m("Grade 3", "wdgt", "inGradeId", "3").
				ity_home_btn_m("Grade 4", "wdgt", "inGradeId", "4").
				ity_home_btn_m("Grade 5", "wdgt", "inGradeId", "5").
				ity_home_btn_m("Grade 6", "wdgt", "inGradeId", "6").
				ity_home_btn_m("Grade 7", "wdgt", "inGradeId", "7").
				ity_home_btn_m("Grade 8", "wdgt", "inGradeId", "8").
			"</div>";

		$vocb_home = 
			"<div style='text-align: center;'>".
				ity_home_btn_v("SAT 5000", "vocb", "inUnitId", "115").
				ity_home_btn_v("GRE 5000", "vocb", "inUnitId", "116").
				ity_home_btn_v("GMAT 1500", "vocb", "inUnitId", "121").
				ity_home_btn_v("TOEFL 2000", "vocb", "inUnitId", "117").
				ity_home_btn_v("SMS & Chat Vocabulary", "vocb", "inUnitId", "164").
				ity_home_btn_v("Business Slang", "vocb", "inUnitId", "231").
			"</div>";

		$app_session = null;
		$lang_home = 
			"<div style='text-align: center;'>".
				ity_home_btn("العربية -- الانجليزية", "vocb", "inUnitId", "122", $app_session, "2").
				ity_home_btn("English-Arabic", "vocb", "inUnitId", "122", $app_session, "1").

				ity_home_btn("Հայերեն - անգլերեն", "vocb", "inUnitId", "158", $app_session, "2").
				ity_home_btn("English-Armenian", "vocb", "inUnitId", "158", $app_session, "1").

				ity_home_btn("Българо-Английски", "vocb", "inUnitId", "155", $app_session, "2").
				ity_home_btn("English-Bulgarian", "vocb", "inUnitId", "155", $app_session, "1").

				ity_home_btn("Català-Anglès", "vocb", "inUnitId", "132", $app_session, "2").
				ity_home_btn("English-Catalan", "vocb", "inUnitId", "132", $app_session, "1").

				ity_home_btn("中英文", "vocb", "inUnitId", "134", $app_session, "2").
				ity_home_btn("English-Chinese", "vocb", "inUnitId", "134", $app_session, "1").

				ity_home_btn("Hrvatsko-Engleski", "vocb", "inUnitId", "126", $app_session, "2").
				ity_home_btn("English-Croatian", "vocb", "inUnitId", "126", $app_session, "1").

				ity_home_btn("Čeština-Angličtina", "vocb", "inUnitId", "145", $app_session, "2").
				ity_home_btn("English-Czech", "vocb", "inUnitId", "145", $app_session, "1").

				ity_home_btn("Dansk-Engelsk", "vocb", "inUnitId", "154", $app_session, "2").
				ity_home_btn("English-Danish", "vocb", "inUnitId", "154", $app_session, "1").

				ity_home_btn("Nederlands-Engels", "vocb", "inUnitId", "138", $app_session, "2").
				ity_home_btn("English-Dutch", "vocb", "inUnitId", "138", $app_session, "1").

				ity_home_btn("Esperanto-English", "vocb", "inUnitId", "151", $app_session, "2").
				ity_home_btn("English-Esperanto", "vocb", "inUnitId", "151", $app_session, "1").

				ity_home_btn("Suomi-Englanti", "vocb", "inUnitId", "124", $app_session, "2").
				ity_home_btn("English-Finnish", "vocb", "inUnitId", "124", $app_session, "1").

				ity_home_btn("Français-Anglais", "vocb", "inUnitId", "133", $app_session, "2").
				ity_home_btn("English-French", "vocb", "inUnitId", "133", $app_session, "1").

				ity_home_btn("Deutsch-Englisch", "vocb", "inUnitId", "144", $app_session, "2").
				ity_home_btn("English-German", "vocb", "inUnitId", "144", $app_session, "1").

				ity_home_btn("Ελληνικά-Αγγλικά", "vocb", "inUnitId", "141", $app_session, "2").
				ity_home_btn("English-Greek", "vocb", "inUnitId", "141", $app_session, "1").

				ity_home_btn("עברית, אנגלית", "vocb", "inUnitId", "128", $app_session, "2").
				ity_home_btn("English-Hebrew", "vocb", "inUnitId", "128", $app_session, "1").

				ity_home_btn("हिंदी - अंग्रेज़ी", "vocb", "inUnitId", "139", $app_session, "2").
				ity_home_btn("English-Hindi", "vocb", "inUnitId", "139", $app_session, "1").

				ity_home_btn("Magyar-Angol", "vocb", "inUnitId", "148", $app_session, "2").
				ity_home_btn("English-Hungarian", "vocb", "inUnitId", "148", $app_session, "1").

				ity_home_btn("Indonesia-Inggris", "vocb", "inUnitId", "123", $app_session, "2").
				ity_home_btn("English-Indonesian", "vocb", "inUnitId", "123", $app_session, "1").

				ity_home_btn("Italiano-Inglese", "vocb", "inUnitId", "125", $app_session, "2").
				ity_home_btn("English-Italian", "vocb", "inUnitId", "125", $app_session, "1").

				ity_home_btn("日本語 - 英語", "vocb", "inUnitId", "146", $app_session, "2").
				ity_home_btn("English-Japanese", "vocb", "inUnitId", "146", $app_session, "1").

				ity_home_btn("한국어 - 영어", "vocb", "inUnitId", "149", $app_session, "2").
				ity_home_btn("English-Korean", "vocb", "inUnitId", "149", $app_session, "1").

				ity_home_btn("Latin-English", "vocb", "inUnitId", "152", $app_session, "2").
				ity_home_btn("English-Latin", "vocb", "inUnitId", "152", $app_session, "1").

				ity_home_btn("Lietuvių-Anglų", "vocb", "inUnitId", "136", $app_session, "2").
				ity_home_btn("English-Lithuanian", "vocb", "inUnitId", "136", $app_session, "1").

				ity_home_btn("Malti-Ingliż", "vocb", "inUnitId", "127", $app_session, "2").
				ity_home_btn("English-Maltese", "vocb", "inUnitId", "127", $app_session, "1").

				ity_home_btn("Norsk-Engelsk", "vocb", "inUnitId", "137", $app_session, "2").
				ity_home_btn("English-Norwegian", "vocb", "inUnitId", "137", $app_session, "1").

				ity_home_btn("Polsko-Angielski", "vocb", "inUnitId", "156", $app_session, "2").
				ity_home_btn("English-Polish", "vocb", "inUnitId", "156", $app_session, "1").

				ity_home_btn("Português-Inglês", "vocb", "inUnitId", "135", $app_session, "2").
				ity_home_btn("English-Portuguese", "vocb", "inUnitId", "135", $app_session, "1").

				ity_home_btn("Română-Engleză", "vocb", "inUnitId", "143", $app_session, "2").
				ity_home_btn("English-Romanian", "vocb", "inUnitId", "143", $app_session, "1").

				ity_home_btn("Русско-Английский", "vocb", "inUnitId", "150", $app_session, "2").
				ity_home_btn("English-Russian", "vocb", "inUnitId", "150", $app_session, "1").

				ity_home_btn("Српски-Енглески", "vocb", "inUnitId", "153", $app_session, "2").
				ity_home_btn("English-Serbian", "vocb", "inUnitId", "153", $app_session, "1").

				ity_home_btn("Slovenská-Anglickom", "vocb", "inUnitId", "131", $app_session, "2").
				ity_home_btn("English-Slovak", "vocb", "inUnitId", "131", $app_session, "1").

				ity_home_btn("Español-Inglés", "vocb", "inUnitId", "140", $app_session, "2").
				ity_home_btn("English-Spanish", "vocb", "inUnitId", "140", $app_session, "1").

				ity_home_btn("Svensk-Engelsk", "vocb", "inUnitId", "142", $app_session, "2").
				ity_home_btn("English-Swedish", "vocb", "inUnitId", "142", $app_session, "1").

				ity_home_btn("ไทยอังกฤษ", "vocb", "inUnitId", "130", $app_session, "2").
				ity_home_btn("English-Thai", "vocb", "inUnitId", "130", $app_session, "1").

				ity_home_btn("Türkçe-İngilizce", "vocb", "inUnitId", "147", $app_session, "2").
				ity_home_btn("English-Turkish", "vocb", "inUnitId", "147", $app_session, "1").

				ity_home_btn("Український-Aнглійський", "vocb", "inUnitId", "157", $app_session, "2").
				ity_home_btn("English-Ukrainian", "vocb", "inUnitId", "157", $app_session, "1").

				ity_home_btn("Việt-Anh", "vocb", "inUnitId", "129", $app_session, "2").
				ity_home_btn("English-Vietnamese", "vocb", "inUnitId", "129", $app_session, "1").
			"</div>";

		// choose home page
		$home = $math_home;
		if($edition == "vocb"){
			$home = $vocb_home;
		}else {
			if($edition == "lang"){
				$home = $lang_home;
			}
		}

		$body = "<div style='width: 100%;' align='center'>".$home."</div>";

		return $body;
	}
?>