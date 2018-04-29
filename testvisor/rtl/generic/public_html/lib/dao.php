<?php

// define("ITY_API_HOST", "http://www.itestyou.com");
define("ITY_API_HOST", "http://127.0.0.1:8080");
define("ITY_API_TIMEOUT", 10);

date_default_timezone_set('America/Los_Angeles');

$ity_dao_debug = false;

function ity_echo_log($msg){
	error_log($msg);
	echo $msg;
}

function ity_insert_event($dbh, $usr_id, $type, $source, $data = "NULL"){
	$qry = ity_qry($dbh,
	   "INSERT INTO ITY_RUNTIME.EVT_EVENT 
		(EVT_USR_ID, EVT_TYPE, EVT_POSTED_ON, EVT_SOURCE, EVT_DATA, EVT_STATE) VALUES 
		(%s, %s, FROM_UNIXTIME(%s), '%s', COMPRESS('%s'), %s)",
		array($usr_id, $type, time(), $source, $data, 0) 
	); 
}  

function ity_put_user_properties($user_id, $props){
	$blob = "";
	foreach($props as $name => $value){
		$blob .= $name."=".$value."\n";
	}

	$dbh = ity_db();
	$qry = ity_qry($dbh,
	  "UPDATE ITY_IDENTITY.CUS_CUSTOMER SET CUS_PROPERTIES = COMPRESS('%s') WHERE CUS_ID = %s",
	  array($blob, $user_id)
	);
}

function ity_get_user_properties($user_id, &$props){
	$dbh = ity_db();
	$qry = ity_qry($dbh,
	  "SELECT UNCOMPRESS(CUS_PROPERTIES) AS CUS_PROPERTIES FROM ITY_IDENTITY.CUS_CUSTOMER WHERE CUS_ID = %s",
	  array($user_id)
	);
	$row = mysql_fetch_assoc($qry);
    ity_get_user_properties_from_row($row, $props);
}

function ity_get_user_properties_from_row($row, &$props){
	if ($row['CUS_PROPERTIES']){
		$lines = explode("\n", $row['CUS_PROPERTIES']);
		foreach($lines as $line){
			$terms = explode("=", $line);
			if (count($terms) == 2) {
				$props[$terms[0]] = $terms[1];
			}
		}
	} else {
		// defaults
		$props['notify_rank'] = 1;
		$props['notify_learn'] = 1;
		$props['notify_update'] = 1;
	}	
}

function ity_infer_is_pro(){
	if (!isset($_REQUEST["inReferer"])){
		return false;
	}

	$inReferer = $_REQUEST["inReferer"];

	if (stristr($inReferer, "-lang-pro-") !== FALSE){
		return true;
	}

	if (stristr($inReferer, "-vocb-pro-") !== FALSE){
		return true;
	}

	if (stristr($inReferer, "-ity-pro-") !== FALSE){
		return true;
	}

	return false;
}

function ity_get_edition(&$has){
	if (!isset($_REQUEST["inReferer"])){
		$has = false;
		return "math";
	}

	$inReferer = $_REQUEST["inReferer"];
	if (!$inReferer){
		$has = false;
		return "math";
	}

	$has = true;
	
	$isVocb = stristr($inReferer, "-vocb") !== FALSE;
	if($isVocb){
		return "vocb";
	}

	$isLang = stristr($inReferer, "-lang") !== FALSE;
	if($isLang){
		return "lang";
	}

	return "math";
}

function ity_pst_time(){
	// MDT to PST is one hour difference
	return time() - 60 * 60 * 1;
}

function ity_pst_time_format(){
	$now = ity_pst_time();
	return date("D, M j Y G:i:s", $now)." PST";
}

function ity_xml_response_get($body, $tag){
	if (preg_match("/<".$tag."\>(.*)<\/".$tag.">/", $body, $matches)){
		return $matches[1];
	} 
	return null;
}	

