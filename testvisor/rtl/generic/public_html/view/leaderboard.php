<?php

	include_once "../lib/dao.php";
	include_once "../lib/cache.php";
	include_once "leaderboardEx.php";
	include_once "common.php";

	// bind to user if any
	$reason = null;  
	$name = null;
	
	$app_session = null;
	if (isset($_GET["app_session"])){
		$app_session = $_GET["app_session"];
	}
	$is_pro = false;
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	$is_narrow = true;
	if ($app_session == null){
		$is_narrow = false;
	}
	 	
	// render
	echo
		"<html><head>
			<title>Leaderboard | MATH WORKSHEETS | GMAT SAT GRE TOEFL VOCABULARY | FOREIGN LANGUAGES</title>
			<meta name='keywords' content='MATH WORKSHEETS GMAT SAT GRE TOEFL VOCABULARY FOREIGN LANGUAGES' />
			<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
			<link type='text/css' href='http://www.itestyou.com/view/normal.css' rel='stylesheet' />
		</head><body style='padding: 0px; margin: 0px;'>"
		.ity_mobile_tools_menu($user_id, $app_session, $_SERVER["SCRIPT_NAME"])
		.ity_render_leaderboard($_SERVER['REQUEST_URI'], null, true, false)
		.ity_render_tracking()
		."</body></html>";	

?>