package com.oy.tv.dao.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.VariationBO;

public class VariationDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(     
			"CREATE TABLE IF NOT EXISTS VAR_VARIATION (" +
			"	  VAR_ID INTEGER NOT NULL," +
			"	  VAR_UNIT_ID INTEGER NOT NULL," +
			"	  VAR_VALUES VARCHAR(255) NOT NULL," +
			"	  VAR_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (VAR_ID)," +  
			"	  		 INDEX idxUNIT (VAR_UNIT_ID)," +
			"	  		 INDEX idxSTATE (VAR_STATE)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);      
	}
	    
	public static void deleteVariation(AnyDB db, VariationBO var) throws SQLException {
		updateVariation(db, var, var.getValues(), EObjectState.DELETED);
	}
  	  
	public static void deleteAllVariations(AnyDB db, UnitBO unit) throws SQLException {
		db.execUpdate(
			"UPDATE VAR_VARIATION SET VAR_STATE = ? WHERE VAR_UNIT_ID = ?",
			new Object [] {EObjectState.DELETED. value(), unit.getId()}
		);
	}
 	
	public static VariationBO addVariation(AnyDB db, UnitBO unit, String values) throws SQLException {
		db.trxBegin();
		
		int next_id;
		{
			AnyResultSetContext result = db.execSelect("SELECT MAX(VAR_ID) + 1 FROM VAR_VARIATION");
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
			"INSERT INTO VAR_VARIATION (VAR_ID, VAR_UNIT_ID, VAR_VALUES, VAR_STATE) VALUES (?, ?, ?, ?)",
			new Object [] {next_id, unit.getId(), values, EObjectState.INACTIVE.value()}
		);   	
		
		VariationBO var = new VariationBO();
		var.setId(next_id);
		var.setUnitId(unit.getId());
		var.setValues(values);
		var.setState(EObjectState.INACTIVE);
 
		db.trxEnd();
		  
		return var;
	}
	
	public static VariationBO loadVariation(AnyDB db, int id) throws SQLException {
		return loadVariation(db, "" + id);
	}
	
	private static VariationBO fromRow(ResultSet rs) throws SQLException {
		VariationBO var = null;  
		if (rs.next()){
			var = new VariationBO();
			var.setId(rs.getInt("VAR_ID"));
			var.setUnitId(rs.getInt("VAR_UNIT_ID")); 
			var.setValues(rs.getString("VAR_VALUES"));
			var.setState(EObjectState.fromValue(rs.getInt("VAR_STATE")));
		}
		return var;
	}
 	
	public static VariationBO loadRndVariation(AnyDB db, UnitBO unit) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM VAR_VARIATION" + 
			" WHERE VAR_UNIT_ID = ? AND VAR_STATE != ?" + 
			" ORDER BY RAND()" + 
			" LIMIT 1",
			new Object [] {unit.getId(), EObjectState.DELETED.value()}
		); 
		return fromRow(result.rs());
	}
	
	public static VariationBO loadVariation(AnyDB db, String id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM VAR_VARIATION WHERE VAR_ID = ?",
			new Object [] {id}
		);
		return fromRow(result.rs());
	}
  	
	public static void updateVariation(AnyDB db, VariationBO var, String values, EObjectState state) throws SQLException {
		db.execUpdate(
			"UPDATE VAR_VARIATION SET VAR_VALUES = ?, VAR_STATE = ? WHERE VAR_ID = ?",
			new Object [] {values, state.value(), var.getId()}
		);
	  
		var.setValues(values);
		var.setState(state);
	}    
	
	public static int countVariationsFor(AnyDB db, UnitBO unit) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT COUNT(*) AS COUNT FROM VAR_VARIATION WHERE VAR_UNIT_ID = ? AND VAR_STATE != ?",
			new Object [] {unit.getId(), EObjectState.DELETED.value()}
		);
		result.rs().next();
		return result.rs().getInt("COUNT");
	}
	
	public static PagedKeyList getAllPaged(AnyDB db, UnitBO unit, int perPage, int currentPage) throws SQLException {
		PagedKeyList pkl;
		pkl = PagedKeyList.createFor(  
			db, "VAR_VARIATION", "VAR_ID", 
			"VAR_UNIT_ID = " + unit.getId() + " AND " + "VAR_STATE != " + EObjectState.DELETED.value(), 
			perPage, currentPage
		);
		return pkl;
	}
	
}
