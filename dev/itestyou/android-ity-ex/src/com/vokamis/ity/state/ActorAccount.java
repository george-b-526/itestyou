package com.vokamis.ity.state;

import java.io.Serializable;
import java.util.Date;

public class ActorAccount implements Serializable {
	public static final long serialVersionUID = 0;

	String email;
	String password;
	boolean generatedPassword;
	
	String authToken;
	
	int runCount;
	int clickCount;

	Date lastUseOn;
	int lastGrade;
	
	public boolean isGeneratedPassword() {
  	return generatedPassword;
  }

	public String getAuthToken() {
  	return authToken;
  }
	
	public String getEmail() {
  	return email;
  }
	
	public String getPassword() {
  	return password;
  }
	
	public int getRunCount() {
  	return runCount;
  }
	
	public int getClickCount() {
  	return clickCount;
  }
	
	public Date getLastUseOn() {
  	return lastUseOn;
  }

	public int getLastGrade() {
  	return lastGrade;
  }

}
