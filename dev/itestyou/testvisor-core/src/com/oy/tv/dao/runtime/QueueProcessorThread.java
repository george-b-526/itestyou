package com.oy.tv.dao.runtime;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;
import com.oy.tv.app.IDatabaseCtx;
import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.dao.core.UnitDAO.UnitType;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.model.learn.ProgressCalculator;
import com.oy.tv.model.learn.ProgressCalculator.RangeContainer;
import com.oy.tv.model.learn.ProgressCalculator.Summary;

public class QueueProcessorThread extends Thread {

	private static TaskExecutionMonitor monitor = new TaskExecutionMonitor(
			QueueProcessorThread.class, "VOTES", 
			"This monitor reports queue procesor task execution."
		); 
	
	static final int MAX_BATCH_SIZE = 100;  
	static final int IDLE_TIMEOUT = 30 * 1000;
	static final String KEY_NAME_LAST_REQ_ID = 
			UserUnitResponseDAO.class.getName() + ".LAST_REQ_ID";  
	
	private Object lock = new Object();
	
	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
	boolean stopped;
	boolean more;
	IDatabaseCtx ctx;
	
	public boolean hasMore(){
		return more;
	}
	
	public void run(){
		while(true) {
			if (stopped) {
				break;
			}
			
			try {
				try {
					doWork(ctx, AllDAO.NS_DEFAULT, com.oy.tv.dao.runtime.AllDAO.NS_DEFAULT);
				} catch (Exception e){
					e.printStackTrace();
				}
				    
				if (!more){
					synchronized(lock){
						lock.wait(IDLE_TIMEOUT);
					}
				}  
			} catch (Exception e){
				e.printStackTrace();
				break;
			}
		}
	}
	   
	void newWorkAvailable(){
		more = true;
		
		synchronized(lock){
			lock.notify();
		}  
	}
	
	public void doWork(IDatabaseCtx ctx, String adminNs, String runtimeNs) throws Exception {
		ctx.beginDb();
		try {
			ctx.getDb().execSelect("USE " + runtimeNs + ";");
			
			more = false;

			int lrid = getLastReqId(ctx);
			boolean moreChunks = completeChunk(ctx, adminNs, lrid);

			more = more || moreChunks;
		} finally {
			ctx.endDb();
		}
	}
	  
	private int getLastReqId(IDatabaseCtx ctx) throws Exception {
		AnyDB db = ctx.getDb();		
		  
		AnyResultSetContext rs = db.execSelect( 
			"SELECT * FROM CPT_CHECKPOINT WHERE CPT_KEY = ?",
			new Object [] {KEY_NAME_LAST_REQ_ID}    
		);
		
		int REQ_ID = -1;
		if (rs.rs().next()){
			REQ_ID = rs.rs().getInt("CPT_INTEGER_VALUE");
		} else {
			db.execUpdate(
				"INSERT INTO CPT_CHECKPOINT (CPT_KEY, CPT_INTEGER_VALUE) VALUES(?, ?)",
				new Object [] {KEY_NAME_LAST_REQ_ID, REQ_ID}
			);
		}
		  
		return REQ_ID;
	}
	  
	private boolean completeChunk(IDatabaseCtx ctx, String adminNs, int lrid) throws Exception {
		AnyDB db = ctx.getDb();		

		int completed = 0;
		int max_req_id = lrid;
		
		db.execSelect("SET AUTOCOMMIT = 0;");
		db.execSelect("START TRANSACTION;");
		try {
  		AnyResultSetContext rs = db.execSelect(
  			"SELECT A.*, UNCOMPRESS(A.REQ_UNIT_DATA) AS UNIT_DATA, " + 
  			"B.UNI_TYPE FROM REQ_RESPONSE_QUEUE AS A " + 
  			"LEFT JOIN " + adminNs + ".UNI_UNIT AS B ON A.REQ_UNI_ID = B.UNI_ID " + 
  			"WHERE REQ_ID > ? LIMIT " + MAX_BATCH_SIZE,
  			new Object [] {lrid}
  		);

  		// process
  		try {
  			while(rs.rs().next()){
  				max_req_id = rs.rs().getInt("REQ_ID");
  				
  				monitor.incStarted();
  				try {
  					handleRow(ctx, rs);
  					monitor.incCompleted();
  				} catch (Exception e){
  					monitor.incFailed();
  					throw e;  					
  				}
  
  				completed++;   
  			}
  		} finally {
  			rs.close();  
  		}
  			 
  		// update checkpoint
  		if (max_req_id != lrid){
  			int rows = db.execUpdate(
  				"UPDATE CPT_CHECKPOINT SET " + 
  				"CPT_INTEGER_VALUE = ? " + 
  				"WHERE CPT_KEY = ? AND CPT_INTEGER_VALUE = ?",
  				new Object [] {max_req_id, KEY_NAME_LAST_REQ_ID, lrid}  
  			); 
  			  
  			if (rows == 0){  
  				throw new RuntimeException(
  						"Checkpoint updated by another thread: " + KEY_NAME_LAST_REQ_ID);
  			}
  		}
  		db.execSelect("COMMIT;");
		} catch (Exception e) {
			db.execSelect("ROLLBACK;");
			throw e;
		} finally {
			db.execSelect("SET AUTOCOMMIT = 1;");
		}
		  
		return completed == MAX_BATCH_SIZE;  
	}
	
