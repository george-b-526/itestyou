<?php

function ity_render_view_account(){
	global $ity_render_view_account_lambda;
	if ($ity_render_view_account_lambda){
		return $ity_render_view_account_lambda;
	}

	include_once "../lib/dao.php";

	// bind to user
	$reason = null;  
	$name = null;
	$is_pro = false;
	$app_session = $_GET["app_session"];
	$user_id = ity_lookup_token($app_session, $reason, $name, $is_pro);
	if ($user_id == null){
		return "<p align='center'>Please <a href='/cms/log-in?ity_returnto=http://www.itestyou.com/cms/my-account'>login</a> to see this page.</p>";
	}

	// action
	$is_change_pwd = isset($_POST['wpChangePwd']) && $_POST['wpChangePwd'] != null;
	$is_comm_prefs = isset($_POST['wpSaveCommPrefs']) && $_POST['wpSaveCommPrefs'] != null;

	// prepare
	$msgChangePwd = "";
	$msgCommPrefs = "";

	// dispatch
	if ($is_comm_prefs){
		$update = array();
		ity_get_user_properties($user_id, $update);

		$update['notify_rank'] = isset($_POST['notify_rank']) ? "1" : "0";
		$update['notify_learn'] = isset($_POST['notify_learn']) ? "1" : "0";
		$update['notify_update'] = isset($_POST['notify_update']) ? "1" : "0";

		ity_put_user_properties($user_id, $update);

		$msgCommPrefs = "<p style='background-color: #AAFFAA;'>Notification preferences saved.</p>";
	}

	// dispatch
	if ($is_change_pwd){
		$old_pwd = $_POST['wpPasswordNow'];
		$pwd = $_POST['wpPassword1'];
		$pwd2 = $_POST['wpPassword2'];

		$reason = null;

		// check pwd
		if ($pwd != $pwd2){
			$reason = "New passwords don't match.";
		}

		if (strlen($pwd) < 6){
			$reason = "New password is too short (6 characters minimum).";
		}

		// register
		if ($reason == null) {
			if (ity_repassword($name, $old_pwd, $pwd, $reason)){
				$msgChangePwd = "<p style='background-color: #AAFFAA;'>Password changed.</p>";
			}
		}

		if ($reason != null){
			$msgChangePwd = "<p style='background-color: #FFAAAA;'>".$reason."</p>";
		}		
	}

	// load
	$props = array();
	ity_get_user_properties($user_id, $props);
	
	// render
	$body = "";

	// links
	$body .= '
		<a name="comm_prefs"></a>
		<div style="width: 100%;" align="center">
		<div style="background-color: #EEFFEE; border: 2px solid #008800; width: 450px; padding: 16px; margin-top: 8px;" id="userloginForm">
			<div id="userloginprompt"><b>Account</b><p>View basic information about you.</p><hr /></div>
			<table><tbody>
				<tr>
					<td class="mw-label"><label for="wpPassword1">Email:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><b>'.htmlentities($name).'</b></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword1">Activity:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;">
						<a href="/cms/week-summary">Week Summary</a><br />
						<a href="/cms/weekly-progress">Weekly Progress</a><br />
						<a href="/cms/my-vocabulary">My Vocabulary</a><br />
						<a href="/cms/my-skills">My Skills & Metrics</a>
						</p>
					</td>
				</tr>
			</tbody></table>
		</div>
		</div>';

	$body .= '<p>&nbsp;</p>';

	// notifications
	$body .= '
		<a name="comm_prefs"></a>
		<div style="width: 100%;" align="center">
		<div style="background-color: #FFFFFF; border: 2px solid #008800; width: 450px; padding: 16px; margin-top: 8px;" id="userloginForm">
		<form action="http://www.itestyou.com/cms/my-account#comm_prefs" method="post" name="myaccount">
			<div id="userloginprompt"><b>Notifications</b><p>Control when and how often ITestYou sends emails to you.</p>'.$msgCommPrefs.'<hr /></div>
			<table><tbody>
				<tr>
					<td colspan="2" align="left">
						<b>Activity</b>
					</td>
				</tr>
				<tr>
					<td align="right">Email me when</td>
					<td style="padding: 8px;" align="left" valign="middle">
						<input type="checkbox" name="notify_rank" '.($props["notify_rank"] ? 'checked="checked"' : '').' /> I am high ranked on the Leaderboard
						<br />
						<input type="checkbox" name="notify_learn" '.($props["notify_learn"] ? 'checked="checked"' : '').' /> I have new material to learn
					</td>
				</tr>
				<tr>
					<td colspan="2" align="left">
						<hr />
						<b>Updates</b>
					</td>
				</tr>
				<tr>
					<td align="right">Email me with</td>
					<td style="padding: 8px;" align="left" valign="middle">
						<input type="checkbox" name="notify_update" '.($props["notify_update"] ? 'checked="checked"' : '').' /> Updates about ITestYou products, features, and tips
						<br />
						<input type="checkbox" checked="checked" disabled="true" /> Critical updates related to my ITestYou account
					</td>
				</tr>
				<tr>
					<td style="text-align: center;" class="mw-submit" colspan="2">
						<hr /><br />
						<input type="submit" value=" Save Changes " tabindex="4" name="wpSaveCommPrefs">
					</td>
				</tr>
			</tbody></table>
		</form>
		</div>
		</div>';

	$body .= '<p>&nbsp;</p>';
	
	$body .= '
		<a name="change_pwd"></a>
		<div style="width: 100%;" align="center">
		<div style="background-color: #FFFFFF; border: 2px solid #008800; width: 450px; padding: 16px; margin-top: 8px;" id="userloginForm">
		<form action="https://www.itestyou.com/cms/my-account#change_pwd" method="post" name="myaccount">
			<div id="userloginprompt"><b>Change Password</b><p>Change your current password for a new one.</p>'.$msgChangePwd.'<hr /></div>
			<table><tbody>
				<tr>
					<td class="mw-label"><label for="wpPassword1">Curent Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="2" id="wpPassword1" name="wpPasswordNow" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword1">New Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="2" id="wpPassword1" name="wpPassword1" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td class="mw-label"><label for="wpPassword2">Re-Enter New Password:</label></td>
					<td class="mw-input">
						<p style="margin: 0px;"><input type="password" size="20" value="" tabindex="3" id="wpPassword2" name="wpPassword2" class="loginPassword"></p>
					</td>
				</tr>
				<tr>
					<td style="text-align: center;" class="mw-submit" colspan="2">
						<hr /><br />
						<input type="submit" value=" Change My Password " tabindex="4" name="wpChangePwd">
					</td>
				</tr>
			</tbody></table>
		</form>
		</div>
		</div>';

	return $body;
}

?>