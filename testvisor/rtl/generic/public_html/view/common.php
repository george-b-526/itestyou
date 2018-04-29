<?php

function ity_mobile_tools_menu($user_id, $app_session, $ctx = ""){
	$referer = "";
	if (isset($_GET["inReferer"])){
		$referer = $_GET["inReferer"];
	}

	$admin = "";
	if ($user_id == 1000){
		$admin = "\n&nbsp;&nbsp;&nbsp;<a href='/view/admin.php?app_session=".htmlentities($app_session)."'><img border='0' src='/img/tools/admin_btn.png' /></a>";
	}

	return 
		"<div align='center' style='margin: 4px; padding: 4px; border-bottom: solid 1px #C0C0C0;'>
			<a href='http://www.facebook.com/ITestYou' target='_blank'><img border='0' src='/img/tools/fb_btn.png' /></a>
			&nbsp;&nbsp;
			<a href='https://twitter.com/#!/ITestYou' target='_blank'><img border='0' src='/img/tools/twit_btn.png' /></a>
			&nbsp;&nbsp;
			<a href='https://plus.google.com/u/0/b/100299113449260023450/' target='_blank'><img border='0' src='/img/tools/gplus_btn.png' /></a>
			&nbsp;&nbsp;
			<a href='/view/progress?app_session=".htmlentities($app_session)."&inReferer=".htmlentities($referer)."'><img border='0' src='/img/tools/my-progress.png' /></a>
			&nbsp;&nbsp;
			<a href='/view/leaderboard?app_session=".htmlentities($app_session)."&inReferer=".htmlentities($referer)."'><img border='0' src='/img/tools/leaders.png' /></a>
			&nbsp;&nbsp;
			<a href='/view/shout?app_session=".htmlentities($app_session)."&inCtx=".htmlentities($ctx)."&inReferer=".htmlentities($referer)."'><img border='0' src='/img/tools/shout-ex.png' /></a>"
			.$admin.
		"</div>";
}

function ity_ad_narrow(){
	$body = "
		<p align='center'><script type='text/javascript'><!--
			google_ad_client = 'pub-8465789318606144';
			google_ad_width = 300;
			google_ad_height = 250;
			google_ad_format = '300x250_as';
			google_ad_channel = '7778573816';
			google_color_border = '99FF99';
			google_color_bg = 'FFFFFF';
			google_color_link = '0033AA';
			google_color_text = '000000';
			google_color_url = '000000';
		//--></script>
		<script type='text/javascript' src='http://pagead2.googlesyndication.com/pagead/show_ads.js'></script></p>";
		
	return $body;
}

function ity_ad_medium(){
	$body = "
		<p align='center'><script type='text/javascript'><!--
			google_ad_client = 'pub-8465789318606144';
			google_ad_width = 468;
			google_ad_height = 60;
			google_ad_format = '468x60_as';
			google_ad_channel = '7778573816';
			google_color_border = '99FF99';
			google_color_bg = 'FFFFFF';
			google_color_link = '0033AA';
			google_color_text = '000000';
			google_color_url = '000000';
		//--></script>
		<script type='text/javascript' src='http://pagead2.googlesyndication.com/pagead/show_ads.js'></script></p>";
		
	return $body;
}

function ity_ad_mobile(){
	return
		"<script type='text/javascript'><!--
		// XHTML should not attempt to parse these strings, declare them CDATA.
		  /* <![CDATA[ */
		  window.googleAfmcRequest = {
		    client: 'ca-mb-pub-8465789318606144',
		    format: '320x50_mb',
		    output: 'html',
		    slotname: '4431150778',
		  };
		  /* ]]> */
		//--></script>
		<script type='text/javascript' src='http://pagead2.googlesyndication.com/pagead/show_afmc_ads.js'></script>";
}

function ity_view_pass_praise($count){
	$praise = " More practice needed!";

	if ($count > 30){
		$praise = " Keep going!";
	}
	if ($count > 50){
		$praise = " Great job!";
	}
	if ($count > 100){
		$praise = " Amazing job!";
	}

	return $praise;
}

function ity_parseUnitData($udata, &$pass, &$fail, &$id2array){
	if ($udata == ""){
		return;
	}
	
	$line = substr($udata, 1, strlen($udata) - 2);
	$parts = explode(",", $line);
	for ($j=0; $j < count($parts); $j++){
		$terms = explode(":", $parts[$j]);		
		$ids[$terms[0]] = $terms[1];

		$id = $terms[0];
		$sz = $terms[1];

		if($sz > 0){
			$id2array[$id] = &$pass;
			$pass[$id] = $sz;
		} else {			
			$id2array[$id] = &$fail;
			$fail[$id] = $sz;
		}
	}
}

