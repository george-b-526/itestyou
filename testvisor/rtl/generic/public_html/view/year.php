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

	// options
	$can_cache = false;
	
	// lookup
	$body = null;
	if ($can_cache){
		$fn = "/oy/testvisor/tmp/cache/year-u".$user_id.".html";
		$body = ity_cache_get($fn, 1);
	}

	// build
	if ($body == null){
		$body = "";

		// query
		$dbh = ity_db();
		$qry = ity_qry($dbh,
		  "SELECT 
			UUR_UNI_ID, UUR_YEAR, UUR_WEEK, SUM(UUR_CORRECT_COUNT) AS UUR_CORRECT_COUNT, SUM(UUR_INCORRECT_COUNT) AS UUR_INCORRECT_COUNT
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY 
			WHERE UUR_USR_ID = '%s'
			GROUP BY UUR_YEAR, UUR_WEEK
			ORDER BY UUR_YEAR DESC, UUR_WEEK DESC
			LIMIT 52",
		  array($user_id)
		);

		// charts
		$effort = "<img style='width: 100%; max-width: 600px;' src='http://www.itestyou.com/view/chart/effort.php?app_session=".$app_session."&width=400&height=200' /><br /><br />";
		$accuracy = "<img style='width: 100%; max-width: 600px;' src='http://www.itestyou.com/view/chart/accuracy.php?app_session=".$app_session."&width=400&height=200' /><br /><br />";
		$body .= "<p align='center'>".$effort."<br />".$accuracy."</p>";

		// render
		$i = 0;
		$corr = 0;
		$icorr = 0; 
		$body .= "<table border='1' class='myscore'>";

		// header row	
		$body .= "<tr class='hdr'>";
		$body .= "<td class='c'>Week of</td>";
		$body .= "<td class='c'>Pass / Fail</td>";
		$body .= "<td class='c'>Pass Rate</td>";
		$body .= "</tr>";

		// action url
		$week_action = "/view/week.php";

		// now
		ity_date_wy_ex(time(), $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);

		// data rows	
		while($row = mysql_fetch_assoc($qry)){
			$sStartDate = date("M d, Y", ity_date_week_start($row['UUR_WEEK'], $row['UUR_YEAR']));
			if($row['UUR_WEEK'] == $now_week && $row['UUR_YEAR'] == $now_year){
				$sStartDate = "current week";
			}
			if($row['UUR_WEEK'] == $prior_week && $row['UUR_YEAR'] == $prior_year){
				$sStartDate = "  last week  ";
			}

			$score = $row['UUR_CORRECT_COUNT'] / ($row['UUR_CORRECT_COUNT'] + $row['UUR_INCORRECT_COUNT']);

			$body .= "<tr>";
			$body .= "<td class='c'><form method='GET' action='".$week_action."'><input type='hidden' name='app_session' value='".$app_session."' /><input type='hidden' name='week' value='".$row['UUR_WEEK']."' /><input type='hidden' name='year' value='".$row['UUR_YEAR']."' /><input class='vote' type='submit' value='  ".$sStartDate."  ' /></form></td>";			
			
			$body .= "<td class='c'>".$row['UUR_CORRECT_COUNT']." / ".$row['UUR_INCORRECT_COUNT']."</td>";
			$body .= "<td class='".ity_score_class($score)."'>".number_format(100 * $score, 0)."%</td>";
			$body .= "</tr>";

			$i++;
			$corr += $row['UUR_CORRECT_COUNT'];
			$icorr += $row['UUR_INCORRECT_COUNT']; 
		}

		// totals 
		$total_score = 0;
		if (($corr + $icorr) != 0){
			$total_score = $corr / ($corr + $icorr);
		}
		
		$body .= "<tr class='hdr'>";
		$body .= "<td class='c'>".$i." practice weeks</td>";
		$body .= "<td class='c'>".$corr." / ".$icorr."</td>";
		$body .= "<td class='".ity_score_class($total_score)."'>".number_format(100 * $total_score, 0)."%</td>";
		$body .= "</tr>";

		$body .= "</table>";

		// ads
		$ads = "";
		$adsEx = "";
		if (!$is_pro){
			//$ads = ity_ad_mobile();
			//$adsEx = ity_ad_narrow();
		}

		$body = 
			"<html>
			<head>
				<title>Weekly Progress | MATH WORKSHEETS | GMAT SAT GRE TOEFL VOCABULARY | FOREIGN LANGUAGES</title>
				<meta name='keywords' content='MATH WORKSHEETS GMAT SAT GRE TOEFL VOCABULARY FOREIGN LANGUAGES' />
				<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
				<link type='text/css' href='http://www.itestyou.com/view/normal.css' rel='stylesheet' />
			</head>
			<body style='padding: 0px; margin: 0px;'>"
				.ity_mobile_tools_menu($user_id, $app_session, $_SERVER["SCRIPT_NAME"]).
				"<h2 align='center'>Weekly Progress</h2>
				".$body.
				$adsEx.
				"<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>".ity_render_tracking()."
			</body>
		</html>";

		if ($can_cache){
			ity_cache_put($fn, $body);
		}
	}

	echo $body;

?>