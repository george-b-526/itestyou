package com.oy.tv.dao.core;

import java.io.StringBufferInputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UserBO;

public class UserDAO {
		
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS USR_USER (" +
			"	  USR_ID INTEGER NOT NULL," +			
			"	  USR_NAME VARCHAR(64) NOT NULL," +
			"	  USR_PWD_HASH VARCHAR(64) NOT NULL," +
			"	  USR_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (USR_ID)," +
			"	  UNIQUE INDEX idxNAME (USR_NAME)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);  
	}
	
	public static UserBO loadUser(AnyDB db, int id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM USR_USER WHERE USR_ID = ?",
			new Object [] {id}
		);
		
		UserBO user = null;  
		if (result.rs().next()){
			user = new UserBO();
			user.setId(result.rs().getInt("USR_ID"));
			user.setName(result.rs().getString("USR_NAME"));
			user.setPasswordHash(result.rs().getString("USR_PWD_HASH"));
			user.setState(EObjectState.fromValue(result.rs().getInt("USR_STATE")));
		}  
		return user;
	}
	
	public static UserBO login(AnyDB db, String name, String pwd) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT * FROM USR_USER WHERE USR_STATE = ? AND USR_NAME = ? AND USR_PWD_HASH = MD5(?)", 
			new Object [] {EObjectState.ACTIVE.value(), name, pwd}
		);    
		try {  
			if (result.rs().next()){
				return loadUser(db, result.rs().getInt("USR_ID"));
			} else {
				return null;
			}
		} finally {
			result.close();
		}
	}   
	
	public static String getProperty(UserBO user, String key) {
		try {
  		if (user != null && user.getProperties() != null){
  			Properties props = new Properties();  			
  			props.load(new StringBufferInputStream(user.getProperties()));
  			return props.getProperty(key);
  		}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		return null;
	}

	public static boolean isInRole(UserBO user, String role){
		String roles = getProperty(user, "roles");
		return roles != null && roles.indexOf(role) != -1;
	}
	
	public static boolean canTranslate(UserBO user, String lang){
		if (isAdmin(user)) {
			return true;
		}
		if (isTranslator(user)){
			String languages = getProperty(user, "translator_of");
			return languages != null && (
					languages.indexOf(lang) != -1 || languages.indexOf(TranslationDAO.LANG_ANY) != -1);
		} 		
		return false;
	}
	
	public static boolean isTranslator(UserBO user){
		return isInRole(user, "translator") || isAdmin(user);
	}
	
	public static boolean isEditor(UserBO user){
		return isInRole(user, "editor") || isAdmin(user);
	}
	
	public static boolean isAdmin(UserBO user){
		return isInRole(user, "admin");
	}

}