function ity_repassword($name, $oldPassword, $newPassword, &$reason){
	$url = ITY_API_HOST."/api/identity?verb=repassword&name=".urlencode($name)."&old-password=".urlencode($oldPassword)."&new-password=".urlencode($newPassword)."&device-id=".urlencode($_SERVER["SCRIPT_NAME"]);

	$ch = curl_init($url);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, ITY_API_TIMEOUT);	 
	curl_setopt($ch, CURLOPT_TIMEOUT, ITY_API_TIMEOUT); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$output = curl_exec($ch);      
    curl_close($ch);

	$status = ity_xml_response_get($output, "status");
	$reason = ity_xml_response_get($output, "reason");

	if ($status == 0){
		return true;		
	}
	if ($status == 1){
		$reason = "Old password did not match.";
		return false;		
	}  
 	if ($reason == null || $reason == ""){
		$reason = "Unable to change password.";
	}
	return false;
}

function ity_recover($name, &$reason){
	if ($name == null){
		$reason = "Please provide valid email.";
		return false;
	}

	$url = ITY_API_HOST."/api/identity?verb=recover&name=".urlencode($name)."&device-id=".urlencode($_SERVER["SCRIPT_NAME"]);

	$ch = curl_init($url);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, ITY_API_TIMEOUT);	 
	curl_setopt($ch, CURLOPT_TIMEOUT, ITY_API_TIMEOUT); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$output = curl_exec($ch);      
    curl_close($ch);

	$status = ity_xml_response_get($output, "status");
	$reason = ity_xml_response_get($output, "reason");

	if ($status == 0){
		return true;		
	}
	if ($status == 1){
		$reason = "Unknown account.";
		return false;		
	}  
 	if ($reason == null || $reason == ""){
		$reason = "Unable to recover password.";
	}
}

function ity_register($name, $pwd, &$reason, $existing = "true"){
	if ($name == null || $pwd == ""){
		$reason = "Please provide valid email and password.";
		return null;
	}
	  
	$ch = curl_init(ITY_API_HOST."/api/identity");
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, ITY_API_TIMEOUT);	 
	curl_setopt($ch, CURLOPT_TIMEOUT, ITY_API_TIMEOUT); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_POST, 5);
	curl_setopt($ch, CURLOPT_POSTFIELDS, 
		"verb=register&name=".urlencode($name)."&pwd=".urlencode($pwd)."&existing=".$existing."&device-id=".urlencode($_SERVER["SCRIPT_NAME"]));
	curl_setopt($ch, CURLOPT_HTTPHEADER, array("X-Forwarded-For: ".$_SERVER["REMOTE_ADDR"])); 
  
	$output = curl_exec($ch);      
    curl_close($ch);

	$status = ity_xml_response_get($output, "status");
	$token = ity_xml_response_get($output, "token");
	$reason = ity_xml_response_get($output, "reason");
 
	if ($status == 0 || $status == 1){
		return $token;		
	}
	if ($status == 2){
		$reason = "Bad email or password.";
		return null;		
	}
	if ($reason == null || $reason == ""){
		$reason = "Unable to login.";
	}
	return null;		
}

function ity_lookup_token($app_session, &$reason, &$name, &$is_pro){
	if ($app_session == null || $app_session == ""){
		$app_session = $_COOKIE['web_session'];
		if ($app_session == null || $app_session == ""){
			$reason = "Access denied.";
			return null;
		}
	}
	
	$url = ITY_API_HOST."/api/identity?verb=lookup&token=".$app_session."&device-id=".$_SERVER["SCRIPT_NAME"];
	$ch = curl_init($url);
	curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, ITY_API_TIMEOUT);	 
	curl_setopt($ch, CURLOPT_TIMEOUT, ITY_API_TIMEOUT); 
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	$output = curl_exec($ch);      
    curl_close($ch);

	$status =  ity_xml_response_get($output, "status");
	$reason =  ity_xml_response_get($output, "reason");
	$user_id = ity_xml_response_get($output, "user-id");
	
	$is_pro = "1" == ity_xml_response_get($output, "is-pro");
	if (!$is_pro){
		$is_pro = ity_infer_is_pro();
	}

	$name = ity_xml_response_get($output, "name");

	if ($status != 0){
		return null;		
	}
	
	return $user_id;
}

