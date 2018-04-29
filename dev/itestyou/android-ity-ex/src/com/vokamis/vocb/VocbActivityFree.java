package com.vokamis.vocb;

import com.vokamis.ity.RootActivity;

public class VocbActivityFree extends RootActivity {
	
	public String getAppName(){
		return "A+ ITestYou";
	}

	public final String getEditionName(){
		return "English Vocabulary";
	}

	public String getAppVersionId(){
		return "ity-vocb-" + getAppVersion();
	}
	
}
