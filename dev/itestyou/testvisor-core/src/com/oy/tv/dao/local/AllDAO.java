package com.oy.tv.dao.local;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class AllDAO extends AnyDAO  {

	public final static String NS_DEFAULT = "ITY_LOCAL";
	
	@Override 
	public void init(AnyDB db) throws SQLException {
		ObjectCacheDAO.init(db);
	}  
	
}
