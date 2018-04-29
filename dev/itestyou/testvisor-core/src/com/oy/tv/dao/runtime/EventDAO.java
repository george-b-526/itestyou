package com.oy.tv.dao.runtime;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EEventState;
import com.oy.tv.schema.core.EEventType;
import com.oy.tv.schema.core.EventBO;

public class EventDAO {
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS EVT_EVENT (" +
			"	  EVT_ID INTEGER NOT NULL AUTO_INCREMENT," +
			"	  EVT_USR_ID INTEGER NOT NULL," +
			"	  EVT_TYPE INTEGER NOT NULL," +
			"	  EVT_POSTED_ON TIMESTAMP," +
			"	  EVT_SOURCE VARCHAR(255) NOT NULL," +
			"	  EVT_DATA MEDIUMBLOB," +
			"	  EVT_STATE INTEGER NOT NULL," +
			"	  EVT_STATE_REASON MEDIUMBLOB," +
			"	  UNIQUE INDEX idxID (EVT_ID)," +  
			"	  		 INDEX idxTYPE (EVT_TYPE, EVT_STATE)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}

	public static EventBO queueEvent(AnyDB db, CustomerBO user, EEventType type, 
			String source, String data) throws SQLException {
		return queueEvent(db, AllDAO.NS_DEFAULT, user, type, source, data);
	}
	
	public static EventBO queueEvent(AnyDB db, String ns, CustomerBO user, EEventType type, 
			String source, String data) throws SQLException {
		db.trxBegin();
		try {		
  		Date now = new Date();
  		EEventState state = EEventState.QUEUED;
  		
  		db.execUpdate(
  			"INSERT INTO " + AnyDB.formatNS(ns) + "EVT_EVENT (EVT_USR_ID, EVT_TYPE, EVT_POSTED_ON, EVT_SOURCE, EVT_DATA, EVT_STATE) VALUES (?, ?, ?, ?, COMPRESS(?), ?)",
  			new Object [] {user.getId(), type.value(), now, source, data, state.value()}
  		);   	

  		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
  		result.rs().next();
  		
  		EventBO evt = new EventBO();
  		evt.setId(result.rs().getInt("ID"));
  		evt.setOwnerId(user.getId());
  		evt.setType(type);
  		
  		evt.setPostedOn(Calendar.getInstance());
  		evt.getPostedOn().setTime(now);
  
  		evt.setSource(source);
  		evt.setData(data);
  		evt.setState(state);
  		
  		return evt;
		} finally {
			db.trxEnd();
		}						

	}
	 	
}
