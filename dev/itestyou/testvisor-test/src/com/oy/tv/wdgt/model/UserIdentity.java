package com.oy.tv.wdgt.model;

public class UserIdentity {

	public enum Origin {
		NONE, WEB, APP
	}
	
	public String sessionId;
	public int userId;
	public String name;
	public String phpToken;
	     
	public String clientAddress;
	public String clientAgent;
	  
	public boolean sessionExpired;
	
	public Origin origin = Origin.NONE;

	public boolean isPro = false;
	
	public boolean isLoggedIn(){
		return userId != 0;
	}  
	
}
