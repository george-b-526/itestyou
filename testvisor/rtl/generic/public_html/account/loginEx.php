<?php

function ity_render_login(){
	global $ity_render_login_lambda;
	if ($ity_render_login_lambda){
		return $ity_render_login_lambda;
	}

	include_once "../lib/dao.php";

	// params
	$name = $_POST['wpName'];
	$pwd = $_POST['wpPassword'];
	$returnto = $_GET['ity_returnto'];
	if ($returnto == null){
		$returnto = "http://www.itestyou.com/cms";
	}
	$is_login = $_POST['wpLogin'] != null;
	$is_forgot = $_POST['wpForgotPwd'] != null;

	// prepare
	$msg = "";

	// login action
	if ($is_login){
		$reason = "";
		$token = ity_register($name, $pwd, $reason);
		if ($token != null){
			$expires = 60 * 60 * 24 * 30 + time();  //this adds 30 days to the current time 
			setcookie("web_session", $token, $expires, "/", ".itestyou.com");
			header("Location: ".$returnto);
			exit();
		} else {
			$msg = "<p style='background-color: #FFAAAA;'>".$reason."</p>";
		}
	}

    // if forgot password
	if ($is_forgot){
		$reason = "";
		if (ity_recover($name, $reason)){ 
			$msg = "<p style='background-color: #AAFFAA;'>New password was sent to your email.</p>";
		} else {
			$msg = "<p style='background-color: #FFAAAA;'>".$reason."</p>";
		}
	}

	// render
	$body = '
		<div style="width: 100%;" align="center">
		<div style="background-color: #FFFFFF; border: 2px solid #008800; width: 450px; padding: 32px; padding-top:16px; margin-top: 8px;" id="userloginForm">
		<form action="https://www.itestyou.com/cms/log-in?ity_returnto='.urlencode($returnto).'" method="post" name="userlogin">
			<div id="userloginprompt"><b>Login to ITestYou</b><p>You must have cookies enabled.</p>'.$msg.'<hr /></div>
			<table style="border-collapse: collapse;" border="0"><tbody>
				<tr>
					<td class="mw-label"><label for="wpName1">Email:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="text" size="20" value="'.htmlentities($name).'" tabindex="1" id="wpName1" name="wpName" class="loginText"></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword1">Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="2" id="wpPassword1" name="wpPassword" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td style="padding: 8px;" colspan="2" align="center" valign="middle"><input type="checkbox" disabled="disabled" checked="checked" /> I agree to the <a href="/cms/terms-of-use">Terms of use</a><hr /></td>
				</tr>
				<tr>
					<td style="text-align: center;" class="mw-submit" colspan="2">
						<input type="submit" value=" Log In " tabindex="5" id="wpLoginattempt" name="wpLogin">
						&nbsp;
						<input type="submit" value=" Forgot Password " id="wpLoginattempt" name="wpForgotPwd">
					</td>
				</tr>
			</tbody></table>
		</form>

		<br />

		<p>Don\'t have an account? Create <a href="/cms/register"><b>FREE account</b></a> now!</p>

		</div>		
		</div>';

	return $body;
}

?>