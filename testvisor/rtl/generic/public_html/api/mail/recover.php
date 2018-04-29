<?php

	include_once "../../lib/dao.php";
	
	$to = $_POST["to"];

	$pwd = $_POST["pwd"];
	$ip = $_SERVER['REMOTE_ADDR'];

	$body = 
		"<html>
		<title>ITestYou: Forgot Password</title>
		<body>
			<p>
			Dear ITestYou user: ".$to."<br /><br />
			Password reset was requested for your account.<br /><br /> 
			Your new password is: <b>".$pwd."</b><br /><br />
			Use this password next time you login.
			If you want to keep your old password, simply disregard this message.
			Your old password will continue working if new password is not entered.<br /><br />
			<p>Regards,<br />ITestYou Support Team<br /><a href='http://www.itestyou.com'>http://www.itestyou.com</a>
			</p>
		</body></html>";

	$result = ity_send_email_now("support@itestyou.com", $to, "ITestYou: Forgot Password", $body);

	if ($result){
		error_log($ip." sent password recovery message to ".$to." (OK)", 0);
		echo "OK";
	} else {
		error_log($ip." sent password recovery message to ".$to." (FAILED)", 0);
		echo "FAIL";
	}

?>