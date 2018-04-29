package com.oy.tv.dao.core;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class AllDAO extends AnyDAO  {

	public final static String NS_DEFAULT = "ITY_ADMIN";
	
	@Override 
	public void init(AnyDB db) throws SQLException {
		UserDAO.init(db);   
		UnitDAO.init(db); 
		VariationDAO.init(db);
		BundleDAO.init(db);
		TestDAO.init(db);
		TagsDAO.init(db);
		TranslationDAO.init(db);
	}  
	
}
