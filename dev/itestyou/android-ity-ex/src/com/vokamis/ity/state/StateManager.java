package com.vokamis.ity.state;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.vokamis.ity.cmd.ShowHomeViewCmd;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.ApiService.RecoverEnvelope;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;
import com.vokamis.ity.service.ApiServerProxy;

public class StateManager {

	private IViewManager parent;
	private SettingsStore store;
	private ApiServerProxy proxy;

	public StateManager(IViewManager parent, Activity root, String apiEndpoint) {
		this.parent = parent;
		
		proxy = new ApiServerProxy(parent, apiEndpoint);
		store = new SettingsStore(root.getSharedPreferences("StateManager",
		    Context.MODE_PRIVATE));

		TelephonyManager tm = (TelephonyManager) root
		    .getSystemService(Context.TELEPHONY_SERVICE);

		initSettings(tm.getSimSerialNumber(),
		    android.provider.Settings.Secure.getString(root.getContentResolver(),
		        android.provider.Settings.Secure.ANDROID_ID));

		syncAccounts(getAccounts(AccountManager.get(root).getAccounts()));
	}

	private void initSettings(String simId, String androidId) {
		if (!store.hasSettings()) {
			AppSettings current = new AppSettings();

			current.runCount = 1;
			current.firstUseOn = new Date();
			current.lastUseOn = new Date();

			current.simId = simId;
			current.androidId = androidId;

			current.accounts = new ArrayList<ActorAccount>();
			current.accountIdx = -1;

			store.saveSettings(current);
		}
	}

	private static List<String> getAccounts(Account[] accounts) {
		List<String> names = new ArrayList<String>();
		for (Account item : accounts) {
			names.add(item.name);
		}
		return names;
	}

	private void syncAccounts(List<String> accounts) {
		AppSettings current = store.loadSettings();

		// remove those we have
		for (ActorAccount aa : current.accounts) {
			accounts.remove(aa.getEmail());
		}

		// add new email addresses to accounts
		for (String account : accounts) {
			ActorAccount aa = new ActorAccount();
			initAccount(aa, account);
			current.accounts.add(aa);
			current.accountIdx = current.accounts.size() - 1;
		}

		store.saveSettings(current);
	}

	private static void initAccount(ActorAccount aa, String account) {
		aa.runCount = 1;
		aa.lastUseOn = new Date();
		aa.email = account;
		aa.password = EntityPolicy.makeRandomPassword();
		aa.generatedPassword = true;
		aa.authToken = null;
		aa.lastGrade = 1;
	}

	public void checkpointProgress(int accountIdx, int grade, String url) {
		boolean expireToken = url.indexOf("expire_token=true") != -1;
		
		AppSettings current = store.loadSettings();
		ActorAccount aa = current.getAccount(accountIdx);

		aa.lastUseOn = new Date();
		aa.lastGrade = grade;
		
		if (expireToken){
			aa.authToken = null;
		}

		store.saveSettings(current);
		
		if (expireToken){
			parent.dispatch(new ShowHomeViewCmd());
		}

	}

	public static String makeProgressUrl(IViewManager parent, String token) {
		if (token == null){
			return  EntityPolicy.PROGRESS_VIEW_URL;
		} else {
  		return EntityPolicy.PROGRESS_VIEW_URL + "?app_session=" + token + 
      	"&inReferer=android-app-" + parent.getAppVersionId();
		}
	}
	
	public static String makeLeaderboardUrl(IViewManager parent, String token) {
		if (token == null){
			return  EntityPolicy.LEADERBOAR_VIEW_URL;
		} else {
  		return EntityPolicy.LEADERBOAR_VIEW_URL + "?app_session=" + token + 
      	"&inReferer=android-app-" + parent.getAppVersionId();
		}
	}
	
	public static String makeTestUrl(IViewManager parent, int grade, int unitId, String token) {
		String _action = "&action_id=0";
		
		String _unit = "";
		if (unitId != 0){
			_unit = "&inUnitId=" + unitId;
		}
		
		String _grade = "";
		if (grade != 0){
			_grade = "&inGradeId=" + grade;
		}
				
		return EntityPolicy.WIDGET_HOME_URL + 
			"?app_session=" + token + 
			"&inReferer=android-app-" + parent.getAppVersionId();
	}

	private String getDeviceId(){
		AppSettings current = store.loadSettings();
		return "{androidId:" + current.getAndroidId() + "; simId:" + current.getSimId() + ";}";
	}
	
	public void recover(String email) throws PolicyException {
		EntityPolicy.assertValidEmail(email);

		RecoverEnvelope recover = proxy.recover(email, getDeviceId());

		if (recover.getStatus() == RecoverEnvelope.Status.FAILED_UNKNOWN_CUSTOMER){
			throw new PolicyException("Unknown account.");
		}
		
		if (recover.getStatus() == RecoverEnvelope.Status.FAILED){
			if (recover.getReason() != null){
				throw new PolicyException(recover.getReason());
			} else {
				throw new PolicyException("Unable to recover password.");
			}
		}
	}
	
	public int register(String email, String pwd, int grade, boolean existing)
	    throws PolicyException {
		AppSettings current = store.loadSettings();
		
		RegisterEnvelope result = proxy.register(email, pwd, existing, getDeviceId());
		
		if (RegisterEnvelope.Status.SUCCESS == result.getStatus()
		    || RegisterEnvelope.Status.FAILED_EXISTS_SUCCESS_AUTH == result
		        .getStatus()) {

			// find account
			current.accountIdx = -1;
			for (int i = 0; i < current.getAccounts().size(); i++) {
				ActorAccount account = current.getAccount(i);
				if (email.equals(account.getEmail())) {
					current.accountIdx = i;
					break;
				}
			}

			// create or retrieve
			ActorAccount aa;
			if (current.accountIdx == -1) {
				aa = new ActorAccount();

				initAccount(aa, email);
				aa.password = pwd;

				current.accounts.add(aa);
				current.accountIdx = current.accounts.size() - 1;
			} else {
				aa = current.getAccount(current.accountIdx);
			}

			// update account
			aa.authToken = result.getToken();
			aa.lastGrade = grade;

			// store
			store.saveSettings(current);

			return current.accountIdx;
		}

		if (RegisterEnvelope.Status.FAILED_EXISTS_FAILED_AUTH == result
		    .getStatus()) {
			throw new PolicyException("Wrong user name or password.");
		}

		if (result.getReason() != null){
			throw new PolicyException(result.getReason());
		}
		  
		throw new PolicyException("Unknown error ("
		    + result.getStatus().ordinal() + ").");
	}

	public void selectAccount(int accountIdx) {
		AppSettings current = store.loadSettings();
		if (accountIdx < 0 || accountIdx >= current.getAccounts().size()) {
			throw new RuntimeException("Error in account selection.");
		}

		current.accountIdx = accountIdx;
		store.saveSettings(current);
	}

	public AppSettings getSettings() {
		return store.loadSettings();
	}

	public static boolean sameAccount(ActorAccount a, ActorAccount b) {
		if (a == b)
			return true;
		if (a == null && b != null)
			return false;
		if (a != null && b == null)
			return false;
		return a.getEmail().equals(b.getEmail());
	}

}
