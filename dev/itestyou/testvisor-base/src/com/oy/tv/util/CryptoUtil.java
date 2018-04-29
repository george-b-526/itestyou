package com.oy.tv.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

	//
	// adopted from http://propaso.com/blog/?cat=6
	//
	
	private static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(str.substring(i * 2,
						i * 2 + 2), 16);
			}
			return buffer;
		}
	}
	
	private static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		} else {
			int len = data.length;
			String str = "";
			for (int i = 0; i < len; i++) {
				if ((data[i] & 0xFF) < 16) {
					str = str + "0"
							+ java.lang.Integer.toHexString(data[i] & 0xFF);
				} else {
					str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
				}
			}
			return str;
		}
	}
	
	private static String pad(String text){
		StringBuffer sb = new StringBuffer(text); 
		int padLen = 16 - text.length() % 16;
		for (int i=0; i < padLen; i++){
			sb.append(" ");
		}
		return sb.toString();
	}
	
	public static String decrypt(String text, String key) throws Exception {
		key = pad(key);
		
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		IvParameterSpec ivSpec = new IvParameterSpec("fedcba9876543210".getBytes());
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] outText = cipher.doFinal(hexToBytes(text));
		return new String(outText).trim();
	}
		
	public static String encrypt(String text, String key) throws Exception {
		text = pad(text);
		key = pad(key);
		
		String iv = "fedcba9876543210";

		SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
		IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
		byte[] encrypted = cipher.doFinal(text.getBytes());

		return bytesToHex(encrypted);
	}
	
	public static void main(String[] args) throws Exception {
		String key = "pass phrase";
		
		String ctext;
		{
			String ptext = "action_id=0&inTestId=123&inUnitIds=1&inUnitIds=1&inExitUrl=http://www.cnn.com"; 
				//"Hello World";
			ctext = encrypt(ptext, key);
			System.out.println(ptext + ":" + ctext);
		}
		
		{
			//String ctext = "444e6969a269829a3e59a86300614fc5";
			String ptext = decrypt(ctext, key);
			System.out.println(ctext + ":" + ptext);
		}
	}

	/*
	
	<?php
	  $cipher     = "rijndael-128";
	  $mode       = "cbc";
	  $plain_text = "Hello World";
	  $secret_key = "01234567890abcde";
	  $iv         = "fedcba9876543210";

	  td = mcrypt_module_open($cipher, "", $mode, $iv);
	  mcrypt_generic_init($td, $secret_key, $iv);
	  $cyper_text = mcrypt_generic($td, $plain_text);
	  echo bin2hex($cyper_text);
	  mcrypt_generic_deinit($td);
	  mcrypt_module_close($td);
	?>
	
	<?php
		function hex2bin($hexdata) {
		  $bindata="";
	
		  for ($i=0;$i<strlen($hexdata);$i+=2) {
		   $bindata.=chr(hexdec(substr($hexdata,$i,2)));
		  }
	
		  return $bindata;
		}
	
		$cipher     = "rijndael-128";
		$mode       = "cbc";
		$secret_key = "01234567890abcde";
		$iv         = "fedcba9876543210";
	
		$td = mcrypt_module_open($cipher, "", $mode, $iv);
	
		mcrypt_generic_init($td, $secret_key, $iv);
		$decrypted_text = mdecrypt_generic($td, hex2bin("444e6969a269829a3e59a86300614fc5"));
		echo trim($decrypted_text);
		mcrypt_generic_deinit($td);
		mcrypt_module_close($td);
	?>	
	
	*/
}
