package com.oy.tv.dao.identity;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class AllDAO extends AnyDAO {

	public final static String NS_DEFAULT = "ITY_IDENTITY";
	
	@Override
	public void init(AnyDB db) throws SQLException {
		CustomerDAO.init(db);
		AuthTokenDAO.init(db);
		DeviceActivationDAO.init(db);
	}  
	
}
