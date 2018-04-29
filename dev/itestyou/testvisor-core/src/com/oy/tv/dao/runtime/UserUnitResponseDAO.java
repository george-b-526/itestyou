package com.oy.tv.dao.runtime;

import java.sql.SQLException;

import com.oy.tv.app.IDatabaseCtx;
import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class UserUnitResponseDAO {
	
	static void init(AnyDB db) throws SQLException {
		
		// checkpoints for managing rollup intermediate states and 
		db.execUpdate(     
			"CREATE TABLE IF NOT EXISTS CPT_CHECKPOINT (" +
			"	  CPT_KEY VARCHAR(255) NOT NULL," +
			"	  CPT_INTEGER_VALUE INTEGER," +
			"	  CPT_BLOB_VALUE MEDIUMBLOB," +  
			"	  UNIQUE INDEX idxKEY (CPT_KEY)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);        
		
		// storage for the unit summary of the user activity for one week
		db.execUpdate(	
			"CREATE TABLE IF NOT EXISTS UUR_UNIT_ROLLUP_WEEKLY (" +
			"	  UUR_USR_ID INTEGER NOT NULL," +
			"	  UUR_UNI_ID INTEGER NOT NULL," +
			"	  UUR_YEAR INTEGER NOT NULL," +
			"	  UUR_WEEK INTEGER NOT NULL," +
			"	  UUR_CORRECT_COUNT INTEGER NOT NULL," +
			"	  UUR_INCORRECT_COUNT INTEGER NOT NULL," +
			"	  UUR_SKIPPED_COUNT INTEGER NOT NULL," +
			"	  UUR_UNIT_DATA MEDIUMBLOB," +
			"	  UUR_LAST_RESPONSE_ON TIMESTAMP NOT NULL," +						
			"	  UNIQUE INDEX idxFWD (UUR_USR_ID, UUR_UNI_ID, UUR_YEAR, UUR_WEEK), " +
			"			INDEX idxYearWeek (UUR_YEAR, UUR_WEEK), "+
			"			INDEX idxDATE (UUR_LAST_RESPONSE_ON) " +
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);       
     				
		// storage for the unit summary of the user activity for all time 
		db.execUpdate(     
				"CREATE TABLE IF NOT EXISTS UUS_UNIT_ROLLUP_TOP (" +
				"	  UUS_USR_ID INTEGER NOT NULL," +
				"	  UUS_UNI_ID INTEGER NOT NULL," +
				"	  UUS_CORRECT_COUNT INTEGER NOT NULL," +
				"	  UUS_INCORRECT_COUNT INTEGER NOT NULL," +
				"	  UUS_SKIPPED_COUNT INTEGER NOT NULL," +
				"	  UUS_DISTINCT_COUNT INTEGER NOT NULL," + 
				"	  UUS_UNIT_DATA MEDIUMBLOB," +						
				"	  UUS_LAST_RESPONSE_ON TIMESTAMP NOT NULL," +
				"	  UNIQUE INDEX idxKEY (UUS_USR_ID, UUS_UNI_ID)" +  
				") TYPE=" + AnyDAO.TBL_ENG + ";"
			);        
	}
		
	private static QueueProcessorThread thread;
	
	private UserUnitResponseDAO(){}
	
	public static void start(IDatabaseCtx ctx){
		if (thread != null){
			throw new IllegalStateException("Already started.");
		}
		
		thread = new QueueProcessorThread();
		thread.ctx = ctx;
		thread.stopped = false;  
		thread.start();
	}
	
	public static void newWorkAvailable(){
		if (thread != null){
			thread.newWorkAvailable();
		}
	}
	  
	public static void stop() {
		if (thread == null){
			throw new IllegalStateException("Not yet started.");
		}
		
		thread.stopped = true;
		try {  
			thread.interrupt();
			thread.join();
		} catch (Exception e){
			e.printStackTrace();
		} finally {  
			thread = null;
		}
	}
	
}