	public static int [] getYearWeekOfNowEx(Date date){
		DateTime dt = new DateTime(date);
		return new int [] {dt.getWeekyear(), dt.getWeekOfWeekyear()};
	}
	
	public static int [] getYearWeekOfNow(Date date){
		return getYearWeekOfNowEx(date);
	}
	
	public static int [] getYearWeekOfNowMinusWeeks(Date date, int weeks){
		DateTime dt = new DateTime(date);
		return getYearWeekOfNowEx(dt.minusWeeks(weeks).toDate());
	}

	public static AnyResultSetContext getWeekSummaryRow(AnyDB db, int usr_id, int uni_id, 
			int year, int week) throws SQLException {
		return getWeekSummaryRow(db, usr_id, uni_id, year, week, false);
	}

	public static RangeContainer getRangeContainerFor(AnyDB db, int usr_id, int uni_id) 
			throws SQLException {
		AnyResultSetContext rs = db.execSelect(
				"SELECT *, UNCOMPRESS(UUS_UNIT_DATA) AS UNIT_DATA " + 
				"FROM ITY_RUNTIME.UUS_UNIT_ROLLUP_TOP " + 
				"WHERE UUS_USR_ID = ? AND UUS_UNI_ID = ? ",
				new Object [] {usr_id, uni_id}
			);
		
		if (rs.rs().next()) {
			RangeContainer rc = new RangeContainer();
			String data = rs.rs().getString("UNIT_DATA");
			ProgressCalculator.parseUnitSummaryWithRanges(data, rc);
			return rc;
		}
		return null;
	}
	
	public static AnyResultSetContext getMostRecentRowsFor(AnyDB db, String ns, int usr_id, int uni_id, 
			int limit) throws SQLException {
		if (ns != null){
			ns = ns + ".";
		} else {
			ns = "";
		}
		return db.execSelect(
				"SELECT *, UNCOMPRESS(UUR_UNIT_DATA) AS UNIT_DATA " + 
				"FROM " + ns + "UUR_UNIT_ROLLUP_WEEKLY " + 
				"WHERE UUR_USR_ID = ? AND UUR_UNI_ID = ? " + 
				"ORDER BY UUR_YEAR DESC, UUR_WEEK DESC " + 
				"LIMIT " + limit,
				new Object [] {usr_id, uni_id}
			);
	}
	
	private static AnyResultSetContext getWeekSummaryRow(AnyDB db, int usr_id, int uni_id, int year, 
			int week, boolean forUpdate) throws SQLException {
		String mode = "";
		if (forUpdate){
			mode = " FOR UPDATE";
		}
		return db.execSelect(
				"SELECT *, UNCOMPRESS(UUR_UNIT_DATA) AS UNIT_DATA " + 
				"FROM UUR_UNIT_ROLLUP_WEEKLY " + 
				"WHERE UUR_USR_ID = ? AND UUR_UNI_ID = ? AND UUR_YEAR= ? AND UUR_WEEK = ?" + mode,
				new Object [] {usr_id, uni_id, year, week}
			);
	}

	private void handleRow(IDatabaseCtx ctx, AnyResultSetContext rs) throws Exception {
		int usr_id = rs.rs().getInt("REQ_USR_ID");
		int uni_id = rs.rs().getInt("REQ_UNI_ID");
		int passed = rs.rs().getInt("REQ_PASSED");
		Timestamp ts = rs.rs().getTimestamp("REQ_RECEIVED_ON");
		
		Map<String, ProgressCalculator.Verb> verbs = null;
		Map<Integer, Integer> delta = null; {
			boolean anonymous = usr_id == 0;
			UnitType uni_type = UnitType.values()[rs.rs().getInt("UNI_TYPE")];
  		if (uni_type == UnitType.VOCB && !anonymous){
  			String data = rs.rs().getString("UNIT_DATA");
  			verbs = new HashMap<String, ProgressCalculator.Verb>();
  			delta = ProgressCalculator.parseUnitDeltaData(data, verbs);
  		}
		}

		updateWeekSummary(ctx.getDb(), usr_id, uni_id, passed, ts, delta);
		updateAllTimeSummary(ctx.getDb(), usr_id, uni_id, passed, verbs, ts);
	}
	
