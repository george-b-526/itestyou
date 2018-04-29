<?php

	include_once "../lib/dao.php";
	include_once "../lib/cache.php";
	include_once "common.php";

	// bind to user
	$reason = null;   
	$name = null;
	$is_pro = null;
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

	$referer = "";
	if (isset($_GET["inReferer"])){
		$referer = htmlentities($_GET["inReferer"]);
	}

	// params
	$uni_id = (int) $_GET['uni_id'];
	
	$now = ity_pst_time();
	ity_date_wy($now, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	$week_start = date("M d, Y", ity_date_week_start($now_week, $now_year));

	// action url
	$year_action = "/view/year.php";
	if ($app_session == null){
		$year_action = "/learn/Content:Year";
	}

	// play action
	$play_action = 
		"<form style='display: inline;' method='GET' action='http://www.itestyou.com/test/vocb'>
		<input type='submit' value=' Practice ' />
		<input type='hidden' name='app_session' value='".$app_session."' />
		<input type='hidden' name='action_id' value='0' />
		<input type='hidden' name='inUnitId' value='".$uni_id."' />
		<input type='hidden' name='inReferer' value='".$referer."' />
		</form>";

	// body
	$body = null;

	// db
	$dbh = ity_db();

	// get unit data
	{
		$qry = ity_qry($dbh,
		  "SELECT UNCOMPRESS(UUR_UNIT_DATA) AS UNIT_DATA 
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY 
			WHERE UUR_UNI_ID = %s AND UUR_USR_ID = %s AND UUR_YEAR = '%s' AND UUR_WEEK = '%s'",
		  array($uni_id, $user_id, $now_year, $now_week) 
		);
		$row = mysql_fetch_assoc($qry);
		$udata = $row['UNIT_DATA'];

		// parse unit data
		$pass = array();
		$fail = array();
		$id2array = array();
		ity_parseUnitData($udata, $pass, $fail, $id2array);
	}

	// get unit XML
	{
		$qry = ity_qry($dbh,
		  "SELECT UNI_TITLE, UNI_TYPE, CONVERT(UNCOMPRESS(UNI_XML) USING 'utf8') AS XML FROM ITY_ADMIN.UNI_UNIT WHERE UNI_ID = %s",
		  array($uni_id) 
		);
		$row = mysql_fetch_assoc($qry);
		$xml = $row['XML'];

		$tblp = ""; $tblf = "";
		$ipass = 0; $ifail = 0; $sz = 0;
		ity_parseUnitXml($xml, $id2array, $pass, $fail, $tblp, $tblf, $ipass, $ifail, $sz, $title);
		$title = $row['UNI_TITLE'];
	}

	// goal
	$total_week_count = 0;
	$total_pass_count = 0;
	$level_reached = 0;
	ity_view_goal_measure ($dbh, $uni_id, $user_id, $total_week_count, $total_pass_count, $level_reached);

	// headers
	$tblt = ity_view_goals_table_hdr($week_start, $total_week_count, $total_pass_count, $sz, $row['UNI_TYPE']);
	$tblf = ity_view_fail_table_hdr($week_start, $ifail, $play_action, $tblf);
	$tblp = ity_view_pass_table_hdr($week_start, $ipass, $tblp);

	//charts
	$effort = "<img style='width: 100%; max-width: 600px;' src='/view/chart/effort.php?app_session=".$app_session."&uni_id=".$uni_id."&width=400&height=200' /><br />";
	$accuracy = "<img style='width: 100%; max-width: 600px;' src='/view/chart/accuracy.php?app_session=".$app_session."&uni_id=".$uni_id."&width=400&height=200' /><br />";
	$charts = "<p align='center'>".$effort."<br />".$accuracy."</p>";

	// ads
	$ads = "";
	$adsEx = "";
	if (!$is_pro){
		//$ads = ity_ad_mobile();
		//$adsEx = ity_ad_narrow();
	}

	// render
	$body = 
		"<html>
		<head>
			<title>".htmlentities($title)."| MATH WORKSHEETS | GMAT SAT GRE TOEFL VOCABULARY | FOREIGN LANGUAGES</title>
			<meta name='keywords' content='MATH WORKSHEETS GMAT SAT GRE TOEFL VOCABULARY FOREIGN LANGUAGES' />
			<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
			<link type='text/css' href='http://www.itestyou.com/view/normal.css' rel='stylesheet' />
		</head>
		<body style='margin: 0px; padding: 0px;'>"
			.ity_mobile_tools_menu($user_id, $app_session, $_SERVER["SCRIPT_NAME"]).
			"<h2 align='center'>".htmlentities($title)."</h2>
			<p align='center'><div style='width: 100%; text-align: center; padding: 0px; margin: 0px;'>
				week of ".$week_start."   
				<form method='GET' action='".$year_action."'>
				<input type='hidden' name='app_session' value='".$app_session."' />
				<input type='hidden' name='inReferer' value='".$referer."' />
				<input class='vote' type='submit' value=' Weekly Progress ' />
				</form>
			</div></p>
			".
			$tblt."<br />".$charts."<br />".$tblf."<br />".$tblp.
			$adsEx.
			"<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>".ity_render_tracking()."
		</body>
	</html>";

	echo $body;
?>