function ity_score_color($score){
	$color = "#FF9999";
	if ($score > 0.35){
		$color = "#FFFF99";
	}
	if ($score > 0.7){
		$color = "#99FF99";
	}
	return $color;
}

function ity_score_class($score){
	$cls = "s_r";
	if ($score > 0.35){
		$cls = "s_y";
	}
	if ($score > 0.7){
		$cls = "s_g";
	}
	return $cls;
}

function ity_date_week_start($week, $year) {
	$firstDayInYear=date("N",mktime(0,0,0,1,1,$year));
	if ($firstDayInYear<5) {
		$shift=-($firstDayInYear-1)*86400;
	} else {
		$shift=(8-$firstDayInYear)*86400;
	}
	if ($week>1) $weekInSeconds=($week-1)*604800; else $weekInSeconds=0;
	$timestamp=mktime(0,0,0,1,1,$year)+$weekInSeconds+$shift;
	return $timestamp;
}

function ity_date_wy_ex($now, &$now_year, &$now_week, &$prior_week, &$prior_year, &$next_year, &$next_week){		
	if (isset($_GET['ity_year'])){
		$now_year = (int) $_GET['ity_year'];
	} else {
		$now_year = date("o", $now);
	}

	if (isset($_GET['ity_week'])){
		$now_week = $_GET['ity_week'];
	} else {
		$now_week = date("W", $now);
	}		

	$now = ity_date_week_start($now_week, $now_year);

	$past = $now - 7 * 24 * 60 * 60;
	$future = $now + 7 * 24 * 60 * 60;

	$prior_year = date("o", $past);
	$prior_week = date("W", $past);

	$next_year = date("o", $future);
	$next_week = date("W", $future);
}

function ity_date_wy_no_params($now, &$now_year, &$now_week, &$prior_week, &$prior_year, &$next_year, &$next_week){		
	$now_year = date("o", $now);
	$now_week = date("W", $now);

	$now = ity_date_week_start($now_week, $now_year);

	$past = $now - 7 * 24 * 60 * 60;
	$future = $now + 7 * 24 * 60 * 60;

	$prior_year = date("o", $past);
	$prior_week = date("W", $past);

	$next_year = date("o", $future);
	$next_week = date("W", $future);
}

function ity_date_wy($now, &$now_year, &$now_week, &$prior_week, &$prior_year, &$next_year, &$next_week){		
	if (isset($_GET['year'])){
		$now_year = (int) $_GET['year'];
	} else {
		$now_year = date("o", $now);
	}

	if (isset($_GET['week'])){
		$now_week = $_GET['week'];
	} else {
		$now_week = date("W", $now);
	}		

	$now = ity_date_week_start($now_week, $now_year);

	$past = $now - 7 * 24 * 60 * 60;
	$future = $now + 7 * 24 * 60 * 60;

	$prior_year = date("o", $past);
	$prior_week = date("W", $past);

	$next_year = date("o", $future);
	$next_week = date("W", $future);
}

function ity_week_start_date_raw($wk_num, $yr, $first = 1, $format = 'M d, Y') {
	$wk_ts  = strtotime('+' . $wk_num . ' weeks', strtotime($yr . '0101'));
	$mon_ts = strtotime('-' . date('w', $wk_ts) + $first . ' days', $wk_ts);
	return $mon_ts;
}

function ity_week_start_date($wk_num, $yr, $first = 1, $format = 'M d, Y') {
	$wk_ts  = strtotime('+' . $wk_num . ' weeks', strtotime($yr . '0101'));
	$mon_ts = strtotime('-' . date('w', $wk_ts) + $first . ' days', $wk_ts);
	return date($format, $mon_ts);
}

