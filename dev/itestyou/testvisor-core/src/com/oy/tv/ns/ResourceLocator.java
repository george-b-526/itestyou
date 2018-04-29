package com.oy.tv.ns;

import com.oy.shared.hmvc.IPropertyProvider;



public class ResourceLocator {

	public final static String CONN_STR_PROPERTY_NAME = "conn-str";
	public final static String DB_NAME_PROPERTY_NAME = "db-name";
	
	public static String getCSSHref(){
		return "/css/test.normal.css";
	}

	public static String getVocbCSSHref(){
		return "/css/vocb.normal.css";
	}
	
	public static String getTestHref(){
		return "/test/bin/view";
	}
	  
	public static String getMathMLBaseHREF(IPropertyProvider pp){
		return "//www.itestyou.com/api/ml/math?";
	} 
	
	public static String getInactiveHref(){
		return "http://www.cnn.com";
	}
	      
	public static String getCommonAlgebraLangPackJarName(){
		return "oy-tv-algebra-1.0.jar";
	}
	
	public static String getHomeDomainName(){
		return "www.itestyou.com";
	}
	
}
