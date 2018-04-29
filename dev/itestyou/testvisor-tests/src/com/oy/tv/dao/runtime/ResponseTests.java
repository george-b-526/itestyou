package com.oy.tv.dao.runtime;

import java.util.Date;
import java.util.GregorianCalendar;

import com.oy.tv.app.IDatabaseCtx.DatabaseCtx;
import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.UnitDAO.UnitType;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.model.learn.ProgressCalculator;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.schema.core.VariationBO;
import com.oy.tv.vocb.model.VocabViewModel;
import com.oy.tv.vocb.view.Result;
import com.oy.tv.vocb.view.VocabView;

public class ResponseTests extends DAOTestBase {

	private final static String ADMIN_NS = "ResponseTests_ITY_ADMIN";
	private final static String RUNTIME_NS = "ResponseTests_ITY_RUNTIME";
	
	public ResponseTests(){
		super(RUNTIME_NS, new AllDAO());
		clearAllData(ADMIN_NS, new com.oy.tv.dao.core.AllDAO());
	}
	
	public void testVocabModel(){
		VocabViewModel model = new VocabViewModel("qwerty");
		
		assertEquals(-1, model.getVariationId());

		model.wordIds = new int [] {1, 2, 3, 4, 5};

		model.answerIdx = 0;
		assertEquals(1, model.getVariationId());

		model.answerIdx = 2;
		assertEquals(3, model.getVariationId());

		model.answerIdx = 4;
		assertEquals(5, model.getVariationId());
		
		model.answerIdx = 99;
		assertEquals(-1, model.getVariationId());
	}
		
	public static synchronized void completAllQueuedWork(AnyDB db, 
			String fromNs, String toNs) throws Exception {
		QueueProcessorThread qpt = new QueueProcessorThread();
		DatabaseCtx ctx = new DatabaseCtx(db);
		while(true){
			qpt.doWork(ctx, fromNs, toNs);
			if (!qpt.hasMore()) break;
		}
	}
	
	public void testResponseSubmit() throws Exception {
		initActivity();
		rollUp();
		
		// last week of 2010 
		{
  		AnyResultSetContext result = db.execSelect(
      		"SELECT * FROM UUR_UNIT_ROLLUP_WEEKLY WHERE UUR_YEAR = 2010 AND UUR_WEEK = 52");
      result.rs().next();
  
  		assertEquals(2, result.rs().getInt("UUR_CORRECT_COUNT"));
  		assertEquals(2, result.rs().getInt("UUR_INCORRECT_COUNT"));
  		assertEquals(evening(2011, 1, 2).getTime(), 
  				result.rs().getTimestamp("UUR_LAST_RESPONSE_ON").getTime());
		}

		// first week of 2011
		{
  		AnyResultSetContext result = db.execSelect(
      		"SELECT * FROM UUR_UNIT_ROLLUP_WEEKLY WHERE UUR_YEAR = 2011 AND UUR_WEEK = 1");
      result.rs().next();
  
  		assertEquals(7, result.rs().getInt("UUR_CORRECT_COUNT"));
  		assertEquals(7, result.rs().getInt("UUR_INCORRECT_COUNT"));
  		assertEquals(evening(2011, 1, 9).getTime(), 
  				result.rs().getTimestamp("UUR_LAST_RESPONSE_ON").getTime());
		}

		// last week of 2011
		{
  		AnyResultSetContext result = db.execSelect(
      		"SELECT * FROM UUR_UNIT_ROLLUP_WEEKLY WHERE UUR_YEAR = 2011 AND UUR_WEEK = 52");
      result.rs().next();
  
  		assertEquals(5, result.rs().getInt("UUR_CORRECT_COUNT"));
  		assertEquals(5, result.rs().getInt("UUR_INCORRECT_COUNT"));
  		assertEquals(evening(2012, 1, 1).getTime(), 
  				result.rs().getTimestamp("UUR_LAST_RESPONSE_ON").getTime());
		}

		// first week of 2012
		{
  		AnyResultSetContext result = db.execSelect(
      		"SELECT * FROM UUR_UNIT_ROLLUP_WEEKLY WHERE UUR_YEAR = 2012 AND UUR_WEEK = 1");
      result.rs().next();
  
  		assertEquals(7, result.rs().getInt("UUR_CORRECT_COUNT"));
  		assertEquals(7, result.rs().getInt("UUR_INCORRECT_COUNT"));
  		assertEquals(evening(2012, 1, 8).getTime(), 
  				result.rs().getTimestamp("UUR_LAST_RESPONSE_ON").getTime());
		}
	}

