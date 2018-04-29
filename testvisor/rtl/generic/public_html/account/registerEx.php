<?php

function ity_render_register(){
	global $ity_render_register_lambda;
	if ($ity_render_register_lambda){
		return $ity_render_register_lambda;
	}

	include_once "../lib/dao.php";

	// params
	$name = $_POST['wpName'];
	$pwd = $_POST['wpPassword1'];
	$pwd2 = $_POST['wpPassword2'];
	$returnto = $_GET['ity_returnto'];
	if ($returnto == null){
		$returnto = "http://www.itestyou.com/cms";
	}
	$is_create = $_POST['wpCreate'] != null;

	// prepare
	$msg = "";

	// login action
	if ($is_create){

		$reason = null;

		// check pwd
		if ($pwd != $pwd2){
			$reason = "Passwords don't match.";
		}

		if (strlen($pwd) < 6){
			$reason = "Passwords is too short (6 characters minimum).";
		}

		if (strlen($name) < 6){
			$reason = "Email is too short (6 characters minimum).";
		}

		// register
		if ($reason == null){
			$token = ity_register($name, $pwd, $reason, false);
			if ($token != null){
				$expires = 60 * 60 * 24 * 30 + time();  //this adds 30 days to the current time 
				setcookie("web_session", $token, $expires, "/", ".itestyou.com");
				header("Location: ".$returnto);
				exit();
			}
		}

		if ($reason != null){
			$msg = "<p style='background-color: #FFAAAA;'>".$reason."</p>";
		}		
	}

	// render
	$body = '
		<div style="width: 100%;" align="center">
		<div style="background-color: #FFFFFF; border: 2px solid #008800; width: 450px; padding: 32px; padding-top:16px; margin-top: 8px;" id="userloginForm">
		<form action="https://www.itestyou.com/cms/register?ity_returnto='.urlencode($returnto).'" method="post" name="userlogin">
			<div id="userloginprompt"><b>Create New ITestYou Account</b><p>You must have cookies enabled.</p>'.$msg.'<hr /></div>
			<table><tbody>
				<tr>
					<td class="mw-label"><label for="wpName1">Email:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="text" size="20" value="'.htmlentities($name).'" tabindex="1" id="wpName1" name="wpName" class="loginText"></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword1">Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="2" id="wpPassword1" name="wpPassword1" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword2">Re-Enter Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="3" id="wpPassword2" name="wpPassword2" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td style="padding: 8px;" colspan="2" align="center" valign="middle"><input type="checkbox" disabled="disabled" checked="checked" /> I agree to the <a href="/cms/terms-of-use">Terms of use</a><hr /></td>
				</tr>
				<tr>
					<td style="text-align: center;" class="mw-submit" colspan="2">
						<input type="submit" value=" Create New Account " tabindex="4" id="wpLoginattempt" name="wpCreate">
					</td>
				</tr>
			</tbody></table>
		</form>

		<br />

		<p align="center">Already have an account? Please <a href="/cms/log-in"><b>login</b></a> now!</p>		
		</div>
		</div>';

	return $body;
}
?>