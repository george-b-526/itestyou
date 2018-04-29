<?php

	function ity_render_vocb_progress($year_action, $vocb_action){

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

		// params
		$uni_id = (int) $_GET['uni_id'];

		// timeline
		$now = ity_pst_time();
		ity_date_wy_ex($now, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
		$week_start = date("M d, Y", ity_date_week_start($now_week, $now_year));

		// unit action
		$play_action = "<form method='GET' action='".$vocb_action.$uni_id."'><input type='submit' value=' Practice ' /></form>";

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
			  "SELECT UNI_TITLE, UNI_TYPE, UNCOMPRESS(UNI_XML) AS XML FROM ITY_ADMIN.UNI_UNIT WHERE UNI_ID = %s",
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
		$effort = "<img src='http://www.itestyou.com/view/chart/effort.php?uni_id=".$uni_id."' /><br />";
		$accuracy = "<img src='http://www.itestyou.com/view/chart/accuracy.php?uni_id=".$uni_id."' /><br />";

		// render
		$body = 
			"<h2 align='center'>".htmlentities($title)."</h2>
			<p align='center'>
			<div style='width: 100%; text-align: center;'>
				week of ".$week_start."   
				<form method='GET' action='".$year_action."'>
				<input type='hidden' name='app_session' value='".$app_session."' />
				<input class='vote' type='submit' value=' Weekly Progress '>
				</form>
			</div></p>".
			$tblt."<br />".$effort."<br />".$accuracy."<br />".$tblf."<br />".$tblp.
			"<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>";

		return $body;
	}

?>