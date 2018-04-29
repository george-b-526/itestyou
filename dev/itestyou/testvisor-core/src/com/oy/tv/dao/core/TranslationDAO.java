package com.oy.tv.dao.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;

public class TranslationDAO {

  public static String LANG_ANY = "*";
  public static String LANG_EN = "en";
  public static String LANG_RU = "ru";
  public static String LANG_ZH = "zh";
  public static String LANG_NL = "nl";
  public static String LANG_ES = "es";

  private static Set<String> supportedLocales = new HashSet<String>();
  static {
  	supportedLocales.add(LANG_ANY);
  	supportedLocales.add(LANG_EN);
  	supportedLocales.add(LANG_RU);
  	supportedLocales.add(LANG_ZH);
  	supportedLocales.add(LANG_NL);
  	supportedLocales.add(LANG_ES);
  }
  
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
				"CREATE TABLE IF NOT EXISTS TLN_TRANSLATION (" +
				"	  TLN_ID INTEGER NOT NULL AUTO_INCREMENT," +
				"	  TLN_LANG VARCHAR(16)," +
				"	  TLN_KEY VARCHAR(255)," +
				"	  TLN_DATA MEDIUMBLOB," +
				"	  TLN_UPDATED_ON TIMESTAMP," +
				"	  TLN_STATE TINYINT NOT NULL," +
				"	  UNIQUE INDEX idxID(TLN_ID)," +
				"	  UNIQUE INDEX idxKEYLANG(TLN_KEY, TLN_LANG)" +
				") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}
	
	public static String [] getSupportedlocales() {
		return supportedLocales.toArray(new String [] {});
	}
	
	public static boolean isSuppotedLocale(String locale) {
		return supportedLocales.contains(locale);
	}

	private static String key(int unitId) {
		return "/unit/math/" + unitId;
	}
	
	private static String key(UnitBO unit) {
		return key(unit.getId());
	}
	
	public static String formatLocale(String locale) {
		if (supportedLocales.contains(locale)) {
			return locale;
		}
		return "en";
	}
	
	public static TranslationBO updateState(AnyDB db, String lang, UnitBO unit, EObjectState state) throws SQLException {
		return updateState(db, lang, key(unit), state);
	}
	
	public static TranslationBO updateState(AnyDB db, String lang, String key, EObjectState state) throws SQLException {
		db.execUpdate(
				"UPDATE TLN_TRANSLATION SET TLN_STATE = ? WHERE TLN_LANG = ? AND TLN_KEY = ?",
				new Object [] {state.value(), lang, key}
			);		
		return get(db, lang, key);
	}

	public static void markTranslatable(AnyDB db, UnitBO unit) throws SQLException {
		delete(db, LANG_ANY, unit);
	}
	
	public static void markNonTranslatable(AnyDB db, UnitBO unit) throws SQLException {
		put(db, LANG_ANY, unit, null);
		updateState(db, LANG_ANY, unit, EObjectState.ACTIVE);
	}

	public static void delete(AnyDB db, String lang, UnitBO unit) throws SQLException {
		db.execUpdate(
			"DELETE FROM TLN_TRANSLATION WHERE TLN_KEY = ? AND TLN_LANG = ?",
			new Object [] {key(unit), lang}
		);
	}
	
	public static TranslationBO put(AnyDB db, String lang, String key, String data) throws SQLException {
		Date now = new Date();
		
		db.execUpdate(
			"REPLACE INTO TLN_TRANSLATION (TLN_LANG, TLN_KEY, TLN_DATA, TLN_UPDATED_ON, TLN_STATE) VALUES (?, ?, COMPRESS(?), ?, ?)",
			new Object [] {lang, key, data, now, EObjectState.INACTIVE.value()}
		);
		
		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
		result.rs().next();
		
		TranslationBO bo = new TranslationBO();
		bo.setId(result.rs().getInt("ID"));
		bo.setLang(lang);
		bo.setKey(key);
		bo.setData(data);
		bo.setState(EObjectState.INACTIVE);
		
		bo.setUpdatedOn(Calendar.getInstance());
		bo.getUpdatedOn().setTime(now);
		
		return bo;
	}

	public static TranslationBO [] getByNamespace(AnyDB db, String ns, List<Integer> ids)
			throws SQLException {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < ids.size(); i++){
			if (i != 0){
				sb.append(", ");
			}
			sb.append("'" + ns + ids.get(i) + "'");
		}

		List<TranslationBO> result = new ArrayList<TranslationBO>();
		
		AnyResultSetContext rs = db.execSelect(
				"SELECT *, CONVERT(UNCOMPRESS(TLN_DATA) USING 'utf8') AS DATA " + 
				"FROM TLN_TRANSLATION WHERE TLN_KEY IN (" + sb.toString() + ")",
				new Object [] {}
		);
		try {
			while(rs.rs().next()){
				result.add(fromRow(rs.rs()));
			}
		} finally {
			rs.close();
		}
		
		return result.toArray(new TranslationBO []{});
	}

	public static TranslationBO put(AnyDB db, String lang, UnitBO unit, String data) throws SQLException {
		if (unit.getType() != UnitDAO.UnitType.WDGT.ordinal()) { 
			throw new RuntimeException("Unsupported type: " + unit.getType());
		}
		return put(db, lang, key(unit), data);
	}

	public static TranslationBO getTranslationFor(AnyDB db, String lang, int uni_id) throws SQLException {
		AnyResultSetContext rs = db.execSelect(
				"SELECT *, CONVERT(UNCOMPRESS(TLN_DATA) USING 'utf8') AS DATA " + 
				"FROM TLN_TRANSLATION WHERE (TLN_LANG = ? OR TLN_LANG = ?) AND TLN_KEY = ?",
				new Object [] {LANG_ANY, lang, key(uni_id)}
		);
		
		if (rs.rs().next()) {
			if (LANG_ANY.equals(rs.rs().getString("TLN_LANG"))) {
				return null;
			} else {
				return fromRow(rs.rs());
			}
		}
		
		return null;
	}
	
	public static TranslationBO get(AnyDB db, String lang, UnitBO unit) throws SQLException {
		if (unit.getType() != UnitDAO.UnitType.WDGT.ordinal()) {
			throw new RuntimeException("Unsupported type: " + unit.getType());
		}
		return get(db, lang, key(unit));
	}
	
	public static TranslationBO get(AnyDB db, String lang, String key) throws SQLException {
		AnyResultSetContext rs = db.execSelect(
				"SELECT *, CONVERT(UNCOMPRESS(TLN_DATA) USING 'utf8') AS DATA " + 
				"FROM TLN_TRANSLATION WHERE TLN_LANG = ? AND TLN_KEY = ?",
				new Object [] {lang, key}
		);
		
		TranslationBO result = null;
		if (rs.rs().next()){
  		result = fromRow(rs.rs());  		
  	}
  	
  	return result;
	}
	
	private static TranslationBO fromRow(ResultSet rs) throws SQLException {
		TranslationBO result = new TranslationBO();
		
		result.setId(rs.getInt("TLN_ID"));
		result.setLang(rs.getString("TLN_LANG"));
		result.setKey(rs.getString("TLN_KEY"));
		result.setData(rs.getString("DATA"));
		result.setState(EObjectState.fromValue(rs.getInt("TLN_STATE")));
		
    result.setUpdatedOn(Calendar.getInstance());
    result.getUpdatedOn().setTime(rs.getTimestamp("TLN_UPDATED_ON"));
    
    return result;
	}
	
}