function ity_parseUnitXml($xml, $id2array, &$pass, &$fail, &$tblp, &$tblf, &$ipass, &$ifail, &$sz, &$title){
	$z = new XMLReader();
	$z->xml($xml);
	$title = "Untitled";

	$pbuf = array();
	$fbuf = array();

	$char = "";
	while ($z->read()){
		if ($z->name == "vocb"){
			$title = $z->getAttribute("name");
			continue;
		}
		
		if ($z->name == "w"){
			$sz++;

			$e = $z->getAttribute("e");
			$id = $z->getAttribute("i");
			$m = $z->getAttribute("m");
			$t = $z->getAttribute("t");

			if (isset($id2array[$id])){				
				$target = $id2array[$id];
				if ($target === $pass){
					$ipass++;
					$pbuf[$e] = "<td><a href='#".$id."'></a><b>".$e."</b></td><td>".$t."</td><td>".$m."</td>";		
					continue;
				} 
				
				if ($target === $fail){
					$ifail++;
					$fbuf[$e] = "<td><a href='#".$id."'></a><b>".$e."</b></td><td>".$t."</td><td>".$m."</td>";
					continue;
				}			
			}				
		}
	}

	// sort
	ksort($pbuf);
	ksort($fbuf);

	// render
	foreach($fbuf as $e => $value){
		$tblf .= "<tr>".$value."</tr>";
	}

	// render
	$char = null;
	$i = 0;
	foreach($pbuf as $e => $value){
		$style = "";
		$first = substr($e, 0, 1);
		if ($first != $char){
			if ($i != 1){
				$style = " style='border-top: solid 2px #99FF99;'";
			}
			$char = $first;
		}

		$tblp .= "<tr".$style.">".$value."</tr>";

		$i++;
	}

}

function ity_view_goal_measure($dbh, $uni_id, $user_id, &$total_week_count, &$total_pass_count, &$level_reached){
	$qry = ity_qry($dbh,
	   "SELECT UUS_DISTINCT_COUNT AS PASS_COUNT,
		(SELECT COUNT(*) FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY WHERE UUR_USR_ID = %s AND UUR_UNI_ID = %s) AS WEEK_COUNT
		FROM ITY_RUNTIME.UUS_UNIT_ROLLUP_TOP
		WHERE UUS_USR_ID = %s AND UUS_UNI_ID = %s", 
	  array($user_id, $uni_id, $user_id, $uni_id) 
	);

	if ($row = mysql_fetch_assoc($qry)) {
		$total_pass_count = $row['PASS_COUNT'];
		$total_week_count = $row['WEEK_COUNT'];
	}
}

function ity_view_pass_table_hdr($week_start, $pass_count, $rows){
	return 
		"<table border='1' class='myscore'>
		<tr class='hdr' style='background-color: #99FF99;'>
		<td class='l' colspan='3' valign='top' style='padding: 4px;'>Words learned this week: <span style='font-size: larger;'>".$pass_count."</span>. ".ity_view_pass_praise($pass_count)."</td>
		</tr>"
		.$rows
		."</table>";
}

function ity_view_fail_table_hdr($week_start, $fail_count, $play_action, $rows){
	return 
		"<table border='1' class='myscore'>
		<tr class='hdr' style='background-color: #FFFF99;'>
		<td class='l' colspan='3' valign='top' style='padding: 4px;'>Words to learn now: <span style='font-size: larger;'>".$fail_count."</span><div style='float: right;'>".$play_action."</div></td>
		</tr>"
		.$rows
		."</table>";
}

function ity_view_goals_table_hdr($week_start, $week_count, $progress_count, $total_count, $unit_type = 1){
	$num = 0;
	if ($total_count != 0){
		$num = (100 * $progress_count) / $total_count;
	}

	$level_reached = 1;
	if ($unit_type == 1) {
		$level_reached = (int) ($progress_count / 3);
	}
	if ($unit_type == 2) {
		$level_reached = (int) ($progress_count / 10);
	}

	return
		"<table border='1' class='myscore'>
		<tr class='hdr' style='background-color: #FFC285;'>
		<td class='l' colspan='3' valign='top' style='padding: px; padding-top: 16px; text-align: center;'><span style='display: block; font-size: 200%;'>You are on level <b>".$level_reached."</b>!</span><br />You practiced for <span style='font-size: larger;'>".$week_count."</span> weeks and know <span style='font-size: larger;'>".$progress_count."</span> of <span style='font-size: larger;'>".$total_count."</span> words (about <span style='font-size: larger;'>".number_format($num, 0)."%</span>). Keep working towards your goal!</td>
		</tr>"
		."</table>";
}

?>