<?php

function ity_render_week($year_action, $math_action, $vocb_action){

	global $ity_render_week_lambda;
	if ($ity_render_week_lambda){
		return $ity_render_week_lambda;
	}

	include_once "../lib/dao.php";
	include_once "../lib/cache.php";
	include_once "common.php";

	// bind to user
	$reason = null;  
	$name = null;
	$is_pro = false;
	$app_session = $_GET["app_session"];
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	if ($user_id == null){
		echo "<p align='center'>Please <a href='/cms/log-in'>login</a> to see this page.</p>";
		return;
	}

	$now = ity_pst_time();
	ity_date_wy_ex($now, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
	$week_start = date("M d, Y", ity_date_week_start($now_week, $now_year));

	// options
	$can_cache = false;

	// lookup
	$body = null;
	if ($can_cache){
		$fn = "/oy/testvisor/tmp/cache/week-ex-u".$user_id."-y".$year."-w".$week.".html";
		$body = ity_cache_get($fn, 1);
	}

	// distinct
	$distinct_unit_count = 0;
	$distinct_unit_id = 0;
	$distinct_unit_type = 0;

	// rebuild
	if ($body == null){

		// query
		$dbh = ity_db();

		$qry = ity_qry($dbh,
		  "SELECT 
			UUR_UNI_ID, UUR_YEAR, UUR_WEEK, UUR_CORRECT_COUNT, UUR_INCORRECT_COUNT,
			(UUR_CORRECT_COUNT / (UUR_CORRECT_COUNT + UUR_INCORRECT_COUNT)) AS SCORE,
			UNI_TYPE, UNI_TITLE
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY
			LEFT JOIN UNI_UNIT ON UNI_ID = UUR_UNI_ID
			WHERE UUR_USR_ID = '%s' AND UUR_YEAR = '%s' AND UUR_WEEK = '%s'
			ORDER BY SCORE ASC, (UUR_CORRECT_COUNT + UUR_INCORRECT_COUNT) ASC
			LIMIT 250",
		  array($user_id, $now_year, $now_week)
		);

		// render
		$i = 0;
		$corr = 0;
		$icorr = 0; 
		$body = "<table border='1' class='myscore'>";

		// header row
		$body .= "<tr class='hdr'>";
		$body .= "<td class='c'>Challenge Type</td>";
		$body .= "<td class='c'>Pass / Fail</td>";
		$body .= "<td class='c'>Success Rate</td>";
		$body .= "</tr>";

		// row
		while($row = mysql_fetch_assoc($qry)){
			$sStartDate = ity_week_start_date($row['UUR_WEEK'] - 1, $row['UUR_YEAR']);
			$sEndDate = strtotime('+6 days', strtotime($sStartDate)); 
			$score = $row['SCORE'];
			$type = $row['UNI_TYPE'];

			// distinct
			$distinct_unit_count++;
			$distinct_unit_id = $row['UUR_UNI_ID'];
			$distinct_unit_type = $type;

			if ($type == 1){
				// unit action
				$unit_action = "Math # ".$row['UUR_UNI_ID']."<form style='float: right;' method='GET' action='".$math_action.$row['UUR_UNI_ID']."'><input type='submit' value='  Practice ' /></form>";
			}

			if ($type == 2){				
				// unit action
				$unit_action = $row['UNI_TITLE']."<form style='float: right;' method='GET' action='".$vocb_action."'><input type='submit' value='  Review  ' />";

				$unit_action .= "<input type='hidden' name='uni_id' value='".$row['UUR_UNI_ID']."' />";
				$unit_action .= "<input type='hidden' name='ity_year' value='".$now_year."' />";
				$unit_action .= "<input type='hidden' name='ity_week' value='".$now_week."' />";
				$unit_action .= "</form>";
			}

			$body .= "<tr>";
			$body .= "<td class='c'>".$unit_action."</td>";
			$body .= "<td class='c'>".$row['UUR_CORRECT_COUNT']." / ".$row['UUR_INCORRECT_COUNT']."</td>";
			$body .= "<td class='".ity_score_class($score)."'>".number_format(100 * $score, 0)."%</td>";
			$body .= "</tr>";
			 
			$i++;
			$corr += $row['UUR_CORRECT_COUNT'];
			$icorr += $row['UUR_INCORRECT_COUNT'];
		}

		// is empty
		if ($i == 0){
			$body .= "<tr><td colspan='3' class='c'>you did not solve any challenges yet</td></tr>";
		}  

		// totals 
		$total_score = 0;
		if(($corr + $icorr) != 0){
			$total_score = ($corr) / ($corr + $icorr);
		}

		// totals
		$body .= "<tr class='hdr'>";
		$body .= "<td class='c'>".$i." challenges</td>";
		$body .= "<td class='c'>".$corr." / ".$icorr."</td>";
		$body .= "<td class='".ity_score_class($total_score)."'>".number_format(100 * $total_score, 0)."%</td>";
		$body .= "</tr>";

		$body .= "</table>";

		$body .= "<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>";

		$body =
			"<p align='center'><div style='width: 100%; text-align: center;'>
				week of ".$week_start."   
				<form method='GET' action='".$year_action."'>
				<input type='hidden' name='app_session' value='".$app_session."' />
				<input class='vote' type='submit' value=' Weekly Progress ' />
				</form>
			</div></p>"
			.$body;

		if ($can_cache){
			ity_cache_put($fn, $body);
		}
	}

	// if just one vocb unit, redirect it to the unit detailed view 
	if ($distinct_unit_count == 1 && $distinct_unit_type == 2){
		$url = $vocb_action."?uni_id=".$distinct_unit_id."&ity_year=".$now_year."&ity_week=".$now_week;
		if ($app_session != null){
			$url .= "&app_session=".$app_session;
		}
		header("Location:".$url);
		exit;
	}

	return $body;
}

?>