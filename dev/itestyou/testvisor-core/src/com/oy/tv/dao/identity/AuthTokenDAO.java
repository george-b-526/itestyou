package com.oy.tv.dao.identity;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Date;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.AuthTokenBO;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.util.SmartCache;
import com.oy.tv.util.SmartCacheMonitor;
import com.oy.tv.util.UtilMD5;
import com.vokamis.ity.rpc.PolicyException;

public class AuthTokenDAO {

	private static SmartCacheMonitor monitor = new SmartCacheMonitor(
		AuthTokenDAO.class, "TOKENS", 
		"This monitor reports Tokens cache."
	); 
	
	private static SmartCache<String, CustomerInfo> cache = 
		new SmartCache<String, CustomerInfo> (5 * 60 * 1000, 100, monitor);

	private static Object lock = new Object();
	
	public static boolean CAN_CACHE = false;
	
	private static SecureRandom sr = new SecureRandom();
	
	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS AUT_AUTH_TOKEN (" +
			"	  AUT_ID INTEGER NOT NULL," +
			"	  AUT_CUS_ID INTEGER NOT NULL," +
			"	  AUT_TOKEN VARCHAR(255) NOT NULL," +
			"	  AUT_CREATED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  AUT_IP_ADDRESS VARCHAR(32) NOT NULL," +
			"	  AUT_USER_AGENT VARCHAR(1024) NOT NULL," +
			"	  AUT_DEVICE_ID VARCHAR(255) NOT NULL," +
			"	  AUT_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (AUT_ID)," +
			"	  INDEX idxAUT_CUS_ID (AUT_CUS_ID, AUT_DEVICE_ID)," +
			"	  INDEX idxAUT_TOKEN (AUT_TOKEN, AUT_STATE)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);  	 	    
	}
	
	public static class CustomerInfo {
		Integer userId;
		boolean isPro;
		
		public Integer getUserId() {
    	return userId;
    }

		public boolean isPro() {
    	return isPro;
    }
	}

	public static CustomerInfo getCustomerIdFor(AnyDB db, String token) throws SQLException {
		// get
		if (CAN_CACHE){
  		synchronized (lock) {
  			CustomerInfo ci = cache.get(token);
  	    if (ci != null){
  	    	return ci;
  	    }
      }
		}
		
		// load
		CustomerInfo ci = new CustomerInfo();
		{
  		AnyResultSetContext result = db.execSelect(
  				"SELECT * FROM ITY_IDENTITY.AUT_AUTH_TOKEN WHERE AUT_TOKEN = ? AND AUT_STATE = ?", 
  				new Object []{token, EObjectState.ACTIVE.value()});
  		try {
  			if (result.rs().next()){
  				ci.userId = result.rs().getInt("AUT_CUS_ID");
  				
  				{
    				String agent = result.rs().getString("AUT_USER_AGENT");
    				
    				boolean applePro = 
    					agent != null && 
    					agent.indexOf("ITestYou%20Pro") != -1 || 
  						agent.indexOf("ITestYouPro") != -1;
    				    				
    				boolean androidPro = 
    					agent != null && 
    					agent.indexOf("ity-pro") != -1 || 
  						agent.indexOf("ity-vocb-pro") != -1 || 
  						agent.indexOf("ity-lang-pro") != -1; 
      				
    				ci.isPro = applePro || androidPro;
  				}

  			}
  		} finally {
  			result.rs().close();
  		}
		}
   
		// put
		if (CAN_CACHE){
  		synchronized (lock) {
  	    if (ci.userId != null){
  	    	cache.put(token, ci);
  	    }
      }
		}
		  
		return ci;
	}
	
	public static AuthTokenBO createAuthToken  (
				AnyDB db, String name, String pwd, String ip, String userAgent, String deviceId) 
				throws SQLException, PolicyException {		
		AuthTokenBO token = new AuthTokenBO();
		
		try {
  		db.trxBegin();
  		try {
  			CustomerBO customer = CustomerDAO.loadCustomer(db, name, pwd);
  			if (customer == null){
  				throw new PolicyException("Unknown user name.");
  			}
  			
  			if (!customer.getPasswordHash().equals(CustomerDAO.hashPassword(pwd))){
  				throw new PolicyException("Bad user name or pasword.");
  			}
    			
  			// allocate new token
  			token.setCustomer(customer);
  			token.setToken(
  					"{" + UtilMD5.string2md5HMA("" + sr.nextLong(), name) + "-" + 
  					UtilMD5.string2md5HMA(name, "token") + "}");
    			
  			// insert new token
  			int id = AnyDAO.nextId(db, "AUT_AUTH_TOKEN", "AUT_ID");
  			db.execUpdate(    
  				"INSERT INTO AUT_AUTH_TOKEN (" + 
  				"AUT_ID, AUT_CUS_ID, AUT_TOKEN, AUT_CREATED_ON, " + 
  				"AUT_IP_ADDRESS, AUT_USER_AGENT, AUT_DEVICE_ID, AUT_STATE" + 
  				") VALUES (" + 
  				"?, ?, ?, ?, " + 
  				"?, ?, ?, ?)",
  				new Object [] {
  					id, customer.getId(), token.getToken(), new Date(), 
  					ip, userAgent, deviceId, EObjectState.ACTIVE.value()
  				}
  			);
  		} finally {
  			db.trxEnd();
  		}
		} catch (PolicyException pe){
			throw pe;
		} catch (Exception e){
			throw new RuntimeException(e);
		} 
		
		return token;
	}   
	
}
