<?php

	include_once "../lib/dao.php";
	include_once "common.php";

	// bind to user
	$reason = null;  
	$email = null;
	$is_pro = null;
	$app_session = $_REQUEST["app_session"];
	if ($app_session == null){
		echo "Access denied (no application session).";
		return;
	}
	$user_id = ity_lookup_token($app_session, $reason, $email, $is_pro);
	if ($user_id == null){
		echo $reason;
		return;
	}

?><html>
<head>
	<link type='text/css' rel='stylesheet' href='http://www.itestyou.com/view/normal.css' />
</head>
<body style="margin: 0px; padding: 0px; font-family: Arial; font-size: 12px; border: none;">
<?php echo ity_mobile_tools_menu($user_id, $app_session, $_SERVER["SCRIPT_NAME"]); ?>
<style>

P.err {
	color: red; 
	font-weight: bold;
	text-align: center;
}

INPUT.field {
	border-color:#7CBA2C;
	border-style:solid;
	border-width:1px 1px 1px 4px;
	width: 250px;
}

TEXTAREA {
	border-color:#7CBA2C;
	border-style:solid;
	border-width:1px 1px 1px 4px;
	width: 250px;
}

</style><?php

	$context = "";
	$message = "";

	$verdict = "&nbsp;<br />";
	$done = false;
	if (array_key_exists('submit', $_POST)){

		$ip = $_SERVER['REMOTE_ADDR'];
		$message = $_POST['message'];
		$context = $_POST['context'];
		$referer = $_POST["inReferer"];
		$ctx = $_POST['inCtx'];

		if (strlen($message) > 1024){
			$verdict = "<p class='err'>Message is too long, 1024 characters max.</p>";
		} else {
			if (!$message){
				$verdict = "<p class='err'>Please provide message.</p>";
			} else {
				$ip = $_SERVER['REMOTE_ADDR'];

				$to = "admin@itestyou.com";
				$from = "From: ".htmlspecialchars($email);
				$body = 
					"<html><body>".
					"<b>ITestYou Online Form Submission</b><br>".
					"IP: ".$ip."<br>".
					"Date: ".date("D M j G:i:s T Y")."<br>".
					"Name: ".htmlspecialchars($email)."<br>".
					"User Id: ".htmlspecialchars($user_id)."<br>".
					"Referer: ".htmlspecialchars($referer)."<br>".	
					"Context: ".htmlspecialchars($ctx)."<br>".	
					"Script Context: ".htmlspecialchars($context)."<br>".
					"Edition: ".htmlspecialchars(ity_get_edition($has_edition))."<br>".
					"Message: ".htmlspecialchars($message).
					"</body></html>";

				error_log($ip."|".$user_id." sent message to ".$to."\n".$message, 0);

				$result = ity_send_email_now("admin@itestyou.com", $to, "ITestYou Feedback Message", $body);
				if ($result){
					echo "You message was sent. Thank you!";
				} else {
					echo "Error sending message. Please try again later.";
				}
			
				$done = true;
			}
		}
	} 
	
	
	if (!$done) {

		echo "<h2 align='center'>Send Us Your Feedback</h2>";

		echo $verdict;

		$referer = "";
		if (isset($_REQUEST["inReferer"])){
			$referer = $_REQUEST["inReferer"];
		}

		$ctx = "";
		if (isset($_REQUEST["inCtx"])){
			$ctx = $_REQUEST["inCtx"];
		}


?>
		<form style="margin: 0px; padding: 0px; " method="POST" action="/view/shout.php">
			<input type='hidden' name='app_session' value='<?php echo htmlspecialchars($app_session); ?>' />
			<input type='hidden' name='inReferer' value='<?php echo htmlentities($referer); ?>' />
			<input type='hidden' name='inCtx' value='<?php echo htmlentities($ctx); ?>' />
			<script language="JavaScript"><!--
					document.write("<" + "input type='hidden' value='" + escape(document.referrer) + "' name='context'" + ">");
			--></script>
			<table style="padding: 8px; width: 100%; font-family: Arial; font-size: 12px;" border="0" width="100%" cellpadding="2" cellspacing="0">

			<tr> 
				<td align="left" nowrap valign="top">
					User: <strong><?php echo htmlspecialchars($email); ?></strong>
				</td>
			</tr>

			<tr>
				<td align="left" nowrap valign="top">
					<br />Your Message, Question or Suggestion:<br />
					<textarea style='font-family: Arial; font-size: 12px; width: 100%;' rows="8" name="message"><?php echo htmlspecialchars($message); ?></textarea>
				</td>
			</tr>

			<tr>
				<td align="center"><input type="submit" name="submit" class='vote' value="Submit"></td>
			</tr>
		</table>
	</form>
<?php } ?>
</body> 