function ity_render_tracking(){
	return 
		'<!-- Google Analytics -->
		<script type="text/javascript">
		var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
		document.write(unescape("%3Cscript src=\'" + gaJsHost + "google-analytics.com/ga.js\' type=\'text/javascript\'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
		try {
		var pageTracker = _gat._getTracker("UA-360750-5");
		pageTracker._trackPageview();
		} catch(err) {}</script>';
}

function ity_db(){
  $dbh = mysql_connect ("localhost", "admin", "admin", true);
  if (!$dbh) {
	  die ("Can't connect to the database.");
  }

  mysql_select_db("ITY_ADMIN", $dbh);
  mysql_query("SET NAMES 'utf8'", $dbh);

  return $dbh;
}

function ity_sql_escape($sql, $params){
  foreach ($params as $key => $param) {
	$array[$key] = mysql_real_escape_string($param);
  }
  return vsprintf($sql, $params);
}

function ity_qry($dbh, $sql, $params = array()){
	$sql =  ity_sql_escape($sql, $params);
	$result = mysql_query($sql, $dbh);
	if (!$result) {
		error_log("Can't execute query: ".$sql.mysql_error());
		global $ity_dao_debug;
		if ($ity_dao_debug){
			die("Can't execute query: ".$sql.mysql_error());
		} else {
			die("Our site has some technical difficulties. Please visit us later when we fix it. Thank you.");
		}
	}
	return $result;
}

function ity_send_email_now($from, $to, $title, $body){
	$to = filter_var($to, FILTER_SANITIZE_EMAIL);
	$from = filter_var($from, FILTER_SANITIZE_EMAIL);

	$headers  = "From: " . $from . "\n";
	$headers .= "Reply-To: ". $from . "\n";
	$headers .= "MIME-Version: " . "1.0" . "\n";
	$headers .= "Content-Type: " . "text/html; charset=ISO-8859-1" . "\n";
	$headers .= "Date: " . date(DATE_RFC822) . "\n";

	return mail(
		$to, 
		$title,
		$body,
		$headers
	);
}

function ity_send_email_now_ex($from, $name, $to, $title, $body){
	$to = filter_var($to, FILTER_SANITIZE_EMAIL);
	$from = filter_var($from, FILTER_SANITIZE_EMAIL);

	$headers  = "From: " . $name . " <". $from . ">\n";
	$headers .= "Reply-To: " . $from . "\n";
	$headers .= "MIME-Version: " . "1.0" . "\n";
	$headers .= "Content-Type: " . "text/html; charset=ISO-8859-1" . "\n";
	$headers .= "Date: " . date(DATE_RFC822) . "\n";

	return mail(
		$to, 
		$title,
		$body,
		$headers
	);
}

function ity_get_lang_selector($user_id, $lang) {
    // derive cookie name from user id
	$cookie_name = "lang".md5($user_id);
	if (isSet($_COOKIE[$cookie_name])) {
		$lang = $_COOKIE[$cookie_name];
	}

	// language selector javascript
	return 
		"<script type='text/javascript'><!--
			document.ityLocale='".htmlentities($lang)."';
			function itySetLocale(lang) {
			  var date = new Date();
			  date.setTime(date.getTime()+(90*24*60*60*1000));
			  document.cookie = '".$cookie_name."=' + lang + '; expires=' + date.toGMTString() + '; path=/; domain=.itestyou.com';
			  document.ityLocale = lang;
			}
		--></script>
		<select onchange='itySetLocale(this.options[this.selectedIndex].value); window.location.reload();'>
			<option value='en' ".(($lang == "en") ? "selected" : "").">English (EN)</option>
			<option value='es' ".(($lang == "es") ? "selected" : "").">Español (ES)</option>
			<option value='nl' ".(($lang == "nl") ? "selected" : "").">Nederlands (NL)</option>
			<option value='ru' ".(($lang == "ru") ? "selected" : "").">Русский (RU)</option>
			<option value='zh' ".(($lang == "zh") ? "selected" : "").">中文 (ZH)</option>
		</select>";
}

?>