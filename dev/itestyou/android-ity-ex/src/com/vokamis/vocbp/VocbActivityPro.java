package com.vokamis.vocbp;

import com.vokamis.vocb.VocbActivityFree;

public class VocbActivityPro extends VocbActivityFree {
	
	public String getAppName(){
		return super.getAppName() + " Pro";
	}
	
	public String getAppVersionId(){
		return "ity-vocb-pro-" + getAppVersion();
	}
	
}
