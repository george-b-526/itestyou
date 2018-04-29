package com.oy.tv.dao.local;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.ObjectCacheBO;

public class ObjectCacheDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS OCA_OBJECT_CACHE(" +
			"	  OCA_KEY VARCHAR(255) NOT NULL," +
			"	  OCA_VALUE MEDIUMBLOB," +  
			"	  OCA_DATE TIMESTAMP," +
			"	  UNIQUE INDEX idxKEY (OCA_KEY)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}

	public static ObjectCacheBO put(AnyDB db, String key, String value, Date date) throws SQLException {
		return put(db, AllDAO.NS_DEFAULT, key, value, date);
	}
	
	public static ObjectCacheBO put(AnyDB db, String ns, String key, String value, Date date) throws SQLException {
		db.trxBegin();
		
		db.execUpdate(
			"REPLACE INTO " + AnyDB.formatNS(ns) + "OCA_OBJECT_CACHE (OCA_KEY, OCA_VALUE, OCA_DATE) VALUES (?, COMPRESS(?), ?)",
			new Object [] {key, value, new java.sql.Timestamp(date.getTime())}
		);   	

		db.trxEnd();
		
		ObjectCacheBO item = new ObjectCacheBO();
		item.setKey(key);
		item.setValue(value);		
		
		item.setDate(Calendar.getInstance());
		item.getDate().setTime(date);
		
		return item;
	}
	  
	public static boolean remove(AnyDB db, String ns, String key) throws SQLException {
		int rows = db.execUpdate(
			"DELETE " + AnyDB.formatNS(ns) + "FROM OCA_OBJECT_CACHE WHERE OCA_KEY = ?",
			new Object [] {key}
		); 
		  
		return rows != 0;
	}

	public static ObjectCacheBO get(AnyDB db, String key) throws SQLException {
		return get(db, AllDAO.NS_DEFAULT, key);
	}
	
	public static ObjectCacheBO get(AnyDB db, String ns, String key) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT *, UNCOMPRESS(OCA_VALUE) AS VALUE FROM " + AnyDB.formatNS(ns) + "OCA_OBJECT_CACHE WHERE OCA_KEY = ?",
			new Object [] {key}
		);
			  
		ObjectCacheBO item = null;  
		if (result.rs().next()){
			item = new ObjectCacheBO();
			item.setKey(result.rs().getString("OCA_KEY"));
			item.setValue(result.rs().getString("VALUE"));
			
			item.setDate(Calendar.getInstance());
			item.getDate().setTime(result.rs().getTimestamp("OCA_DATE"));
		}
		  
		return item;
	}	
}
