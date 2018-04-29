package com.vokamis.lang;

import com.vokamis.ity.RootActivity;

public class LangActivityFree extends RootActivity {
	
	public String getAppName(){
		return "A+ ITestYou";
	}

	public final String getEditionName(){
		return "Foreign Languages";
	}

	public String getAppVersionId(){
		return "ity-lang-" + getAppVersion();
	}
	
}