	private void rollUp() throws Exception {
		completAllQueuedWork(db, ADMIN_NS, RUNTIME_NS);
				
		AnyResultSetContext result = db.execSelect(
				"SELECT * FROM CPT_CHECKPOINT");
		result.rs().next();
		
		assertEquals("com.oy.tv.dao.runtime.UserUnitResponseDAO.LAST_REQ_ID", 
				result.rs().getString("CPT_KEY"));
		
		assertEquals(1392, 
				result.rs().getInt("CPT_INTEGER_VALUE"));
	}
	
	private void initActivity() throws Exception {

		UserBO user = new UserBO();
		user.setId(1);
		
		UnitBO unit = new UnitBO();
		unit.setId(2);
		
		VariationBO var = new VariationBO();
		var.setId(3);
		  
		for (int month = 1; month <= 12; month++){
			int year = 2011;
			for (int day = 1; day < 30; day++){
				ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), var.getId(), null, morning(year, month, day), ResponseDAO.Score.FAIL,
						"www.cnn.com", "127.0.0.1", "IE", "12345", "qwerty", null, null);
				ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), var.getId(), new Date(), evening(year, month, day), ResponseDAO.Score.PASS,
						"www.cnn.com", "127.0.0.1", "IE", "12345", "qwerty", null, null);
			}			
		}
		
		for (int month = 1; month <= 12; month++){
			int year = 2012;
			for (int day = 1; day < 30; day++){
				ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), var.getId(), null, morning(year, month, day), ResponseDAO.Score.FAIL,
						"www.cnn.com", "127.0.0.1", "IE", "12345", "qwerty", null, null);
				ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), var.getId(), new Date(0), evening(year, month, day), ResponseDAO.Score.PASS,
						"www.cnn.com", "127.0.0.1", "IE", "12345", "qwerty", null, null);
			}			
		}

		AnyResultSetContext result = db.execSelect(
				"SELECT COUNT(*) AS COUNT FROM " + RUNTIME_NS + ".REQ_RESPONSE_QUEUE");
		result.rs().next();
		assertEquals(1392, result.rs().getInt("COUNT"));
	}

	private Date morning(int year, int month, int day){
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(year, month - 1, day);
		
		gc.set(GregorianCalendar.AM_PM, GregorianCalendar.AM); 
		gc.set(GregorianCalendar.HOUR, 9);
		gc.set(GregorianCalendar.MINUTE, 15);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);

		return gc.getTime();
	}

	private Date evening(int year, int month, int day){
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(year, month - 1, day);
		
		gc.set(GregorianCalendar.AM_PM, GregorianCalendar.PM);
		gc.set(GregorianCalendar.HOUR, 5);
		gc.set(GregorianCalendar.MINUTE, 45);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		
		return gc.getTime();
	}

	public void testVocbPlay() throws Exception {
		UserBO owner = new UserBO();
		owner.setId(777);
		
		UnitBO unit; {
  		db.execSelect("USE " + ADMIN_NS + ";");
  		unit = UnitDAO.addUnit(db, owner);
  		unit.setType(UnitType.VOCB.ordinal());
  		UnitDAO.updateUnit(db, unit, EObjectState.ACTIVE);
  		db.execUpdate("UPDATE UNI_UNIT SET UNI_TYPE = ? WHERE UNI_ID = ?", 
  				new Object [] {2, unit.getId()});
  		db.execSelect("USE " + RUNTIME_NS + ";");
		}

		VocabViewModel model = new VocabViewModel(null);
		model.wordIds = new int [] {11, 22, 33, 44, 55, 66};
		
		for (int i=0; i < 25 * model.wordIds.length; i++){
			int vote = i % model.wordIds.length;
			
			model.answerIdx = vote;
  		Result result = VocabView.getResult(model, "" + vote);
  		
			ResponseDAO.enqueue(  
				db, RUNTIME_NS, owner.getId(), unit.getId(), 888, new Date(model.getCreatedOn()), new Date(), result.getScore(), 
				model.getReferer(), "clientAddress", "clientAgent", "sessionId", model.getActivityId(),
				VocabView.createUnitData(model.getVariationId(), model.isInverted(), result), null);    
		}

		doWorkAndAssert(owner, unit, 
				"{11:50,22:50,33:50,44:50,55:50,66:50}",
				"{11,22,33,44,55,66}", 150, 0, 6); 

		for (int i=0; i < 25 * model.wordIds.length; i++){
			int vote = i % model.wordIds.length;

			model.answerIdx = vote;
  		Result result;
  		if (vote == 0){
    		result = VocabView.getResult(model, "" + (vote + 1));
  		} else {
  			result = VocabView.getResult(model, "" + (vote - 1));
  		}
  		
			ResponseDAO.enqueue(  
				db, RUNTIME_NS, owner.getId(), unit.getId(), 888, new Date(model.getCreatedOn()), new Date(), result.getScore(), 
				model.getReferer(), "clientAddress", "clientAgent", "sessionId", model.getActivityId(),
				VocabView.createUnitData(model.getVariationId(), model.isInverted(), result), null);    
		}

		doWorkAndAssert(owner, unit, 
				"{11:0,22:-25,33:0,44:0,55:0,66:25}",
				"{11,22,33,44,55,66}", 150, 150, 6);
	}
	
	public void testVocbPlayRanges() throws Exception {
		UserBO owner = new UserBO();
		owner.setId(777);
		
		UnitBO unit; {
  		db.execSelect("USE " + ADMIN_NS + ";");
  		unit = UnitDAO.addUnit(db, owner);
  		unit.setType(UnitType.VOCB.ordinal());
  		UnitDAO.updateUnit(db, unit, EObjectState.ACTIVE);
  		db.execUpdate("UPDATE UNI_UNIT SET UNI_TYPE = ? WHERE UNI_ID = ?", 
  				new Object [] {2, unit.getId()});
  		db.execSelect("USE " + RUNTIME_NS + ";");
		}
		
		VocabViewModel model = new VocabViewModel(null);
		model.wordIds = new int [] {11, 13, 14, 15, 16, 18};
		
		for (int i=0; i < 25 * model.wordIds.length; i++){
			int vote = i % model.wordIds.length;
			
			model.answerIdx = vote;
  		Result result = VocabView.getResult(model, "" + vote);
  		
			ResponseDAO.enqueue(  
				db, RUNTIME_NS, owner.getId(), unit.getId(), 888, new Date(model.getCreatedOn()), new Date(), result.getScore(), 
				model.getReferer(), "clientAddress", "clientAgent", "sessionId", model.getActivityId(),
				VocabView.createUnitData(model.getVariationId(), model.isInverted(), result), null);    
		}

		doWorkAndAssert(owner, unit, 
				"{11:50,13:50,14:50,15:50,16:50,18:50}",
				"{11,13|16,18}", 150, 0, 6); 

		for (int i=0; i < 25 * model.wordIds.length; i++){
			int vote = i % model.wordIds.length;

			model.answerIdx = vote;
  		Result result;
  		if (vote == 0){
    		result = VocabView.getResult(model, "" + (vote + 1));
  		} else {
  			result = VocabView.getResult(model, "" + (vote - 1));
  		}
  		
			ResponseDAO.enqueue(  
				db, RUNTIME_NS, owner.getId(), unit.getId(), 888, new Date(model.getCreatedOn()), new Date(), result.getScore(), 
				model.getReferer(), "clientAddress", "clientAgent", "sessionId", model.getActivityId(),
				VocabView.createUnitData(model.getVariationId(), model.isInverted(), result), null);    

		}

		doWorkAndAssert(owner, unit, 
				"{11:0,13:-25,14:0,15:0,16:0,18:25}",
				"{11,13|16,18}", 150, 150, 6);
	}
	
	private void doWorkAndAssert(UserBO owner, UnitBO unit, String unitData, 
			String summaryData, int pass, int fail, int completed) throws Exception {
		completAllQueuedWork(db, ADMIN_NS, RUNTIME_NS);
				
		{
  		AnyResultSetContext result = db.execSelect(
  				"SELECT UNCOMPRESS(UUR_UNIT_DATA) AS UNIT_DATA " + 
  				"FROM UUR_UNIT_ROLLUP_WEEKLY " + 
  				"WHERE UUR_USR_ID = ? AND UUR_UNI_ID = ?",
  				 new Object []{owner.getId(), unit.getId()});
  		result.rs().next();
  		assertEquals(unitData, result.rs().getString("UNIT_DATA"));	
		}

		{
  		AnyResultSetContext result = db.execSelect(
  				"SELECT *, UNCOMPRESS(UUS_UNIT_DATA) AS UNIT_DATA " + 
  				"FROM UUS_UNIT_ROLLUP_TOP " + 
  				"WHERE UUS_USR_ID = ? AND UUS_UNI_ID = ?",
  				 new Object []{owner.getId(), unit.getId()});
  		result.rs().next();
  		ProgressCalculator.RangeContainer rc = new ProgressCalculator.RangeContainer();
  		ProgressCalculator.parseUnitSummaryWithRanges(
  				result.rs().getString("UNIT_DATA"), rc);
  		assertEquals(summaryData, rc.render());
  		assertEquals(pass, result.rs().getInt("UUS_CORRECT_COUNT"));
  		assertEquals(fail, result.rs().getInt("UUS_INCORRECT_COUNT"));
  		assertEquals(completed, result.rs().getInt("UUS_DISTINCT_COUNT"));
		}
	}
	
	private void assertWY(int [] from, int [] to){
		assertEquals(from.length, to.length);
		for (int i=0; i< from.length; i++){
			assertEquals(from[i], to[i]);
		}
	}

	public void testLastWeek() throws Exception {
		Date now = new Date(Date.parse("2012/01/14"));
		int [] curr_week = QueueProcessorThread.getYearWeekOfNow(now);
		int [] last_week = QueueProcessorThread.getYearWeekOfNowMinusWeeks(now, 1);
		int [] last_last_week = QueueProcessorThread.getYearWeekOfNowMinusWeeks(now, 2);
		
		assertWY(new int [] {2012, 2}, curr_week);
		assertWY(new int [] {2012, 1}, last_week);
		assertWY(new int [] {2011, 52}, last_last_week);
	}
	
	public void testWeekYear() throws Exception {
		int [] nov_13 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2011/11/13")));
		int [] nov_14 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2011/11/14")));
		
		int [] dec_25 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2011/12/25")));
		int [] dec_26 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2011/12/26")));

		int [] jan_1 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2012/01/01")));
		int [] jan_2 = QueueProcessorThread.getYearWeekOfNowEx(new Date(Date.parse("2012/01/02")));
		
		assertWY(new int [] {2011, 45}, nov_13);

		assertWY(new int [] {2011, 46}, nov_14);

		assertWY(new int [] {2011, 51}, dec_25);

		assertWY(new int [] {2011, 52}, dec_26);

		assertWY(new int [] {2011, 52}, jan_1);

		assertWY(new int [] {2012, 1}, jan_2);		
	}
}
