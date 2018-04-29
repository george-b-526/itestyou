package com.vokamis.langp;

import com.vokamis.lang.LangActivityFree;

public class LangActivityPro extends LangActivityFree {
	
	public String getAppName(){
		return super.getAppName() + " Pro";
	}
	
	public String getAppVersionId(){
		return "ity-lang-pro-" + getAppVersion();
	}
	
}
