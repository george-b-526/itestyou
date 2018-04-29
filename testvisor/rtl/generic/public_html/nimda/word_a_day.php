<?php

	include_once "../lib/dao.php";
	include_once "../lib/social.php";


	$dbh = ity_db();

	// get unit XML
	$qry = ity_qry($dbh,
	  "SELECT UNCOMPRESS(UNI_XML) AS XML FROM ITY_ADMIN.UNI_UNIT WHERE UNI_ID = %s",
	  array(115) 
	);
	$row = mysql_fetch_assoc($qry);
	$xml = $raw = $row['XML'];

	// item
	$message = "";
	$rnd = rand(0, 5100);

	// parse XML
	$z = new XMLReader();
	$z->xml($xml);
	$i = 0;
	while ($z->read()){
		if ($z->name == "w"){
			$i++;
			if ($i == $rnd){
				$e = $z->getAttribute("e");
				$id = $z->getAttribute("i");
				$m = $z->getAttribute("m");
				$t = $z->getAttribute("t");

				// format
				$message = $e;
				if ($t != ""){
					$message .= " (".$t.")";
				}
				$message .= ": ".$m;

				break;
			}
		}
	}

	$subject = "FAILED to get message";
	if ($message != null){
		$tw_status = false;
		$fb_status = false;

		// twitter
		if (true){
			if (ity_post_twitter($message, "#ity_vocab", $tw_reason)){
				$tw_status = true;
			} else {
				echo $tw_reason;
			}
		}

		// facebook
		if (true){
			if (ity_post_fb("Word of the day:\n\n".$message."\n\n", "ITestYou", $fb_reason, "http://www.itestyou.com", "SAT, GRE, TOEFL Vocab")){
				$fb_status = true;
			} else {
				echo $fb_reason;

				// render fb login page
				echo "<a href='".ity_fb_make_allow_url()."&response_type=token'>allow FB access</a>";
			}
		}

		if ($tw_status && $fb_status){
			$subject = "POST {SUCCESS}";
		} else {
			$subject = "POST {TWITTER:".($tw_status? "OK" : "FAILED").", FB:".($fb_status? "OK" : "FAILED")."}";
		}
	}

	echo $subject;

 	$to = "levap@vokamis.com";
	$headers = 
		'From: server@itestyou.com' . "\r\n" .
		'Reply-To: server@itestyou.com' . "\r\n" .
		'X-Mailer: PHP/' . phpversion();

	mail($to, $subject, "twitter.php", $headers)
?>