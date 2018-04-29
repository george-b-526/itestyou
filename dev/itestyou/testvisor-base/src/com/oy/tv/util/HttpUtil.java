package com.oy.tv.util;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {

	public static String getRemoteAddr(HttpServletRequest req){
		String ip = req.getRemoteAddr();
		if ("127.0.0.1".equals(ip)){
			String xforward = req.getHeader("X-Forwarded-For");
			if (xforward != null && xforward.length() != 0){
				ip = xforward.split(",")[0].trim();
			}			
		}  
		return ip;
	}
	
}
