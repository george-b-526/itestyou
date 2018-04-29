package com.oy.tv.dao.runtime;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class AllDAO extends AnyDAO  {

	public final static String NS_DEFAULT = "ITY_RUNTIME";
	
	@Override 
	public void init(AnyDB db) throws SQLException {
		UserUnitResponseDAO.init(db);
		ResponseDAO.init(db);
		EventDAO.init(db);
	}  
	
}
