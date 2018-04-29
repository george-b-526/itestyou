package com.oy.tv.dao.local;

import java.util.Date;

import com.oy.tv.dao.DAOTestBase;

public class LocalDAOTests extends DAOTestBase {
  
	private static final String NS = "daotests_ITY_LOCAL"; 
	
	public LocalDAOTests(){
		super(NS, new AllDAO());
	}

	public void testCacheUpsert() throws Exception {
		ObjectCacheDAO.put(db, NS, "key", "foo", new Date());
		assertEquals("foo", ObjectCacheDAO.get(db, NS, "key").getValue());
		
		ObjectCacheDAO.put(db, NS, "key", "bar", new Date());
		assertEquals("bar", ObjectCacheDAO.get(db, NS, "key").getValue());
	}
}
