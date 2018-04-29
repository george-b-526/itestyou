<?php

	 function ity_rel_time($tm,$rcs = 0) {
		$cur_tm = time(); $dif = $cur_tm-$tm;
		$pds = array('second','minute','hour','day','week','month','year','decade');
		$lngh = array(1,60,3600,86400,604800,2630880,31570560,315705600);
		for($v = sizeof($lngh)-1; ($v >= 0)&&(($no = $dif/$lngh[$v])<=1); $v--); if($v < 0) $v = 0; $_tm = $cur_tm-($dif%$lngh[$v]);
	   
		$no = floor($no); if($no <> 1) $pds[$v] .='s'; $x=sprintf("%d %s",$no,$pds[$v]);
		if(($rcs == 1)&&($v >= 1)&&(($cur_tm-$_tm) > 0)) $x .= time_ago($_tm);
		return $x;
	}

	function ity_get_total_in_bucket($dbh, $table_name, $time_field_name, $select_field_name, $where = ""){
		$cutoff = time() - 60 * 60 * 24;
		$qry = "SELECT ".$select_field_name." AS COUNT FROM ".$table_name." WHERE UNIX_TIMESTAMP(".$time_field_name.") > ".$cutoff." ".$where;
		$qry = ity_qry($dbh, $qry);
		$row = mysql_fetch_assoc($qry);
		return $row['COUNT'];
	}

	function ity_get_cus_by_bucket($dbh, $min_per_bucket, $backet_count){
		$window_size = "60 * ".$min_per_bucket;

		$term = "FROM_UNIXTIME((".$window_size.") * FLOOR(UNIX_TIMESTAMP(CUS_CREATED_ON) / (".$window_size.")))";

		return ity_qry($dbh,
		  "SELECT CUS_CREATED_ON, UNIX_TIMESTAMP(CUS_CREATED_ON) AS MILLIS, TIME(".$term.") AS BIN, COUNT(*) / ".$min_per_bucket." AS COUNT
			FROM ITY_IDENTITY.CUS_CUSTOMER
			WHERE UNIX_TIMESTAMP(CUS_CREATED_ON) > %s
			GROUP BY ".$term."
			ORDER BY CUS_ID DESC",
		  array(time() - $backet_count * 60 * $min_per_bucket)
		);
	}

    function ity_get_req_by_bucket($dbh, $min_per_bucket, $backet_count){
		$window_size = "60 * ".$min_per_bucket;

		$term = "FROM_UNIXTIME((".$window_size.") * FLOOR(UNIX_TIMESTAMP(REQ_RECEIVED_ON) / (".$window_size.")))";

		return ity_qry($dbh,
		  "SELECT REQ_RECEIVED_ON, UNIX_TIMESTAMP(REQ_RECEIVED_ON) AS MILLIS, TIME(".$term.") AS BIN, COUNT(*) / ".$min_per_bucket." AS COUNT
			FROM ITY_RUNTIME.REQ_RESPONSE_QUEUE
			WHERE UNIX_TIMESTAMP(REQ_RECEIVED_ON) > %s
			GROUP BY ".$term."
			ORDER BY REQ_ID DESC",
		  array(time() - $backet_count * 60 * $min_per_bucket)
		);
	}

	function ity_result_set_to_chart_data($qry, $format, $scale = 1){
		$now = time();
		$data = $now."\n0\n".date($format, $now).";";
		while($row = mysql_fetch_assoc($qry)){
			$point = $row['MILLIS']."\n".($row['COUNT'] * $scale)."\n".date($format, $row['MILLIS']).";";
			$data = $point.$data;
		}
		return $data;
	}

	function ity_embed_chart_data($title, $data, $style = "width: 100%;"){
		return "<img style='".htmlentities($style)."' src='/view/line_chart.php?title=".urlencode($title)."&data=".base64_encode($data)."' />\n";
	}

	include_once "../lib/dao.php";
  
	// bind to user
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
		return;
	}

	// admin only
	if ($user_id != 1000){
		echo "Access denied.";
		return;
	}

	// params
	$cut_off_h = 24;
	$cut_off_l = time() - 60*60*$cut_off_h;
	$cut_off_s = date("Y-m-d H:i:s", $cut_off_l);
	$cut_off_d = date("Y-m-d H:%", $cut_off_l);

	// db
	$dbh = ity_db();

	$charts = "";

	// chart
	$qry = ity_get_req_by_bucket($dbh, 1, 180);
	$data = ity_result_set_to_chart_data($qry, "H:i");
	$charts .= ity_embed_chart_data("last 3 hours RESPONSES RECEIVED / min", $data);

	// chart
	$qry = ity_get_req_by_bucket($dbh, 240, 24 * 7);
	$data = ity_result_set_to_chart_data($qry, "m/d");
	$charts .= ity_embed_chart_data("last 4 weeks RESPONSES RECEIVED / min", $data);

	// chart
	$qry = ity_get_cus_by_bucket($dbh, 240, 24 * 7);
	$data = ity_result_set_to_chart_data($qry, "m/d", 60);
	$charts .= ity_embed_chart_data("last 4 weeks ACCOUNTS CREATED / hour", $data);

	// votes detail
	{
		$votes = "<p style='padding-left: 16px;'>";
		$qry = ity_qry($dbh,
		  "SELECT *, COUNT(*) AS COUNT, REQ_RECEIVED_ON FROM ITY_RUNTIME.REQ_RESPONSE_QUEUE AS A 
		  LEFT JOIN ITY_IDENTITY.CUS_CUSTOMER AS B ON B.CUS_ID = A.REQ_USR_ID
		  WHERE A.REQ_RECEIVED_ON > '%s'
		  GROUP BY REQ_USR_ID
		  ORDER BY COUNT(*) DESC",
		  array($cut_off_d)
		);
		$map = array();
		$i = 0;
		$vtotal = 0;
		while($row = mysql_fetch_assoc($qry)){
			$map[$row['REQ_USR_ID']] = $row['COUNT'];
			if ($i < 50){
				$votes .= "<b>".$row['COUNT']."</b>:".$row['REQ_USR_ID']." \n";
			}
			$vtotal += $row['COUNT'];
			$i++;
		}
		$votes .= "</p>";
	}

	// query
	{
		$users = "<p style='padding-left: 16px;'>";
		$qry = ity_qry($dbh,
		  "SELECT *, B.DEA_USER_AGENT AS AGENT FROM ITY_IDENTITY.CUS_CUSTOMER 
		  LEFT JOIN ITY_IDENTITY.DEA_DEVICE_ACTIVATION AS B ON B.DEA_CUS_ID = CUS_ID
		  WHERE CUS_CREATED_ON > '%s' 
		  GROUP BY CUS_ID
		  ORDER BY CUS_ID DESC",
		  array($cut_off_d)
		);
		$android = 0;
		$apple = 0;
		$utotal = 0;
		while($row = mysql_fetch_assoc($qry)){
			$agent = explode(";", $row['AGENT']);
			
			$count = 0;
			if (isset($map[$row['CUS_ID']])){
				$count = $map[$row['CUS_ID']];
			}
		
			$id = $row['CUS_ID'];
			if (!$count){
				$count = 0;
				$id = "<b style='color: red;'>".$id."</b>";
			} else {
				if ($count >= 5){
					$id = "<b style='color: blue;'>".$id."</b>";
				}
			}
			if ($utotal < 50){
				$from = "";

				// extract app type
				$terms = explode("CFNetwork", $row['DEA_USER_AGENT']);
				if (count($terms) >= 2){
					$apple++;
					$from = "CFNetwork: ".$terms[0];
				} else {
					$terms = explode("android", $row['DEA_USER_AGENT']);
					if (count($terms) >= 2){
						$android++;
						$from = "android: ".$terms[0];
					} else {
						$from = $row['DEA_USER_AGENT'];
					}
				}

				$users .= "<b>".$id."</b>:".$count."[".$row['CUS_NAME']."]".$from."&nbsp;&nbsp;&nbsp; ";
			}
			$utotal++;
		}
		$users.= "</p>";
	}

	// latest
	$latest = "";

	// last join
	$qry = ity_qry($dbh,
	  "SELECT *, UNIX_TIMESTAMP(CUS_CREATED_ON) AS CUS_CREATED_ON FROM ITY_IDENTITY.CUS_CUSTOMER ORDER BY CUS_ID DESC LIMIT 1",
	  array($cut_off_d)
	);
	$row = mysql_fetch_assoc($qry);
	$latest .= "<b><u>Now</u></b> last user joined (<b>".ity_rel_time($row['CUS_CREATED_ON'])."</b> ago)";

	// last vote
	$qry = ity_qry($dbh,
	  "SELECT *, UNIX_TIMESTAMP(REQ_RECEIVED_ON) AS REQ_RECEIVED_ON FROM ITY_RUNTIME.REQ_RESPONSE_QUEUE ORDER BY REQ_ID DESC LIMIT 1",
	  array($cut_off_d)
	);
	$row = mysql_fetch_assoc($qry);
	$latest.= ", last unit response received (<b>".ity_rel_time($row['REQ_RECEIVED_ON'])."</b> ago)";

	// today
	$today = "<u><b>Summary (24h)</b></u> ";
	$today.= "votes received (<b>".ity_get_total_in_bucket($dbh, "ITY_RUNTIME.REQ_RESPONSE_QUEUE", "REQ_RECEIVED_ON", "COUNT(*)", "")."</b>)";

	// lang
	$today.= ", lang (";
	$qry = ity_qry($dbh,
	  "SELECT REQ_LOCALE, COUNT(REQ_LOCALE) AS COUNT FROM ITY_RUNTIME.REQ_RESPONSE_QUEUE WHERE UNIX_TIMESTAMP(REQ_RECEIVED_ON) > %s GROUP BY REQ_LOCALE",
	  array($cut_off_l)
	);
	while($row = mysql_fetch_assoc($qry)) {
		$today.= $row["REQ_LOCALE"].":<b>".$row["COUNT"]."</b>; ";
	}
	$today.= ")";

	$today.= ", users joined (<b>".ity_get_total_in_bucket($dbh, "ITY_IDENTITY.CUS_CUSTOMER", "CUS_CREATED_ON", "COUNT(*)")."</b>)";
	$today.= ", users played (<b>".ity_get_total_in_bucket($dbh, "ITY_RUNTIME.REQ_RESPONSE_QUEUE", "REQ_RECEIVED_ON", "COUNT(DISTINCT(REQ_USR_ID))")."</b>)";
	$today.= ", 'first use help' emails sent (<b>".ity_get_total_in_bucket($dbh, "ITY_RUNTIME.EVT_EVENT", "EVT_STATE_CHANGE_ON", "COUNT(*)", " AND EVT_TYPE = 2 AND EVT_STATE = 2")."</b>)";
	$today.= ", 'we missed you' emails sent (<b>".ity_get_total_in_bucket($dbh, "ITY_RUNTIME.EVT_EVENT", "EVT_STATE_CHANGE_ON", "COUNT(*)", " AND EVT_TYPE = 3 AND EVT_STATE = 2")."</b>)";

	$qry = ity_qry($dbh, "SELECT COUNT(*) AS COUNT FROM ITY_ADMIN.UNI_UNIT WHERE UNI_STATE = 1 AND UNI_OWNER_USR_ID != 1", array());
	$row = mysql_fetch_assoc($qry);
	$today.= ", my vocb (<b>".$row["COUNT"]."</b>)";

	// even queue type 2
	$qry = ity_qry($dbh,
	  "SELECT COUNT(*) AS COUNT FROM ITY_RUNTIME.EVT_EVENT WHERE EVT_TYPE = 2 AND EVT_STATE = 0",
	  array()
	);
	$row = mysql_fetch_assoc($qry);
	$latest.= ", 'first use help' emails to send (<b>".$row['COUNT']."</b>)";

	// even queue type 3
	$qry = ity_qry($dbh,
	  "SELECT COUNT(*) AS COUNT FROM ITY_RUNTIME.EVT_EVENT WHERE EVT_TYPE = 3 AND EVT_STATE = 0",
	  array()
	);
	$row = mysql_fetch_assoc($qry);
	$latest.= ", 'we missed you' emails to send (<b>".$row['COUNT']."</b>)";

	// render
	$body = $latest."<br />".$today."<br />";
	$body .= "<b><u>Votes</u></b><br />".$votes;
	$body .= "<b><u>Users (Android: ".$android.", iOS: ".$apple.")</u></b><br />".$users;
	$body .= "<hr />".$charts;

	// html
	echo "  
		<html>
			<head>
				<title>ITestYou Real Time Monitoring Console</title>
				<link type='text/css' href='http://www.itestyou.com/view/normal.css' rel='stylesheet' />
				<link type='text/css' href='http://www.itestyou.com/cms/wp-content/themes/ityx/style.css' rel='stylesheet' />			
			</head>
			<body style='padding: 4px; margin: 0px;'>
				<p align='center'><b>ITestYou Real Time Monitoring Console</b> [".date(DATE_RFC822)."] ".$name."</p>
				<p align='center' class='ity-hot' style='border: solid 1px #C0C0C0; padding: 6px; background-color: #C0C0FF;'>
					<a href='/nimda/perf.php'>SNMP Counters</a>
					<a href='/nimda/webalizer/index.html'>Webalizer</a>
					<a href='/nimda/logs.php'>Logs</a>
					<a href='/admin/bin/view'>Content Admin</a>
					&nbsp;&nbsp;&nbsp;
					<a href='/view/admin.php?app_session=".htmlentities($app_session)."'>Refresh</a>
				</p>
				".$body."
				<p align='center'><small>last update on ".ity_pst_time_format()."</small></p>
			</body>
		</html>";

?>