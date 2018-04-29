package com.oy.tv.dao.know;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.CorpusBO;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EObjectState;

public class CorpusDAO {
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
				"CREATE TABLE IF NOT EXISTS COR_CORPUS (" +
				"	  COR_ID INTEGER AUTO_INCREMENT NOT NULL," +		// primary key
				"	  COR_CUS_ID INTEGER NOT NULL," +								// owner CUS_CUSTOMER
				"	  COR_NAME VARCHAR(255) NOT NULL," +						// readable name
				"	  COR_DESCRIPTION VARCHAR(4096) NOT NULL," +		// description
				"	  COR_ACL MEDIUMBLOB," +												// {1025:RWE;1000:R}
				"	  COR_STATE TINYINT NOT NULL," +
				"	  UNIQUE INDEX idx1 (COR_ID)," +
				"	  		   INDEX idx2 (COR_NAME)" +
				") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);
	}

	private static void row2object(AnyResultSetContext rs, String ns, CorpusBO corpus) throws SQLException {
		corpus.setId(rs.rs().getInt("COR_ID"));
		corpus.setOwnerId(rs.rs().getInt("COR_CUS_ID"));
		corpus.setName(rs.rs().getString("COR_NAME"));
		corpus.setDescription(rs.rs().getString("COR_DESCRIPTION"));
		corpus.setAcl(rs.rs().getString("COR_ACL"));
	}

	public static boolean delete(AnyDB db, CorpusBO corpus) throws SQLException {
		 int rows = db.execUpdate(
					"UPDATE COR_CORPUS SET COR_STATE = ? " + 
					"WHERE COR_ID = ? AND COR_STATE = ?",
					new Object [] {EObjectState.DELETED.value(), corpus.getId(), EObjectState.ACTIVE.value()});
		 return rows == 1;
	}
	
	public static List<CorpusBO> lookup(AnyDB db, CustomerBO owner) throws SQLException {
		List<CorpusBO> all = new ArrayList<CorpusBO>();
		
		AnyResultSetContext result = db.execSelect(
				"SELECT *, UNCOMPRESS(COR_ACL) AS COR_ACL FROM COR_CORPUS " + 
				"WHERE COR_CUS_ID = ? AND COR_STATE = ?",
				new Object [] {owner.getId(), EObjectState.ACTIVE.value()});
		try {
  		while (result.rs().next()){
  			CorpusBO corpus = new CorpusBO();
  			row2object(result, "", corpus);
  			all.add(corpus);
  		}
		} finally {
			result.close();
		}
		
		return all;
	}

	private static void fill(AnyResultSetContext rs, String name, List<String> list) throws SQLException {
		String value = rs.rs().getString(name);
		if (value != null){
			String [] types = value.split("\t");
			for (String type : types){
				list.add(type);
			}  		
		}
	}

	public static CorpusBO load(AnyDB db, int cor_id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
				"SELECT *, UNCOMPRESS(COR_ACL) AS COR_ACL FROM COR_CORPUS " + 
				"WHERE COR_ID = ? AND COR_STATE = ?",
				new Object [] {cor_id, EObjectState.ACTIVE.value()});
		try {
  		if (result.rs().next()){
  			CorpusBO corpus = new CorpusBO();
  			row2object(result, "", corpus);
  			
  			AnyResultSetContext rs = db.execSelect(
  					"SELECT " + 
  					"GROUP_CONCAT(DISTINCT TRM_TYPE ORDER BY TRM_TYPE SEPARATOR '\t') AS TRM_TYPES, " +
  					"GROUP_CONCAT(DISTINCT TRM_DIMENTION ORDER BY TRM_DIMENTION SEPARATOR '\t') AS TRM_DIMENTIONS, " +
  					"GROUP_CONCAT(DISTINCT TRM_CATEGORY ORDER BY TRM_CATEGORY SEPARATOR '\t') AS TRM_CATEGORIES " +
  					"FROM TRM_TERM " + 
  					"WHERE TRM_COR_ID = ?",
  					new Object [] {cor_id});
  			try {
  				rs.rs().next();
  				
  				fill(rs, "TRM_TYPES", corpus.getTypes());
  				fill(rs, "TRM_DIMENTIONS", corpus.getDimentions());
  				fill(rs, "TRM_CATEGORIES", corpus.getCategories());
  				
  			} finally {
  				rs.rs().close();
  			}
  			
  			return corpus;
  		} else {
  			return null;
  		}
		} finally {
			result.close();
		}
	}
	
	public static CorpusBO insert(AnyDB db, CustomerBO owner, String name, String desc) throws SQLException { 
		db.execUpdate(  
  		"INSERT INTO COR_CORPUS (" +
  		"COR_CUS_ID, COR_NAME, COR_DESCRIPTION, COR_STATE" +
  		") VALUES (?, ?, ?, ?)",
  		new Object [] {
  				owner.getId(), name, desc, EObjectState.ACTIVE.value()
  		}  
  	);

		CorpusBO corpus = new CorpusBO();
		
		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS COR_ID");
		try {
  		result.rs().next();

  		corpus.setId(result.rs().getInt("COR_ID"));
  		corpus.setOwnerId(owner.getId());
  		corpus.setName(name);
  		corpus.setDescription(desc);
		} finally {
			result.close();
		}
		
		return corpus;
	}
	
}
