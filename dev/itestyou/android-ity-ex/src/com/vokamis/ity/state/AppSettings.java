package com.vokamis.ity.state;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AppSettings implements Serializable {
	public static final long serialVersionUID = 0;
	
	int runCount;
	
	boolean firstTime;
	Date firstUseOn;
	Date lastUseOn;
	
	String androidId;
	String simId;

	List<ActorAccount> accounts;
	int accountIdx;
	
	public int getRunCount() {
  	return runCount;
  }

	public boolean isFirstTime() {
  	return firstTime;
  }

	public Date getFirstUseOn() {
  	return firstUseOn;
  }

	public Date getLastUseOn() {
  	return lastUseOn;
  }

	public String getAndroidId() {
  	return androidId;
  }

	public String getSimId() {
  	return simId;
  }

	public List<ActorAccount> getAccounts() {
		return Collections.unmodifiableList(accounts);
	}

	public int getAccountIdx() {
		return accountIdx;
	}

	public ActorAccount getAccount(){
		if (accountIdx == -1){
			return null;
		} else {
			return accounts.get(accountIdx);
		}
	}  

	public ActorAccount getAccount(int idx){
		if (idx == -1){
			return null;
		} else {
			return accounts.get(idx);
		}
	}
	
}
