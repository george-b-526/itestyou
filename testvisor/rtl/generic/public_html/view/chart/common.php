<?php
	include_once "../../lib/dao.php";

	function ity_chart_data_correct_incorrect($user_id){
		$dbh = ity_db();

		// unit_id
		$unit_id_where = "";
		if (isset($_GET['uni_id'])){
			$unit_id = (int) $_GET['uni_id'];
			$unit_id_where = "AND UUR_UNI_ID = ".$unit_id;
		}

		// clock
		$week_count = 26; // 6 month
		$now = time();
		$now_year = (int) date("o", $now);
		$now_week = (int) date("W", $now);
		$clock  = ity_date_week_start($now_week, $now_year);

		// timeline
		$starts = array();
		$years = array();
		$weeks = array();

		// where
		$where = "";
		for ($i=0; $i < $week_count; $i++){
			$now_year = (int) date("o", $clock);
			$now_week = (int) date("W", $clock);
			array_push($starts, $clock);
			array_push($years, $now_year);
			array_push($weeks, $now_week);

			if ($i != 0){
				$where .= " OR ";
			}
			$where .= "(UUR_YEAR = ".$now_year." AND UUR_WEEK = ".$now_week.")";

			$week = 7 * 24 * 60 * 60;
			$clock = $clock - $week;
		}

		// query
		$qry = ity_qry($dbh,
		  "SELECT UUR_YEAR, UUR_WEEK, 
		  SUM(UUR_CORRECT_COUNT) AS SUM_CORRECT, 
		  SUM(UUR_INCORRECT_COUNT) AS SUM_INCORRECT
		  FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY
		  WHERE (UUR_USR_ID = %s ".$unit_id_where.") AND (".$where.")
		  GROUP BY UUR_YEAR, UUR_WEEK
		  ORDER BY UUR_YEAR DESC, UUR_WEEK DESC
		  LIMIT ".$week_count,
		  array($user_id)
		);

		// fill
		$dataset = array();
		$i=0;
		while($row = mysql_fetch_assoc($qry)){
			$year = $row['UUR_YEAR'];
			$week = $row['UUR_WEEK'];
			$correct = $row['SUM_CORRECT'];
			$incorrect = $row['SUM_INCORRECT'];

			while($i < $week_count){
				$date = date("d / m", $starts[$i]);
				if ($year == $years[$i] && $week == $weeks[$i]){
					array_push($dataset, array($date, $incorrect, $correct));		
					$i++;
					break;
				}
				array_push($dataset, array($date, null, null));
				$i++;
			}
		}
		$dataset = array_reverse($dataset);
		
		if (count($dataset) == 0){
			array_push($dataset, array(date("d / m", time()), null, null));
		}

		return $dataset;
	}

	function ity_new_chart($title){
		include_once('../../../library/phplot-5.7.0/phplot.php');

		$w = 640;
		if (isset($_GET['width'])){
			$w = (int) $_GET['width'];
			if ($w < 0 || $w > 640){
				$w = 640;
			}
		}

		$h = 320;
		if (isset($_GET['height'])){
			$h = (int) $_GET['height'];
			if ($h < 0 || $h > 320){
				$h = 320;
			}
		}

		// mime type
		header("Content-Type: image/png");	

		// allow caching
		$expires = 60 * 1;
		header("Pragma: public");
		header("Cache-Control: maxage=".$expires);
		header('Expires: ' . gmdate('D, d M Y H:i:s', time()+$expires) . ' GMT');

		$plot = new PHPlot($w, $h);
		$plot->SetTitle($title);
		
		return $plot;
	}

	function ity_chart_render($plot){
		$plot->SetDefaultTTFont("arial");
		$plot->SetIsInline(true);
		$plot->DrawGraph();
	}

	function ity_bind_to_user(){
		$reason = null;  
		$name = null;
		$is_pro = false;

		$app_session = null;
		if (isset($_GET["app_session"])){
			$app_session = $_GET["app_session"];
		}

		$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
		if ($user_id == null){
			echo $reason;
			return null;
		}
		return $user_id;
	}
?>