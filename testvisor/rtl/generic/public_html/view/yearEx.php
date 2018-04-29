<?php

function ity_render_year($week_action){

	include_once "../lib/dao.php";
	include_once "../lib/cache.php";
	include_once "common.php";
  
	// bind to user
	$reason = null;  
	$name = null;
	$is_pro = null;
	$app_session = $_GET["app_session"];
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	if ($user_id == null){
		echo "<p align='center'>Please <a href='/cms/log-in'>login</a> to see this page.</p>";
		return;
	}

	// options
	$can_cache = false;
	
	// lookup
	$body = null;
	if ($can_cache){
		$fn = "/oy/testvisor/tmp/cache/year-ex-u".$user_id.".html";
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
			$body .= "<td class='c'><form method='GET' action='".$week_action."'><input type='hidden' name='app_session' value='".$app_session."' /><input type='hidden' name='ity_week' value='".$row['UUR_WEEK']."' /><input type='hidden' name='ity_year' value='".$row['UUR_YEAR']."' /><input class='vote' type='submit' value='  ".$sStartDate."  ' /></form></td>";			
			
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
		$total_score = $corr / ($corr + $icorr);

		$body .= "<tr class='hdr'>";
		$body .= "<td class='c'>".$i." practice weeks</td>";
		$body .= "<td class='c'>".$corr." / ".$icorr."</td>";
		$body .= "<td class='".ity_score_class($total_score)."'>".number_format(100 * $total_score, 0)."%</td>";
		$body .= "</tr>";

		$body .= "</table>";

		$body .= "<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>";

		if ($can_cache){
			ity_cache_put($fn, $body);
		}
	}

	return $body;
}

?>