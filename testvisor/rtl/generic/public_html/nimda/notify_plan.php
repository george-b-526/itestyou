<?php

	//
	// send email if person did not have any activity in the last several weeks
	//
	function ity_notify_plan_we_missed_you($dbh){
		ity_echo_log("BEGIN: ity_notify_plan_we_missed_you");

		// event type
		$NO_ACTIVITY_LAST_TWO_WEEKS = 3;

		// look for 17 weeks of idle time
		$now = ity_pst_time();
		$cutoff = $now - 17 * 7 * 24 * 60 * 60;

		$sql = 
		   "SELECT CUS_ID, CUS_NAME, CUS_PROPERTIES, MAX(UUR_LAST_RESPONSE_ON) AS LAST_ACTIVE_ON, EVT_ID, EVT_POSTED_ON
			FROM ITY_RUNTIME.UUR_UNIT_ROLLUP_WEEKLY AS A
			LEFT JOIN ITY_IDENTITY.CUS_CUSTOMER AS B ON B.CUS_ID = A.UUR_USR_ID
			LEFT JOIN ITY_RUNTIME.EVT_EVENT AS C 
				ON C.EVT_USR_ID = A.UUR_USR_ID AND C.EVT_TYPE = ".$NO_ACTIVITY_LAST_TWO_WEEKS." AND UNIX_TIMESTAMP(C.EVT_POSTED_ON) > ".$cutoff."
			WHERE 
				CUS_ID >= 1000 AND
				(C.EVT_ID IS NULL OR UNIX_TIMESTAMP(C.EVT_POSTED_ON) < ".$cutoff.")
			GROUP BY UUR_USR_ID
			HAVING (UNIX_TIMESTAMP(LAST_ACTIVE_ON) < ".$cutoff.") AND (EVT_ID IS NULL OR EVT_POSTED_ON IS NULL)"
			;
		$qry = ity_qry($dbh, $sql);

		// insert
		$i = 0;
		while($row = mysql_fetch_assoc($qry)){
			$props = array();
			ity_get_user_properties_from_row($row, $props);

			// respect preferences
			if (!$props['notify_learn']){
				ity_echo_log("ity_notify_plan_we_missed_you: skipped CUS_ID=".$row['CUS_ID'].", CUS_NAME=".$row['CUS_NAME']);
				continue;
			}

			ity_echo_log("ity_notify_plan_we_missed_you: pushed CUS_ID=".$row['CUS_ID'].", CUS_NAME=".$row['CUS_NAME']);
			
			ity_insert_event($dbh, $row['CUS_ID'], $NO_ACTIVITY_LAST_TWO_WEEKS, $_SERVER["SCRIPT_NAME"], null);
			
			$i++;
		}

		ity_echo_log("ity_notify_plan_we_missed_you: pushed ".$i." events");

		ity_echo_log("END: ity_notify_plan_we_missed_you");
	}

	// 
	// send email if person registeres, but does not play a single time in the next 3 hours
	//
	function ity_notify_plan_inactive_registration($dbh){
		ity_echo_log("BEGIN: ity_notify_plan_inactive_registration");

		// event type
		$JOINED_BUT_DID_NOT_IMMEDIATELY_PLAY = 2;

		// give person 3 hours to play the first game
		$till = time() - 60 * 60 * 3;
		
		// select
		$qry = ity_qry($dbh,
		   "SELECT * FROM ITY_IDENTITY.CUS_CUSTOMER AS A
			LEFT JOIN ITY_RUNTIME.REQ_RESPONSE_QUEUE AS B ON B.REQ_USR_ID = A.CUS_ID
			LEFT JOIN ITY_RUNTIME.EVT_EVENT AS C ON C.EVT_USR_ID = A.CUS_ID AND C.EVT_TYPE = %s
			WHERE 
				B.REQ_USR_ID IS NULL AND 
				UNIX_TIMESTAMP(A.CUS_CREATED_ON) < %s AND 
				C.EVT_ID IS NULL
			", array($JOINED_BUT_DID_NOT_IMMEDIATELY_PLAY, $till) 
		);

		// insert
		$i = 0;
		while($row = mysql_fetch_assoc($qry)){
			ity_echo_log("ity_notify_plan_inactive_registration: pushed CUS_ID=".$row['CUS_ID'].", CUS_NAME=".$row['CUS_NAME']);

			ity_insert_event($dbh, $row['CUS_ID'], $JOINED_BUT_DID_NOT_IMMEDIATELY_PLAY, $_SERVER["SCRIPT_NAME"], null);
			
			$i++;
		}
		ity_echo_log("ity_notify_plan_inactive_registration: pushed ".$i." events");

		ity_echo_log("END: ity_notify_plan_inactive_registration");
	}

	// turned off on 7/29/2013
	// include_once "../lib/dao.php";
	// $dbh = ity_db();
	// ity_notify_plan_inactive_registration($dbh);
	// ity_notify_plan_we_missed_you($dbh);

?>