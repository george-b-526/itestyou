package com.oy.tv.util;

import java.security.Provider;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UtilMD5 {

	public static void init(){
		Provider sunJce = new com.sun.crypto.provider.SunJCE();
        Security.insertProviderAt(sunJce,0);		
	}
	
    public static String string2md5HMA(String keyString, String message)  {
        SecretKey key = new SecretKeySpec(keyString.getBytes(), "HmacMD5");
        try {
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);
            return toHEX(mac.doFinal(message.getBytes())); 
        } catch (Exception e) { 
            System.err.println("problem creating HmacMD5 hash: " + e.getMessage());
            return "";
        }
    }
    
    private static String toHEX(byte[] digest) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < digest.length; ++i) { 
            String hx = Integer.toHexString(0xFF & digest[i]); 
            if (hx.length() == 1) { 
                hx = "0" + hx; 
            } 
            hexString.append(hx); 
        }
        return hexString.toString();
    } 
    
    public static void main(String [] args){
    	System.out.println(string2md5HMA("nimda!", "ity"));
    }
	
}
