package com.oy.tv.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PagedKeyList {

	public int count;
	public int pages;
	public int perPage;
	public int page;
	public int [] ids;
	
	public static PagedKeyList createFor(AnyDB db, String tableName, String keyName, String where, int perPage, int page) throws SQLException {
		PagedKeyList prs = new PagedKeyList();	
		{
			prs.perPage = perPage;
			prs.page = page;
		}

		// where
		{
			if (where != null){
				where = " WHERE " + where; 
			} else {  
				where = "";
			}
		}
		
		// get record count
		try {
			AnyResultSetContext ctx = db.execSelect("SELECT COUNT(*) AS COUNT FROM " + tableName + where);	
			ctx.rs().next();
			prs.count = ctx.rs().getInt("COUNT");
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		
		// get max page count
		{
			prs.pages = prs.count / prs.perPage;
			if (prs.count % prs.perPage != 0){
				prs.pages++;
			}
		}
		
		// check if current page in range
		{
			if (prs.pages <= prs.page){
				prs.page = prs.pages - 1;
			}
			  
			if (prs.page < 0){
				prs.page = 0;
			}
		}
		  	
		// get all ids
		List<Integer> all;
		{
			AnyResultSetContext ctx = db.execSelect(
				"SELECT " + keyName + " FROM " + tableName + where + " LIMIT ?, ?",
				new Object [] {prs.page * prs.perPage, prs.perPage}
			);	
			
			all = new ArrayList<Integer>();
			while(ctx.rs().next()){				
				int id = ctx.rs().getInt(keyName);
				all.add(new Integer(id));
			}
		}
		
		// convert
		{
			prs.ids = new int [all.size()];
			for (int i=0; i< prs.ids.length; i++){
				prs.ids[i]= all.get(i).intValue();
			}
		}
				
		return prs;
	}
	
}
