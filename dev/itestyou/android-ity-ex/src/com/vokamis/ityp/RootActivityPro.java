package com.vokamis.ityp;

import com.vokamis.ity.RootActivityFree;

public class RootActivityPro extends RootActivityFree {
		
	public String getAppName(){
		return super.getAppName() + " Pro";
	}
	
	public String getAppVersionId(){
		return "ity-pro-" + getAppVersion();
	}
	
}
