package com.vokamis.ity.rpc;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityPolicy {
	
	public static final String HOME_DOMAIN = "itestyou.com";
	public static final String LEADERBOAR_VIEW_URL = "http://www.itestyou.com/view/leaderboard";
	public static final String PROGRESS_VIEW_URL = "http://www.itestyou.com/view/progress";
	public static final String WIDGET_HOME_URL = "http://www.itestyou.com/view/home";
	
	private static final String expires = "2018/12/31";
	
	public static final int MIN_PWD_LEN = 6;
	private static final int MIN_EMAIL_LEN = 6;

	private static Random rnd;
	private static List<String> abc;
	
	public static boolean mustUpgrade(){
		try {
  		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
  		Date expiresOn = format.parse(expires);

  		Date today = new Date();
  		return today.after(expiresOn);
		} catch (Exception e){
			return true;
		}
	}
	
	public static void assertNewAccountData(String _email, String _pwd) throws PolicyException {
		assertNewAccountData(_email, _pwd, _pwd);
  }

	public static void assertValidEmail(String _email) throws PolicyException {		
		if (_email == null || _email.length() < MIN_EMAIL_LEN || !validate(_email)) {
			throw new PolicyException("Please enter valid email address.");
		}		
  }

	public static boolean isValidPassword(String _pwd) throws PolicyException {		
		return !(_pwd == null || _pwd.length() < MIN_PWD_LEN);
	}
	
  static final String EMAIL_PATTERN = 
    "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	
	private static boolean validate(final String email){
    Pattern pattern = Pattern.compile(EMAIL_PATTERN);
  	Matcher matcher = pattern.matcher(email);
	  return matcher.matches();
  }
	
	public static void assertNewAccountData(String _email, String _pwd1, String _pwd2) 
	throws PolicyException {

	assertValidEmail(_email);
	  
	if (_pwd1 == null || _pwd1.length() < MIN_PWD_LEN || _pwd2 == null
	    || _pwd2.length() < MIN_PWD_LEN) {

		throw new PolicyException("Password must be " + MIN_PWD_LEN + " characters or longer.");
	}

	if (!_pwd1.equals(_pwd2)) {
		throw new PolicyException("Passwords don't match.");
	}
}
	
	public static String makeRandomPassword() {
		if (rnd == null){
			try {
				rnd = SecureRandom.getInstance("SHA1PRNG");
			} catch (Exception e) {
				e.printStackTrace(System.err);
				rnd = null;
			}
			if (rnd == null) {
				rnd = new Random();
				rnd.setSeed(System.nanoTime());
			}
			
			abc = new ArrayList<String>();
			for (char c = 'a'; c <= 'z'; c++){
				abc.add("" + c);
			}
			for (char c = 'A'; c <= 'Z'; c++){
				abc.add("" + c);
			}
			for (char c = '0'; c <= '9'; c++){
				abc.add("" + c);
			}	
		}
		
		String pwd = "";
		for (int i=0; i < MIN_PWD_LEN; i++){
			int idx = rnd.nextInt(abc.size());
			pwd += abc.get(idx);
		}
		
		return pwd;
	}

	
}