package com.oy.tv.dao.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.schema.core.VariationBO;
import com.oy.tv.util.SmartCache;
import com.oy.tv.util.SmartCacheMonitor;

public class UnitDAO {
	
	public enum UnitType {NONE, WDGT, VOCB} 
	
	private static SmartCacheMonitor monitor = new SmartCacheMonitor(
		UnitDAO.class, "UNITS", 
		"This monitor reports Units cache."
	); 
	
	private static SmartCache<String, List<UnitBO>> cache = 
		new SmartCache<String, List<UnitBO>> (5 * 60 * 1000, 1000, monitor);

	private static Object lock = new Object();
	
	public static boolean CAN_CACHE = false;

	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS UNI_UNIT (" +
			"	  UNI_ID INTEGER NOT NULL AUTO_INCREMENT," +
			"	  UNI_OWNER_USR_ID INTEGER NOT NULL," +
			"	  UNI_XML MEDIUMBLOB NOT NULL," +
			"	  UNI_GRADE TINYINT NOT NULL," +
			"	  UNI_TITLE VARCHAR(255)," +
			"	  UNI_DESC VARCHAR(2048)," +
			"	  UNI_NOTES VARCHAR(255) NOT NULL," +
			"	  UNI_PROPERTIES MEDIUMBLOB," +
			"	  UNI_TYPE INTEGER," +
			"	  UNI_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (UNI_ID)," +  
			"	  		 INDEX idxOWNER (UNI_OWNER_USR_ID)," +
			"	  		 INDEX idxGRADE (UNI_GRADE)," +
			"	  		 INDEX idxSTATE (UNI_STATE)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		); 
	} 
	  
	public static void deleteUnit(AnyDB db, UnitBO unit) throws SQLException {
		VariationDAO.deleteAllVariations(db, unit);
		updateUnit(db, unit, unit.getXml(), unit.getNotes(), unit.getGrade(), EObjectState.DELETED);
	}
  	
	private static UnitBO fromRow(ResultSet rs) throws SQLException {
		UnitBO unit = null;  
		if (rs.next()){
			unit = new UnitBO();
			unit.setId(rs.getInt("UNI_ID"));
			unit.setOwnerId(rs.getInt("UNI_OWNER_USR_ID"));
			unit.setGrade(rs.getInt("UNI_GRADE"));
			unit.setXml(rs.getString("XML"));
			unit.setDesc(rs.getString("UNI_DESC"));
			unit.setTitle(rs.getString("UNI_TITLE"));
			unit.setNotes(rs.getString("UNI_NOTES"));
			unit.setType(rs.getInt("UNI_TYPE"));
			unit.setState(EObjectState.fromValue(rs.getInt("UNI_STATE")));
		}
		return unit;
	}
	
