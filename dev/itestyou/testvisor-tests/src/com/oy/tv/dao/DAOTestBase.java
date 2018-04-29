package com.oy.tv.dao;

import junit.framework.TestCase;

import com.oy.tv.db.AnyDB;

public class DAOTestBase extends TestCase {

	public static final String conn_str = 
		"jdbc:mysql://localhost:10011/mysql?user=root&useUnicode=true&characterEncoding=utf8";
	
	protected AnyDB db;
	private String db_name;
	
	public DAOTestBase(String dbName) {
		this.db_name = dbName;
	}
	
	public DAOTestBase(String dbName, AnyDAO dao) {
		this.db_name = dbName;
		clearAllData(db_name, dao);
	}
	
	@Override
	public void setUp() throws Exception {
		db = new AnyDB();
		db.open_mysql(conn_str, db_name);
	}  

	public static void clearAllData(String db_name, AnyDAO dao) {
		try {
			AnyDB db = new AnyDB();
  		db.open_mysql(conn_str, db_name);
  
  		db.execUpdate("DROP DATABASE IF EXISTS " + db_name + ";");
    	db.execUpdate("CREATE DATABASE " + db_name + ";");
    	db.execSelect("USE " + db_name + ";");
  
  		dao.init(db);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
}
