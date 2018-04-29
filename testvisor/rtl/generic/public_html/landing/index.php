<?php

	// mark confirmed
	if (isset($_REQUEST['evt_id'])){
		$evt_id = $_REQUEST['evt_id'];
		
		if ($evt_id){
			include_once "../lib/dao.php";

			$dbh = ity_db();

			// lookup event and user
			$qry = ity_qry($dbh,
			   "SELECT * FROM ITY_RUNTIME.EVT_EVENT WHERE MD5(EVT_ID) = '%s'"
				, array($evt_id) 
			);

			// mark confirmed
			if ($row = mysql_fetch_assoc($qry)){
				$qry = ity_qry($dbh,
				   "UPDATE ITY_IDENTITY.CUS_CUSTOMER SET CUS_VERIFIED = 1, CUS_VERIFIED_ON = FROM_UNIXTIME(%s) WHERE CUS_ID = %s",
					array(time(), $row['EVT_USR_ID']) 
				);
			}
		}
	}

	// redirect to the proper place
	if (isset($_REQUEST['verb'])){
		$verb = $_REQUEST['verb'];
		
		if ($verb == "help"){
			header("Location: http://www.itestyou.com/cms/support");
			exit;
		} 

		if ($verb == "use"){
			header("Location: http://www.itestyou.com/");
			exit;
		}

		if ($verb == "manage"){		
			header("Location: http://www.itestyou.com/cms/my-account");
			exit;
		}

		if ($verb == "unit"){		
			$uni_id = 25;
			if (isset($_REQUEST['uni_id'])){
				$uni_id = (int) $_REQUEST['uni_id'];
			}
			header("Location: http://www.itestyou.com/cms/unit-".$uni_id);
			exit;
		}
	}

?>