	public static List<UnitBO> loadRndUnit(AnyDB db, int gradeId) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT *, CONVERT(UNCOMPRESS(UNI_XML) USING 'utf8') AS XML FROM UNI_UNIT" + 
			" WHERE UNI_GRADE = ? AND UNI_STATE = ?"  + 
			" ORDER BY RAND()" +
			" LIMIT 50",
			new Object [] {gradeId, EObjectState.ACTIVE.value()}
		);
		
		List<UnitBO> units = new ArrayList<UnitBO>();
		while(true){
			UnitBO unit = fromRow(result.rs()); 
			if (unit == null){
				break;
			}
			units.add(unit);
		}
		return units;
	}

	public static List<UnitBO> loadUnitsOldestLast(AnyDB db, int gradeId, String locale) throws SQLException {
		if (!TranslationDAO.isSuppotedLocale(locale)) {
			throw new RuntimeException("Unknown locale: " + locale);
		}
		if (TranslationDAO.LANG_ANY.equals(locale)) {
			throw new RuntimeException("Must use concrete locale, not LANG_ANY.");
		}
		
		// create key
		final String key = "/units/" + locale + "/" + gradeId;

		// get where for joining translations
		String whereEx;
		if (TranslationDAO.LANG_EN.equals(locale)) {
			whereEx = "";
		} else {
			whereEx = " AND (TLN_LANG = \"" + locale + "\" OR TLN_LANG = \"*\")";
		}
		
		// get
		if (CAN_CACHE){
  		synchronized(lock){
  			List<UnitBO> result = cache.get(key);
  			if (result != null){
  				return result;
  			}
  		}
		}
		
	  // load
		AnyResultSetContext result = db.execSelect(
			"SELECT *, CONVERT(UNCOMPRESS(UNI_XML) USING 'utf8') AS XML FROM UNI_UNIT" +
			" LEFT JOIN TLN_TRANSLATION ON TLN_KEY = CONCAT(\"/unit/math/\", UNI_ID)" + 
			" WHERE UNI_GRADE = ? AND UNI_STATE = ? " + whereEx +
			" ORDER BY UNI_ID DESC" +
			" LIMIT 1000",
			new Object [] {gradeId, EObjectState.ACTIVE.value()}
		);
		List<UnitBO> units = new ArrayList<UnitBO>();
		while(true){
			UnitBO unit = fromRow(result.rs()); 
			if (unit == null){
				break;
			}
			units.add(unit);
		}
  	
		// put
		if (CAN_CACHE){
  		synchronized(lock){
  			cache.put(key, units);
  		}
		}
		
  	return units;
	}
	
	public static UnitBO loadUnit(AnyDB db, int id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT *, CONVERT(UNCOMPRESS(UNI_XML) USING 'utf8') AS XML FROM UNI_UNIT WHERE UNI_ID = ?",
			new Object [] {id}
		);	
		return fromRow(result.rs());
	}
	
	public static UnitBO addUnit(AnyDB db, UserBO owner) throws SQLException {
		db.execUpdate(
			"INSERT INTO UNI_UNIT (UNI_OWNER_USR_ID, UNI_XML, UNI_NOTES, UNI_GRADE, UNI_STATE) " + 
			"VALUES (?, COMPRESS(''), '', 0, ?)",
			new Object [] {owner.getId(), EObjectState.INACTIVE.value()}
		);   	

		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
		result.rs().next();
		
		UnitBO unit = new UnitBO();
		
		unit.setId(result.rs().getInt("ID"));
		unit.setOwnerId(owner.getId());
		unit.setXml("");
		unit.setNotes("");
		unit.setGrade(0); 
		unit.setState(EObjectState.INACTIVE);

		return unit;
	}

	public static void updateUnit(AnyDB db, UnitBO unit, EObjectState state) throws SQLException {
		updateUnit(db, unit, unit.getXml(), unit.getNotes(), unit.getGrade(), state);
	}

	public static void updateUnit(AnyDB db, UnitBO unit, String xml, String notes, int grade, EObjectState state) throws SQLException {	
			updateUnit(db, unit, xml, notes, grade, UnitType.WDGT.ordinal(), state);
	}

	public static void updateUnit(AnyDB db, UnitBO unit, String xml, String notes, int grade, int type, EObjectState state) throws SQLException {	
		updateUnit(db, unit, xml, notes, null, null, grade, type, state);
	}
	
	public static void updateUnit(AnyDB db, UnitBO unit, String xml, String notes, String title, String desc, int grade, int type, EObjectState state) throws SQLException {	
		db.execUpdate(
			"UPDATE UNI_UNIT SET UNI_XML = COMPRESS(?), UNI_NOTES = ?, UNI_TITLE = ?, UNI_DESC = ?, UNI_GRADE = ?, UNI_STATE = ?, UNI_TYPE = ? WHERE UNI_ID = ?",
			new Object [] {xml, notes, title, desc, grade, state.value(), type, unit.getId()}
		);

		unit.setGrade(grade);
		unit.setXml(xml);
		unit.setTitle(title);
		unit.setDesc(desc);
		unit.setNotes(notes);
		unit.setState(state);
		unit.setType(type);
	}
		
	public static PagedKeyList getAllPaged(AnyDB db, UserBO user, int perPage, int currentPage, EObjectState state) throws SQLException {
		return getAllPagedAny(db, user, perPage, currentPage, "UNI_STATE = " + state.value());
	}
	
	public static PagedKeyList getAllPaged(AnyDB db, UserBO user, int perPage, int currentPage) throws SQLException {
		return getAllPagedAny(db, user, perPage, currentPage, "UNI_STATE != " + EObjectState.DELETED.value());
	}
	
	private static PagedKeyList getAllPagedAny(AnyDB db, UserBO user, int perPage, int currentPage, String state_where) throws SQLException {
		PagedKeyList pkl;
		try {
			String user_where = "";
			if (user != null){
				user_where = "UNI_OWNER_USR_ID = " + user.getId() + " AND ";
			}
			
			pkl = PagedKeyList.createFor(
				db, 
				"UNI_UNIT", "UNI_ID", 
				user_where + state_where, 
				perPage, currentPage
			);  
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		return pkl;
	}

	public static UnitBO deepCopy(AnyDB from, AnyDB to, UserBO owner, UnitBO unit) throws SQLException {
		UnitBO oUnit = UnitDAO.loadUnit(from, unit.getId());
		PagedKeyList oVars = VariationDAO.getAllPaged(from, unit, Integer.MAX_VALUE, 0);
		
		UnitBO nUnit = UnitDAO.addUnit(to, owner);
		
		String title = "Math Worksheets Unit #" + nUnit.getId();
		String desc = oUnit.getDesc();
		if (desc == null){
			desc = "";
		}
		
		UnitDAO.updateUnit(to, nUnit, 
				oUnit.getXml(), oUnit.getNotes(), title, desc, 
				oUnit.getGrade(), oUnit.getType(), EObjectState.ACTIVE);

		for (int id : oVars.ids){
			VariationBO oVar = VariationDAO.loadVariation(from, id);
			VariationDAO.addVariation(to, nUnit, oVar.getValues());
		}
		
		return nUnit;
	}
	
}
