<?php

	function ity_make_in($fieldName, $unitIds){
		$in = "";
		if (count($unitIds) != 0){
			$in = " AND ".$fieldName." (";
			for($i=0; $i < count($unitIds); $i++){
				if ($i != 0){
					$in .= ",";
				}
				$in .= $unitIds[$i];
			}
			$in .= ")";
		}
		return $in;
	}

	function ity_rollup($where, $edition, $whereEx){
		return "
			SELECT
			  UUR_USR_ID, CUS_ID, B.CUS_NAME AS NAME, SUM(UUR_CORRECT_COUNT) AS C, SUM(UUR_INCORRECT_COUNT) AS I,			  
			   (SUM(UUR_CORRECT_COUNT * C.UNI_GRADE) * (1 - (SUM(UUR_INCORRECT_COUNT * C.UNI_GRADE))/(SUM(UUR_CORRECT_COUNT * C.UNI_GRADE) + SUM(UUR_INCORRECT_COUNT * C.UNI_GRADE))))  AS S
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY AS A
				LEFT JOIN ITY_IDENTITY.CUS_CUSTOMER AS B ON B.CUS_ID = A.UUR_USR_ID
				LEFT JOIN ITY_ADMIN.UNI_UNIT AS C ON C.UNI_ID = A.UUR_UNI_ID
			WHERE UUR_USR_ID > 100 ".$whereEx." ".$where."
			GROUP BY UUR_USR_ID
			ORDER BY (SUM(UUR_CORRECT_COUNT * C.UNI_GRADE) * (1 - (SUM(UUR_INCORRECT_COUNT * C.UNI_GRADE))/(SUM(UUR_CORRECT_COUNT * C.UNI_GRADE) + SUM(UUR_INCORRECT_COUNT * C.UNI_GRADE)))) DESC
			LIMIT 10
		";
	}

	function ity_table($title, $query){
		$dbh = ity_db();
		$qry = ity_qry($dbh, $query, array(""));

		$body = "<table border='1' class='myscore' style='width: 100%;'>";

		// header row	
		$body .= "<tr class='hdr'>";
		$body .= "<td>".$title."</td>";
		$body .= "<td class='c'>Pass / Fail</td>";
		$body .= "<td class='r'>Score</td>";
		$body .= "</tr>";


		// data rows	
		while($row = mysql_fetch_assoc($qry)){
			$body .= "<tr id='id-".md5($row['CUS_ID'])."'>";
			$body .= "<td>".ity_email_obfuscate($row['NAME'])."</td>";
			$body .= "<td class='c'>".$row['C']." / ".$row['I']."</td>";
			$body .= "<td class='r'>".number_format($row['S'], 1, '.', ',')."</td>";
			$body .= "</tr>";		
		}

		$body .= "</table>";

		return $body;
	}

	function ity_email_obfuscate($name){
		$mask = "*";

		$split = explode("@", $name);
		$uname = $split[0];
		$dname = $split[1];
		$split = explode(".", $dname);
		$dname = $split[0];
		$com = "com";
		if (count($split) == 2){
			$com = $split[1];
		}

		$uname[strlen($uname) - 1] = $mask;

		if (strlen($uname) > 6){
			$uname[strlen($uname) - 2] = $mask;
			$uname[strlen($uname) - 3] = $mask;

			if (strlen($uname) > 12){
				$uname[strlen($uname) - 4] = $mask;
				$uname[strlen($uname) - 5] = $mask;
			}
		}

		for ($i = 0; $i < strlen($dname); $i++){
			$dname[$i] = $mask;
		}
		
		return $uname."@".$dname.".".$com;
	}

	function ity_enum_unit_by_type(&$vocb, $whereEx){
		$dbh = ity_db();
		$qry = ity_qry($dbh, 
			"SELECT * FROM ITY_ADMIN.UNI_UNIT WHERE UNI_STATE = 1 ".$whereEx." ORDER BY UNI_TITLE", array(""));
		while($row = mysql_fetch_assoc($qry)){
			$vocb[$row['UNI_ID']] = $row['UNI_TITLE'];
		}
	}

	function ity_top_render($base_url, $edition, $has_links, $has_title = true, $narrow = false, $app_session = null, $user_id = null, $is_pro = false){
		$now = ity_pst_time();
		ity_date_wy_ex($now, $now_year, $now_week, $prior_week, $prior_year, $next_year, $next_week);
		$week_start = date("M d, Y", ity_date_week_start($now_week, $now_year));
		
		// ads 
		$ads_marker = "<a href='ity-ads'></a>";
		$ads_markerEx = "<a href='ity-ads-ex'></a>";

		// edition
		if ($edition == null){
			$edition = ity_get_edition($has_edition);		
		} else {
			$has_edition = true;
		}
		if (!$has_edition){
			$ns = "all";
		} else {
			$ns = $edition;
		}

		// cache
		$name = "/oy/testvisor/tmp/cache/cms-win-ex-".$ns."-".$now_week."-".$now_year;
		if ($has_title){
			$name .= "-title";
		} else {
			$name .= "-no_title";
		}
		if ($has_links){
			$name .= "-links";
		} else {
			$name .= "-no_links";
		}
		if ($narrow){
			$name .= "-narrow";
		} else {
			$name .= "-medium";
		}
		$name .= ".html";

		// render
		$body = ity_cache_get($name, 1);	
		if ($body == null){			
			$date_filter = "AND A.UUR_YEAR=".$now_year." AND A.UUR_WEEK=".$now_week;
			$body = "";

			// math
			if ($edition == "math" || !$has_edition){
				$whereEx = " AND UNI_TYPE = 1";
				$title = "Math Worksheets Overall Best";
				if ($has_links){
					$title = "<a href='http://www.itestyou.com/cms/math-worksheets-leaderboard'>".$title."</a>";
				}
				$body .= ity_table($title, ity_rollup ($date_filter, "math", $whereEx));

				// ads 
				$body .= $ads_marker;

				// per grade
				if ($has_edition){
					for ($i = 8; $i > 0; $i--){
						$body .= "<br />";
						$caption = "Best in Grade ".$i." Math";
						if ($has_links){
							$caption = "Best in <a href='http://www.itestyou.com/cms/category/math-grade-".$i."'>Grade ".$i." Math</a>";
						}
						
						$body .=  ity_table($caption, ity_rollup ($date_filter." AND C.UNI_GRADE = ".$i, "math", $whereEx));
					}
				}

				$body .= "<br />";
			} 

			// vocb
			if ($edition == "vocb" || !$has_edition){
				$whereEx = " AND UNI_ID IN (115,116,117,121,164,231) AND UNI_TYPE = 2";
				$title = "English Vocabulary Overall Best";
				if ($has_links){
					$title = "<a href='http://www.itestyou.com/cms/english-vocabulary-leaderboard'>".$title."</a>";
				}
				$body .= ity_table($title, ity_rollup ($date_filter, "vocb", $whereEx));

				// ads 
				$body .= $ads_marker;

				// per unit
				if ($has_edition){
					$vocbs = array();
					ity_enum_unit_by_type($vocbs, $whereEx);
					foreach ($vocbs as $uid => $utitle) {
						$body .= "<br />";
						$caption = $utitle;
						if ($has_links){
							$caption = "Best in <a href='http://www.itestyou.com/cms/unit-".$uid."'>".$utitle."</a>";
						}
						$body .=  ity_table($caption, ity_rollup ($date_filter." AND C.UNI_ID = ".$uid, "vocb", $whereEx));
					}
				}

				$body .= "<br />";
			}

			// lang
			if ($edition == "lang" || !$has_edition){
				$whereEx = " AND (UNI_ID  >= 122 AND UNI_ID <= 158) AND UNI_TYPE = 2";
				$title = "Foreign Languages Overall Best";
				if ($has_links){
					$title = "<a href='http://www.itestyou.com/cms/foreign-languages-leaderboard'>".$title."</a>";
				}
				$body .= ity_table($title, ity_rollup ($date_filter, "vocb", $whereEx));

				// ads 
				$body .= $ads_marker;
				
				// per unit
				if ($has_edition){
					$vocbs = array();
					ity_enum_unit_by_type($vocbs, $whereEx);
					foreach ($vocbs as $uid => $utitle) {
						$body .= "<br />";
						$caption = $utitle;
						if ($has_links){
							$caption = "Best in <a href='http://www.itestyou.com/cms/unit-".$uid."'>".$utitle."</a>";
						}
						$body .=  ity_table($caption, ity_rollup ($date_filter." AND C.UNI_ID = ".$uid, "vocb", $whereEx));
					}
				}

				$body .= "<br />";
			}

			// get time for PST
			$update_time = date("D, M j Y G:i:s", $now)." PST";

			// params
			$paramEx = "";
			if ($app_session != null){
				$paramEx = "<input type='hidden' name='app_session' value='".$app_session."' />";
			}

			// title
			$title = "Math Worksheets";
			if ($edition == "vocb"){
				$title = "Vocabulary Builder";
			} else {
				if ($edition == "lang"){
					$title = "Foreign Languages";
				}
			}

			// referer
			$referer_param = "";
			if (isset($_GET["inReferer"])){
				$referer_param = "<input type='hidden' name='inReferer' value='".htmlentities($_GET["inReferer"])."' />";
			}

			// prev/next
			$btn_prev = "<form method='GET' action='".$base_url."'>".$paramEx."<input type='hidden' name='ity_year' value='".$prior_year."' /><input type='hidden' name='ity_week' value='".$prior_week."' /><input class='vote' type='submit' value=' &lt;&lt; '>".$referer_param."</form>";
			$btn_next = "<form method='GET' action='".$base_url."'>".$paramEx."<input type='hidden' name='ity_year' value='".$next_year."' /><input type='hidden' name='ity_week' value='".$next_week."' /><input class='vote' type='submit' value=' &gt;&gt; '>".$referer_param."</form>";
			
			// hide too distant past and all future buttons
			ity_date_wy_no_params(time(), $now_year_x, $now_week_x, $prior_week_x, $prior_year_x, $next_year_x, $next_week_x);
			if ($prior_year <= 2011 && $prior_week < 36){
				$btn_prev = "";
			}
			if (($next_year >= $now_year_x && $next_week > $now_week_x)){
				$btn_next = "";
			}

			// render
			$body = 
				"".($has_title ? "<h2 align='center'>".$title." Leaderboard</h2>" : "")."
						<p align='center'>
						<div style='width: 100%; text-align: center;'>"
							.$btn_prev
							."&nbsp;week of ".$week_start."&nbsp;"
							.$btn_next
					  ."</div>						
						</p>
						<div class='text'>".$body."</div>
				".$ads_markerEx."
				<div style='padding: 4px;'>
				<p><b>How do we compute the scores?</b> We use the following formula:
					<p align='center'><img style='text-align: center;' src='/api/ml/math?\\sum_{grades} (1 - \\frac{I}{I+C})\\times C\\times G' /></p>
					<p>, where:<br /><br />
					<b><code>C</code></b> - is the number of correct answers,<br />
					<b><code>I</code></b> - is the number of incorrect answers, and <br />
					<b><code>G</code></b> - is the grade number.</p><br />

				<p><b>How often is the leaderboard updated?</b> 
					The leaderboard runs for one week and is updated every 5 minutes. The leaderboard week starts on Monday and ends at midnight on Sunday.   
				</p>
				<p align='center'><small>last updated on ".$update_time."</small></p>
				</div>";

			ity_cache_put($name, $body);
		}
 
		// style
		$style = "";
		if ($user_id != null){
			$style = 
				"<style>
				TR#id-".md5($user_id)."{
					background-color: #99FF99;
				}
				</style>";
		}

		// ads
		$ads = "";
		$adsEx = "";
		if (!$is_pro && !$has_links){
			//$ads = "<br />".ity_ad_mobile();
			//$adsEx = "<br />".ity_ad_narrow();
		}
		$body = str_replace($ads_marker, $ads, $body);
		$body = str_replace($ads_markerEx, $adsEx, $body);

		return $style.$body;
	}

	function ity_render_leaderboard($base_url, $edition, $has_title, $has_links = false){
		include_once "../lib/dao.php";
		include_once "../lib/cache.php";
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
		
		return ity_top_render($base_url, $edition, $has_links, $has_title, $is_narrow, $app_session, $user_id, $is_pro);
	}

?>