package com.oy.tv.model.words.dicts;

import java.util.HashMap;
import java.util.Map;

import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.util.StringNavigator;

public class Push {

	public static void main(String [] args) throws Exception {
		String index = Parse.load("index.php");
		
		Map<String, String> langs = new HashMap<String, String>(); {
  		StringNavigator sn = new StringNavigator(index);
  		sn.next("<form name=\"search\"");
  		sn.next("<select ");
  		while(sn.tryNext("<option ")){
  			sn.next("value=\"");
  			sn.next("\"");
  			String ns = sn.prev().trim();
  			sn.next("</option>");
  			String name = sn.prev().trim();
  			
  			langs.put(ns, name);
  		}
		}
				
		for (String ns : langs.keySet()){
			if ("english".equals(ns)) continue;
			if ("arabic".equals(ns)) continue;
			if ("ukrainian".equals(ns)) continue;

			String content = Parse.load("xml/" + ns + ".xml");
			
			if ("norwegian_bokmal".equals(ns)){
				ns = "norwegian"; 
			}
			
			final String conn_str = "jdbc:mysql://localhost:10011/mysql?user=root&useUnicode=true&characterEncoding=utf8";

			AnyDB db = new AnyDB();
			db.open_mysql(conn_str, AllDAO.NS_DEFAULT);
			
			UserBO user = new UserBO();
			user.setId(1);
			
			String firstCap = ns.toUpperCase().substring(0, 1) + ns.substring(1); 
			
			UnitBO unit = UnitDAO.addUnit(db, user);		
			UnitDAO.updateUnit(
					db, unit, content, 
					"OY." + firstCap + "-EN.1500",
					firstCap +  "-English 1500 Vocab",
					"",
					10, 2, EObjectState.ACTIVE);
		
		}
	}
			
}
