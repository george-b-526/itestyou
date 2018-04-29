package com.oy.tv.dao.core;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;

public class UnitTests extends DAOTestBase {
  
	public UnitTests(){
		super("coreunittests_ITY_ADMIN", new AllDAO());
	}
	
	public void testDeepCopy() throws Exception {
		UserBO owner = new UserBO();
		owner.setId(12345);
		
		db.trxBegin();
		UnitBO from = UnitDAO.addUnit(db, owner);
		UnitDAO.updateUnit(db, from, "xml", "notes", "title", "desc", 100, 1, EObjectState.ACTIVE);
		for (int i =0; i < 10; i++){
			VariationDAO.addVariation(db, from, "{" + i + "}");
		}
		db.trxEnd();
		
		db.trxBegin();
		UnitBO to = UnitDAO.deepCopy(db, db, owner, from);
		db.trxEnd();
		
		assertEquals(from.getXml(), to.getXml());
		assertEquals("Math Worksheets Unit #2", to.getTitle());
		assertEquals(from.getDesc(), to.getDesc());
		assertEquals(from.getGrade(), to.getGrade());
		assertEquals(EObjectState.ACTIVE, to.getState());
		
		PagedKeyList keys = VariationDAO.getAllPaged(db, to, Integer.MAX_VALUE, 0);
		assertEquals(10, keys.ids.size());
	}

}
