package com.oy.tv.dao.core;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.BundleBO;
import com.oy.tv.schema.core.ETestState;
import com.oy.tv.schema.core.TestBO;
import com.oy.tv.schema.core.UserBO;

public class TestDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS TST_TEST (" +
			"	  TST_ID INTEGER NOT NULL," +
			"	  TST_BUNDLE_ID INTEGER NOT NULL," +
			"	  TST_OWNER_USR_ID INTEGER NOT NULL," +
			  
			"	  TST_STARTED_ON TIMESTAMP," +
			"	  TST_UPDATED_ON TIMESTAMP," +
			
			"	  TST_VARIATION_IDS VARCHAR(512)," +
			"	  TST_ANSWER_INDEXES VARCHAR(512)," +
			"	  TST_SCORE_SHEET VARCHAR(512)," +
			
			"	  TST_ATTEMPTED INTEGER," +
			"	  TST_COMPLETED INTEGER," +
			"	  TST_CORRECT INTEGER," +
			"	  TST_INCORRECT INTEGER," +
			
			"	  TST_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (TST_ID)," +  
			"	  		 INDEX idxBUNDLE (TST_BUNDLE_ID)," +
			"	  		 INDEX idxOWNER (TST_OWNER_USR_ID)," +
			"	  		 INDEX idxSTATE (TST_STATE)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}
	
	public static TestBO addTest(AnyDB db, BundleBO bundle, UserBO owner) throws SQLException {
		db.trxBegin();
		
		int next_id;
		{
			AnyResultSetContext result = db.execSelect("SELECT MAX(TST_ID) + 1 FROM TST_TEST;");
			try {
				result.rs().next();
				next_id = result.rs().getInt(1);
				
				if (next_id == 0){
					next_id = 1;
				}
			} finally {  
				result.close();
			}  
		}
 		
		db.execUpdate(
			"INSERT INTO TST_TEST (TST_ID, TST_OWNER_USR_ID, TST_BUNDLE_ID, TST_STATE) VALUES (?, ?, ?, ?);",
			new Object [] {next_id, owner.getId(), bundle.getId(), ETestState.CREATED.value()}
		);   	
 		  
		TestBO test = new TestBO();
		test.setId(next_id);
		test.setBundleId(bundle.getId());
		test.setOwnerId(owner.getId());
		test.setState(ETestState.CREATED);
		
		db.trxEnd();
		
		return test;
	}
  	
	public static void updateTest(
		AnyDB db, TestBO test, 
		Date startedOn,
		List<Integer> variationIds,
		ETestState state
	) throws SQLException {
		  
		db.execUpdate(
			"UPDATE TST_TEST SET TST_STARTED_ON = ?, TST_UPDATED_ON = ?, TST_VARIATION_IDS = ?, TST_STATE = ? WHERE TST_ID = ?",
			new Object [] {
				new java.sql.Timestamp(startedOn.getTime()),
				new java.sql.Timestamp(startedOn.getTime()),
				AnyDAO.intArray2String(variationIds), 
				state.value(), test.getId()
			}  
		);  
	    
		test.setStartedOn(Calendar.getInstance());
		test.getStartedOn().setTime(startedOn);

		test.setUpdatedOn(Calendar.getInstance());
		test.getUpdatedOn().setTime(startedOn);
		
		test.getVariationIds().clear();
		test.getVariationIds().addAll(variationIds);
		
		test.setState(state);
	}
	
	public static void updateTest(
			AnyDB db, TestBO test, 
			Date updatedOn, 
			List<Integer> answerIndexes, List<Boolean> scoreSheet,
			int attempted, int completed, int correct, int incorrect,
			ETestState state
		) throws SQLException {
			  
			db.execUpdate(
				"UPDATE TST_TEST SET TST_UPDATED_ON = ?, TST_ANSWER_INDEXES = ?, TST_SCORE_SHEET = ?, TST_ATTEMPTED = ?, TST_COMPLETED = ?, TST_CORRECT = ?, TST_INCORRECT = ?, TST_STATE = ? WHERE TST_ID = ?",
				new Object [] {  
					new java.sql.Timestamp(updatedOn.getTime()),
					AnyDAO.intArray2String(answerIndexes), AnyDAO.boolArray2String(scoreSheet),
					attempted, completed, correct, incorrect,
					state.value(), test.getId()
				}
			);  
			
			test.setUpdatedOn(Calendar.getInstance());
			test.getUpdatedOn().setTime(updatedOn);
		    
			test.getAnswerIndexes().clear();
			test.getAnswerIndexes().addAll(answerIndexes);
			
			test.getScoreSheet().clear();
			test.getScoreSheet().addAll(scoreSheet);
			
			test.setAttempted(attempted);
			test.setCompleted(completed);
			test.setCorrect(correct);
			test.setIncorrect(incorrect);
			
			test.setState(state);
		}	
	
	public static TestBO loadTest(AnyDB db, int id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM TST_TEST WHERE TST_ID = ?",
			new Object [] {id}
		);
		  
		TestBO test = null;  
		if (result.rs().next()){
			test = new TestBO();
			
			test.setId(result.rs().getInt("TST_ID"));
			test.setBundleId(result.rs().getInt("TST_BUNDLE_ID"));
			test.setOwnerId(result.rs().getInt("TST_OWNER_USR_ID"));
			
			test.setStartedOn(Calendar.getInstance());
			test.getStartedOn().setTime(result.rs().getTimestamp("TST_STARTED_ON"));

			test.setUpdatedOn(Calendar.getInstance());
			test.getUpdatedOn().setTime(result.rs().getTimestamp("TST_UPDATED_ON"));
			
			test.getVariationIds().clear();
			test.getVariationIds().addAll(
				AnyDAO.string2IntArray(result.rs().getString("TST_VARIATION_IDS"))
			);
			
			test.getAnswerIndexes().clear();
			test.getAnswerIndexes().addAll(
				AnyDAO.string2IntArray(result.rs().getString("TST_ANSWER_INDEXES"))
			);
			
			test.getScoreSheet().clear();
			test.getScoreSheet().addAll(
				AnyDAO.string2BoolArray(result.rs().getString("TST_SCORE_SHEET"))
			);
			
			test.setAttempted(result.rs().getInt("TST_ATTEMPTED"));
			test.setCompleted(result.rs().getInt("TST_COMPLETED"));
			test.setCorrect(result.rs().getInt("TST_CORRECT"));
			test.setIncorrect(result.rs().getInt("TST_INCORRECT"));
					  
			test.setState(ETestState.fromValue(result.rs().getInt("TST_STATE")));
		}    
		return test;
	}
	
}
