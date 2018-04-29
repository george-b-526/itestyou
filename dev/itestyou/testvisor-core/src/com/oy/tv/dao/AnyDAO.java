package com.oy.tv.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;

public abstract class AnyDAO {
 
	public abstract void init(AnyDB db) throws SQLException;
 	
	public static final String TBL_ENG = "InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_unicode_ci";
	
	private static final String SEPARATOR = "\n"; 

	public static String boolArray2String(List<Boolean> items){
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < items.size(); i++){
			if (i != 0){
				sb.append(SEPARATOR);
			}
			sb.append(items.get(i) ? "1" : "0");  
		}
		return sb.toString();
	}
	
	public static List<Boolean> string2BoolArray(String text){
		List<Boolean> items = new ArrayList<Boolean>();
		if (text != null && text.trim().length() != 0){
			String [] parts;
			if (text.trim().length() != 0){
				parts = text.split(SEPARATOR);
			} else {
				parts = new String [] {};
			}
			
			for (int i=0; i < parts.length; i++){
				items.add(parts[i].trim().equals("1") ? true : false);
			}
		}
		return items;
	}	
	
	public static String intArray2String(List<Integer> items){
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < items.size(); i++){
			if (i != 0){
				sb.append(SEPARATOR);
			}
			sb.append(items.get(i));  
		}
		return sb.toString();
	}
	
	public static List<Integer> string2IntArray(String text){
		List<Integer> items = new ArrayList<Integer>();
		if (text != null && text.trim().length() != 0){
			String [] parts;
			if (text.trim().length() != 0){
				parts = text.split(SEPARATOR);
			} else {
				parts = new String [] {};
			}
			for (int i=0; i < parts.length; i++){
				items.add(Integer.parseInt(parts[i].trim()));
			}		
		}
		return items;
	}

	public static int nextId(AnyDB db, String tableName, String idColumnName) throws SQLException {
		if (!db.trxIsActive()){
			throw new IllegalStateException("Must be in transaction.");
		}
		
		AnyResultSetContext result = db.execSelect(
				"SELECT MAX(" + idColumnName + ") FROM " + tableName + " FOR UPDATE;");
		try {
			if (result.rs().next()){
				return result.rs().getInt(1) + 1;
			} else {
				return 0;
			}
		} finally {  
			result.close();
		}  
	}
	
}
