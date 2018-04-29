package com.oy.tv.dao.runtime;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.perf.monitor.SimpleCounterMonitor;
import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.ResponseBO;

public class ResponseDAO {

	private static SimpleCounterMonitor monitor = new SimpleCounterMonitor(
			ResponseDAO.class, "VOTES_QUEUED", 
			"This monitor reports number of items inserted into REQ_RESPONSE_QUEUE."
		); 
	
	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(     	
			"CREATE TABLE IF NOT EXISTS REQ_RESPONSE_QUEUE (" +
			"	  REQ_ID INTEGER NOT NULL AUTO_INCREMENT," +
			"	  REQ_USR_ID INTEGER NOT NULL," +
			"	  REQ_UNI_ID INTEGER NOT NULL," +
			"	  REQ_VAR_ID INTEGER NOT NULL," +
			"	  REQ_ISSUED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  REQ_RECEIVED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  REQ_PASSED SMALLINT NOT NULL," +
			"	  REQ_UNIT_DATA MEDIUMBLOB," +
			"	  REQ_LOCALE VARCHAR(5)," +
			"	  REQ_REFERER VARCHAR(255)," +
			"	  REQ_CLIENT_ADDRESS VARCHAR(15)," +
			"	  REQ_CLIENT_AGENT VARCHAR(255)," +
			"	  REQ_SESSION_ID VARCHAR(255)," +
			"	  REQ_ACTIVITY_ID VARCHAR(32)," +
			"	  UNIQUE INDEX idxID (REQ_ID), " +
			"	  		 INDEX idxUSR (REQ_USR_ID), " +
			"	  		 INDEX idxVAR (REQ_VAR_ID)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);         
	}
	
	public enum Score {
		FAIL, PASS, SKIP
	}

	static ResponseBO loadResponse(AnyDB db, int id) throws SQLException {
  		AnyResultSetContext result = db.execSelect(
      		"SELECT * FROM REQ_RESPONSE_QUEUE WHERE REQ_ID = LAST_INSERT_ID();");
      result.rs().next();  
        
      ResponseBO res = new ResponseBO();
      
      res.setId(result.rs().getInt("REQ_ID"));  
      res.setUserId(result.rs().getInt("REQ_USR_ID"));
      res.setUnitId(result.rs().getInt("REQ_UNI_ID"));
      res.setVariationId(result.rs().getInt("REQ_VAR_ID"));

      res.setSubmittedOn(Calendar.getInstance());
      res.getSubmittedOn().setTime(result.rs().getTimestamp("REQ_SUBMITTED_ON"));
      
      res.setReceivedOn(Calendar.getInstance());
      res.getReceivedOn().setTime(result.rs().getTimestamp("REQ_RECEIVED_ON"));
        
      res.setPassed(result.rs().getInt("REQ_PASSED"));
        
      res.setReferrer(result.rs().getString("REQ_REFERER"));
      res.setClientAddress(result.rs().getString("REQ_CLIENT_ADDRESS"));
      res.setClientAgent(result.rs().getString("REQ_CLIENT_AGENT"));
      res.setSessionId(result.rs().getString("REQ_SESSION_ID"));
      res.setActivityId(result.rs().getString("REQ_ACTIVITY_ID"));
             
      return res;		
	}

	public static void enqueue(AnyDB db, int userId, int unitId, int variationId, 
			Date issued, Date received, Score score, String referer, String clientAddress, 
			String clientAgent, String sessionId, String activityId, String unitData, 
			String locale) throws SQLException {		
		enqueue(db, AllDAO.NS_DEFAULT, userId, unitId, variationId, 
				issued, received, score, referer, clientAddress, 
				clientAgent, sessionId, activityId, unitData, locale);
	}
	
	public static void enqueue(AnyDB db, String ns, int userId, int unitId, int variationId, 
			Date issued, Date received, Score score, String referer, String clientAddress, 
			String clientAgent, String sessionId, String activityId, String unitData, String locale) 
			throws SQLException {		
		
		if (issued == null || issued.getTime() == 0){
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(2011, 0, 1);
			
			cal.set(GregorianCalendar.HOUR, 0);
			cal.set(GregorianCalendar.MINUTE, 0);
			cal.set(GregorianCalendar.SECOND, 0);
			cal.set(GregorianCalendar.MILLISECOND, 0);
			cal.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
			
			issued = cal.getTime();
		}
		
		// format database selector
		if (ns != null){
			ns = ns + ".";
		} else {
			ns = "";
		}
		
		db.execUpdate(
			"INSERT INTO " + ns + "REQ_RESPONSE_QUEUE (" + 
			"REQ_USR_ID, REQ_UNI_ID, " + 
			"REQ_VAR_ID, REQ_ISSUED_ON, REQ_RECEIVED_ON, REQ_PASSED, REQ_REFERER, REQ_CLIENT_ADDRESS, REQ_CLIENT_AGENT, REQ_SESSION_ID, REQ_ACTIVITY_ID, " +
			"REQ_UNIT_DATA, REQ_LOCALE" +
			") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COMPRESS(?), ?);",
			new Object []{
				userId, unitId, variationId, 
				new java.sql.Timestamp(issued.getTime()),
				new java.sql.Timestamp(received.getTime()),
				score.ordinal(),
				referer,
				clientAddress,
				clientAgent,
				sessionId,  
				activityId,
				unitData,
				locale
			}
		);  

		monitor.incValue(1);
	}
	  	
}
