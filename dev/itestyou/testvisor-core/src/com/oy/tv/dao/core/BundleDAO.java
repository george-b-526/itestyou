package com.oy.tv.dao.core;

import java.sql.SQLException;
import java.util.List;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.BundleBO;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UserBO;

public class BundleDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS BUN_BUNDLE (" +
			"	  BUN_ID INTEGER NOT NULL," +
			"	  BUN_OWNER_USR_ID INTEGER NOT NULL," +
			"	  BUN_UNIT_IDS VARCHAR(512) NOT NULL," +
			"	  BUN_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (BUN_ID)," +  
			"	  		 INDEX idxOWNER (BUN_OWNER_USR_ID)," +
			"	  		 INDEX idxSTATE (BUN_STATE)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}
	
	public static BundleBO addBundle(AnyDB db, UserBO owner) throws SQLException {
		db.trxBegin();
		
		int next_id;
		{
			AnyResultSetContext result = db.execSelect("SELECT MAX(BUN_ID) + 1 FROM BUN_BUNDLE;");
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
			"INSERT INTO BUN_BUNDLE (BUN_ID, BUN_OWNER_USR_ID, BUN_UNIT_IDS, BUN_STATE) VALUES (?, ?, ?, ?);",
			new Object [] {next_id, owner.getId(), "", EObjectState.INACTIVE.value()}
		);   	
		  
		BundleBO bundle = new BundleBO();
		bundle.setId(next_id);
		bundle.setOwnerId(owner.getId());
		bundle.setState(EObjectState.INACTIVE);
		
		db.trxEnd(); 
		
		return bundle;
	}
  	
	public static void updateBundle(AnyDB db, BundleBO bundle, List<Integer> unitIds, EObjectState state) throws SQLException {
		db.execUpdate(
			"UPDATE BUN_BUNDLE SET BUN_UNIT_IDS = ?, BUN_STATE = ? WHERE BUN_ID = ?",
			new Object [] {AnyDAO.intArray2String(unitIds), state.value(), bundle.getId()}
		);  
	    
		bundle.getUnitIds().clear();
		bundle.getUnitIds().addAll(unitIds);
		bundle.setState(state);
	}
	
	public static BundleBO loadBundle(AnyDB db, int id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM BUN_BUNDLE WHERE BUN_ID = ?",
			new Object [] {id}
		);
		  
		BundleBO bundle = null;  
		if (result.rs().next()){
			bundle = new BundleBO();
			bundle.setId(result.rs().getInt("BUN_ID"));
			bundle.setOwnerId(result.rs().getInt("BUN_OWNER_USR_ID"));
			bundle.getUnitIds().addAll(AnyDAO.string2IntArray(result.rs().getString("BUN_UNIT_IDS")));
			bundle.setState(EObjectState.fromValue(result.rs().getInt("BUN_STATE")));
		}    
		return bundle;
	}
	
}
