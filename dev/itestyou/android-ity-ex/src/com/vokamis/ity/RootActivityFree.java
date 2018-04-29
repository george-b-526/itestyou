package com.vokamis.ity;


public class RootActivityFree extends RootActivity {

	public String getAppName(){
		return "A+ ITestYou";
	}
		
	public final String getEditionName(){
		return "Math Worksheets";
	}
	
	public String getAppVersionId(){
		return "ity-" + getAppVersion();
	}
	
}
