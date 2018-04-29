<?php

	// event types
	DEFINE("JOINED_BUT_DID_NOT_IMMEDIATELY_PLAY", 2);
	DEFINE("NO_ACTIVITY_LAST_TWO_WEEKS", 3);

	// event states
	DEFINE("ITY_EVT_STATE_QUEUED", 0);
	DEFINE("ITY_EVT_STATE_BUSY", 1);
	DEFINE("ITY_EVT_STATE_PROCESSED", 2);
	DEFINE("ITY_EVT_STATE_FAILED", 3);


	function endsWith($haystack, $needle) {
		$length = strlen($needle);
		if ($length == 0) {
			return true;
		}

		return (substr($haystack, -$length) === $needle);
	}

	function ity_notify_do_we_missed_you($dbh){
		ity_echo_log("BEGIN: ity_notify_do_we_missed_you");

		// find
		$qry = ity_qry($dbh,
		   "SELECT CUS_ID, CUS_NAME, CUS_PROPERTIES, C.EVT_ID, D.UNI_ID, D.UNI_TYPE, D.UNI_TITLE
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY AS A
			LEFT JOIN ITY_IDENTITY.CUS_CUSTOMER AS B ON B.CUS_ID = A.UUR_USR_ID
			LEFT JOIN ITY_RUNTIME.EVT_EVENT AS C ON C.EVT_USR_ID = A.UUR_USR_ID AND C.EVT_TYPE = ".NO_ACTIVITY_LAST_TWO_WEEKS."
			LEFT JOIN ITY_ADMIN.UNI_UNIT AS D ON D.UNI_ID = A.UUR_UNI_ID
			WHERE
				CUS_ID >= 1000
				AND (C.EVT_ID IS NOT NULL AND C.EVT_STATE = ".ITY_EVT_STATE_QUEUED.")
				AND (D.UNI_ID = (
					SELECT UUR_UNI_ID FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY
					WHERE UUR_USR_ID = CUS_ID
					ORDER BY UUR_LAST_RESPONSE_ON DESC
					LIMIT 1
				))
			GROUP BY UUR_USR_ID
			LIMIT 5"
		);

		// load
		$i = 0;
		while($row = mysql_fetch_assoc($qry)){
			$id =  $row['EVT_ID'];
			$to = $row['CUS_NAME'];
			$uni_id = $row['UNI_ID'];
			$uni_type = $row['UNI_TYPE'];
			$uni_title = $row['UNI_TITLE'];

			// urls
			$href_use = "http://www.itestyou.com/land/use?evt_id=".md5($id);
			$href_manage = "http://www.itestyou.com/land/manage?evt_id=".md5($id);
			$href_unit = "http://www.itestyou.com/land/unit?evt_id=".md5($id)."&uni_id=".$uni_id;
			$img_tag = "";

			// link to image
			$unit_img = "";
			if ($uni_type == 1) {
				$ity_lang = "en";
				if (endsWith($to, ".ru")) {
					$ity_lang = "ru";
				}
				if (endsWith($to, ".nl")) {
					$ity_lang = "nl";
				}
				if (endsWith($to, ".cn") || endsWith($to, ".tw")) {
					$ity_lang = "zh";
				}
				if (endsWith($to, ".es")) {
					$ity_lang = "es";
				}
				$unit_img = "http://www.itestyou.com/img/units/medium/".$uni_type."-".$uni_id."-".$ity_lang.".png";
			}
			if ($uni_type == 2) {
				$unit_img = "http://www.itestyou.com/img/units/medium/".$uni_type."-".$uni_id."-en.png";
			}
			if ($unit_img != "") {
				$img_tag = "<a href='".$href_unit."'><img style='border: 2px solid #ff6600;' src='".$unit_img."'></a>";
			}

			// sanitize
			$to = filter_var($to, FILTER_SANITIZE_EMAIL);

			$title = "Practice ".$uni_title." on ITestYou!";

			$body = 
				"<html>
				<head>
					<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
					<title>".$title."</title>
				</head>
				<body>
					<p>We've missed you on ITestYou, <i>".$to."</i>!</p>
					<p>Several weeks ago you practiced <b>".$uni_title."</b>. Remember the answer? Try it now:<br\></p>
					<p align='center'>".$img_tag."</p>
					<p style='margin:0px; padding:0px;'>ITestYou helps learners all over the world practice
					<ul style='margin-top: 4px;'>
					<li><b>Math</b> Worksheets,</li>
					<li><b>SAT, GRE, TOEFL</b> Vocabulary + your own vocabulary!</li>
					<li>over 36 <b>Foreign Languages</b>!</li>
					</ul>
					Improve yourself <a href='".$href_use."'>online</a> from any web browser and any mobile device!</p>
					<p>See you soon,<br />ITestYou Support Team</p>
					<p style='font-size: smaller; border-top: solid 1px #C0C0C0;'><br />
					You can <a href='".$href_manage."'>unsubscribe or change</a> your notification preferences. Please do not reply to this message; it was sent from an unmonitored email address. This message is a service email related to your use of ITestYou.</p>
				</body>
			</html>";

			$state = ity_notify_do_any($dbh, $id, $to, $title, $body);
			ity_echo_log("ity_notify_do_we_missed_you: email ".$state.", EVT_ID=".$id.", CUS_NAME=".$to);

			$i++;
		}

		ity_echo_log("ity_notify_do_we_missed_you: pushed ".$i." events");

		ity_echo_log("END: ity_notify_do_we_missed_you");
	}

	function ity_notify_do_inactive_registration($dbh){
		ity_echo_log("BEGIN: ity_notify_do_inactive_registration");

		// find
		$qry = ity_qry($dbh,
		   "SELECT A.*, B.CUS_NAME AS EMAIL FROM ITY_RUNTIME.EVT_EVENT AS A
			LEFT JOIN ITY_IDENTITY.CUS_CUSTOMER AS B ON B.CUS_ID = A.EVT_USR_ID
			WHERE EVT_TYPE = ".JOINED_BUT_DID_NOT_IMMEDIATELY_PLAY." AND EVT_STATE = ".ITY_EVT_STATE_QUEUED."
			ORDER BY B.CUS_ID
			LIMIT 5"
		);

		// load
		$todo = array();
		while($row = mysql_fetch_assoc($qry)){
			$todo[$row['EVT_ID']] = $row['EMAIL'];
		}
	
		// update
		$i = 0;
		foreach ($todo as $id => $to) {
			$to = filter_var($to, FILTER_SANITIZE_EMAIL);

			$title = "Welcome to ITestYou!";

			$href_help = "http://www.itestyou.com/land/help?evt_id=".md5($id);
			$href_use = "http://www.itestyou.com/land/use?evt_id=".md5($id);
			$href_manage = "http://www.itestyou.com/land/manage?evt_id=".md5($id);

			$body = 
				"<html>
				<head>
					<meta http-equiv='Content-type' value='text/html; charset=UTF-8' />
					<title>".$title."</title>
				</head>
				<body>
					<p>Dear ".$to.",</p>
					<p>Welcome to <b>ITestYou</b>!</p>
					<p>You created an account, but did not practice any <i>Math</i> or <i>Vocabulary</i> challenges. Did you have technical issues? Get <a href='".$href_help."'>help</a> setting up ITestYou on your Android phone, iPhone or iPad.</p>
					<p>Improve your grades with <a href='".$href_use."'>ITestYou online</a> from any web browser and any mobile device!</p>
					<p>See you soon,<br />ITestYou Support Team</p>
					<p style='font-size: smaller; border-top: solid 1px #C0C0C0;'><br />
					You can <a href='".$href_manage."'>unsubscribe or change</a> your notification preferences. Please do not reply to this message; it was sent from an unmonitored email address. This message is a service email related to your use of ITestYou.</p>
				</body>
			</html>";
			
			$state = ity_notify_do_any($dbh, $id, $to, $title, $body);

			ity_echo_log("ity_notify_do_inactive_registration: email ".$state.",EVT_ID=".$id.", CUS_NAME=".$to);

			$i++;
		}
		ity_echo_log("ity_notify_do_inactive_registration: pushed ".$i." events");

		ity_echo_log("END: ity_notify_do_inactive_registration");
	}

	function ity_notify_do_any($dbh, $id, $to, $title, $body){
		// busy
		$qry = ity_qry($dbh, "UPDATE ITY_RUNTIME.EVT_EVENT SET EVT_STATE = ".ITY_EVT_STATE_BUSY.", EVT_STATE_CHANGE_ON = FROM_UNIXTIME(%s) WHERE EVT_ID = %s" , array(time(), $id) );

		// send
		$result = ity_send_email_now_ex("support@itestyou.com", "ITestYou Support", $to, $title, $body);

		// done
		$state = ITY_EVT_STATE_PROCESSED;
		if (!$result){
			$state = ITY_EVT_STATE_FAILED;
		}
		$qry = ity_qry($dbh, "UPDATE ITY_RUNTIME.EVT_EVENT SET EVT_STATE = %s, EVT_STATE_CHANGE_ON = FROM_UNIXTIME(%s) WHERE EVT_ID = %s" , array($state, time(), $id) );
	
		return $state;
	}

	include_once "../lib/dao.php";

	$dbh = ity_db();

	ity_notify_do_inactive_registration($dbh);
	ity_notify_do_we_missed_you($dbh);
?>