	public static void updateWeekSummary(AnyDB db, int usr_id, int uni_id, int passed, 
			Date date, Map<Integer, Integer> delta) throws Exception {	
		int [] year_week = getYearWeekOfNow(date);
		int year = year_week[0];
		int week = year_week[1];
					
		int c_delta = passed == 1 ? 1 : 0;
		int i_delta = passed == 0 ? 1 : 0;
		int s_delta = passed == 2 ? 1 : 0;
		
		AnyResultSetContext rsx = getWeekSummaryRow(db, usr_id, uni_id, year, week, true);
		try {
			if (!rsx.rs().next()){
				db.execUpdate(
					"INSERT INTO UUR_UNIT_ROLLUP_WEEKLY " + 
					"(UUR_USR_ID, UUR_UNI_ID, UUR_CORRECT_COUNT, UUR_INCORRECT_COUNT, UUR_SKIPPED_COUNT, " + 
					"UUR_UNIT_DATA, UUR_LAST_RESPONSE_ON, UUR_YEAR, UUR_WEEK) " + 
					"VALUES (?, ?, ?, ?, ?, COMPRESS(?), ?, ?, ?)",
					new Object [] {usr_id, uni_id, c_delta, i_delta, s_delta, 
							ProgressCalculator.mergeUnitData((String) null, delta), date, year, week}
				);	
			} else {
				int c_count = rsx.rs().getInt("UUR_CORRECT_COUNT") + c_delta;
				int i_count = rsx.rs().getInt("UUR_INCORRECT_COUNT") + i_delta;
				int s_count = rsx.rs().getInt("UUR_SKIPPED_COUNT") + s_delta;
				
				db.execUpdate(
					"UPDATE UUR_UNIT_ROLLUP_WEEKLY SET " + 
					"UUR_CORRECT_COUNT = ?, UUR_INCORRECT_COUNT = ?, UUR_SKIPPED_COUNT = ?, " + 
					"UUR_UNIT_DATA = COMPRESS(?), UUR_LAST_RESPONSE_ON = ? " + 
					"WHERE UUR_USR_ID = ? AND UUR_UNI_ID = ? AND UUR_YEAR = ? AND UUR_WEEK = ? ",
					new Object [] {c_count, i_count, s_count, 
							ProgressCalculator.mergeUnitData(rsx.rs().getString("UNIT_DATA"), delta), date, 
							usr_id, uni_id, year, week}
				);
			}     
		} finally {
			rsx.close();
		}
	}
	
	public static void updateAllTimeSummary(AnyDB db, int usr_id, int uni_id, int passed, 
			Map<String, ProgressCalculator.Verb> verbs, Date date) throws Exception {	
		AnyResultSetContext rs = db.execSelect(
			"SELECT *, UNCOMPRESS(UUS_UNIT_DATA) AS UNIT_DATA " +
			"FROM UUS_UNIT_ROLLUP_TOP " +
			"WHERE UUS_USR_ID = ? AND UUS_UNI_ID = ?",
			new Object [] {usr_id, uni_id}
		);		
		try {
			int c_delta = passed == 1 ? 1 : 0;
			int i_delta = passed == 0 ? 1 : 0;
			int s_delta = passed == 2 ? 1 : 0;
	
			int c_count = c_delta;
			int i_count = i_delta;
			int s_count = s_delta;
			int c_distinct = -1;
			String data = null;
			if (rs.rs().next()){
				c_count = rs.rs().getInt("UUS_CORRECT_COUNT") + c_delta;
				i_count = rs.rs().getInt("UUS_INCORRECT_COUNT") + i_delta;
				s_count = rs.rs().getInt("UUS_SKIPPED_COUNT") + s_delta;
				data = rs.rs().getString("UNIT_DATA");
			}
			
			if (verbs != null) {
				Summary summary = ProgressCalculator.mergeUnitSummary(data, verbs);
				data = summary.getData();
				c_distinct = summary.getCompleted();
			}
			
			db.execUpdate("REPLACE INTO UUS_UNIT_ROLLUP_TOP SET " + 
					"UUS_USR_ID = ?, UUS_UNI_ID = ?, UUS_CORRECT_COUNT = ?, UUS_INCORRECT_COUNT = ?, " + 
					"UUS_SKIPPED_COUNT = ?, UUS_DISTINCT_COUNT = ?, UUS_UNIT_DATA = COMPRESS(?), " + 
					"UUS_LAST_RESPONSE_ON = ?",
					new Object [] {usr_id, uni_id, c_count, i_count, 
					s_count, c_distinct, data, date});
		} finally {
			rs.close();
		}
	}
}
