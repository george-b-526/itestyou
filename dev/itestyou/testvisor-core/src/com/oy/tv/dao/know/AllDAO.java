package com.oy.tv.dao.know;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class AllDAO extends AnyDAO {

	@Override
	public void init(AnyDB db) throws SQLException {
		TermDAO.init(db);
		CorpusDAO.init(db);
	}  
	
}
