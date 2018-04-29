package com.oy.tv.dao.know;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.CorpusBO;
import com.oy.tv.schema.core.DiffItemBO;
import com.oy.tv.schema.core.ProjectionBO;
import com.oy.tv.schema.core.TermBO;
import com.oy.tv.schema.core.TermEditBO;
import com.oy.tv.schema.core.TermsBO;

public class TermDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS TRM_TERM (" +
			"	  TRM_ID INTEGER AUTO_INCREMENT NOT NULL," +
			"	  TRM_COR_ID INTEGER NOT NULL," +						// owning corpus "SAT Vocabulary"
			"	  TRM_DIMENTION VARCHAR(255) NOT NULL," +		// en, en-uk, ...
			"	  TRM_VALUE VARCHAR(4096) NOT NULL," +			// text
			"	  TRM_TYPE VARCHAR(255) NOT NULL," +				// noun, adv, ...
			"	  TRM_CATEGORY VARCHAR(255)," +							// colors, business, ...
			"	  TRM_UPDATED_ON TIMESTAMP NOT NULL," +
			"	  UNIQUE INDEX idx1 (TRM_ID)," +
			"	  			 INDEX idx2 (TRM_COR_ID, TRM_DIMENTION)," +
			"	  	     INDEX idx3 (TRM_VALUE, TRM_TYPE)," +
			"	  	     INDEX idx4 (TRM_CATEGORY)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);
		
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS REL_TERM_RELATION (" +
			"	  REL_ID INTEGER AUTO_INCREMENT NOT NULL," +
			"	  REL_TRM_ID_TO INTEGER NOT NULL," +
			"	  REL_TRM_ID_FROM INTEGER NOT NULL," +
			"	  REL_TYPE VARCHAR(128) NOT NULL," +		// can be /is-a, /antonym, /synonym, /definition
			"	  REL_INDEX INTEGER NOT NULL," + 				// unique index for relation instance
			"	  REL_UPDATED_ON TIMESTAMP NOT NULL," +
			"	  UNIQUE INDEX idx1 (REL_ID)," +
			"	  			 INDEX idx2 (REL_TRM_ID_FROM, REL_TRM_ID_TO, REL_TYPE)," +
			"	  	     INDEX idx3 (REL_TRM_ID_FROM, REL_INDEX)," +
			"	  	     INDEX idx4 (REL_TRM_ID_TO, REL_INDEX)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);
	}

	private static void row2object(AnyResultSetContext rs, String ns, TermBO term) throws SQLException {
		term.setId(rs.rs().getInt(ns + "TRM_ID"));
		term.setCorpusId(rs.rs().getInt(ns + "TRM_COR_ID"));
		term.setDimention(rs.rs().getString(ns + "TRM_DIMENTION"));
		term.setValue(rs.rs().getString(ns + "TRM_VALUE"));
		term.setType(rs.rs().getString(ns + "TRM_TYPE"));
		term.setCategory(rs.rs().getString(ns + "TRM_CATEGORY"));		
	}
	
	public static TermBO lookup(AnyDB db, CorpusBO corpus, String dimention, String value, String type) throws SQLException {
		AnyResultSetContext result = db.execSelect(
				"SELECT * FROM TRM_TERM " + 
				"WHERE TRM_COR_ID = ? AND TRM_DIMENTION = ? AND TRM_VALUE = ? AND TRM_TYPE = ?",
				new Object [] {corpus.getId(), dimention, value, type});
		try {
  		if (result.rs().next()){
  			TermBO term = new TermBO();
  			row2object(result, "", term);
  			return term;
  		} else {
  			return null;
  		}
		} finally {
			result.close();
		}
	}

	public static TermBO insert(AnyDB db, CorpusBO corpus, String dimention, String value, String type, String category) 
		throws SQLException { 

		TermBO term = new TermBO();
		
		db.execUpdate(  
  		"INSERT INTO TRM_TERM (" +
  		"TRM_COR_ID, TRM_DIMENTION, TRM_VALUE, TRM_TYPE, TRM_CATEGORY, TRM_UPDATED_ON" +
  		") VALUES (?, ?, ?, ?, ?, ?)",
  		new Object [] {
				corpus.getId(), dimention, value, type, category, new Date()
  		}  
  	);

		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
		try{ 
  		result.rs().next();  		
  		term.setId(result.rs().getInt("ID"));
  		term.setCorpusId(corpus.getId());
  		term.setValue(value);
  		term.setDimention(dimention);
  		term.setType(type);
  		term.setCategory(category);
		} finally {
			result.close();
		}

		return term;
	}	

	public static TermsBO link(AnyDB db, TermBO from, TermBO to, String type, int index)
		throws SQLException {

		if (!from.getType().equals(to.getType())){
			throw new RuntimeException("Terms must be of the same type.");
		}
		
		// validate index is unique
		{
  		AnyResultSetContext rs = db.execSelect(
  				"SELECT * FROM REL_TERM_RELATION AS A " +
  				"LEFT JOIN TRM_TERM AS B ON B.TRM_ID = A.REL_TRM_ID_FROM " + 
  				"LEFT JOIN TRM_TERM AS C ON C.TRM_ID = A.REL_TRM_ID_TO " +
  				"WHERE B.TRM_ID = ? AND C.TRM_ID = ? AND A.REL_INDEX = ?",
  				new Object [] {from.getId(), to.getId(), index});
  		try {
    		if (rs.rs().next()){
    			throw new RuntimeException("Index must be unique for any pair of dimentions.");
    		}
  		} finally {
  			rs.rs().close();
  		}
		}
		
		// insert
		db.execUpdate(    
			"INSERT INTO REL_TERM_RELATION (" +
			"REL_TRM_ID_FROM, REL_TRM_ID_TO, REL_TYPE, REL_INDEX, REL_UPDATED_ON" +
			") VALUES (?, ?, ?, ?, ?)",
			new Object [] {
					from.getId(), to.getId(), type, index, new Date()
			}  
		);
		
		// last id
		int rel_id;
		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
		try{ 
  		result.rs().next();  		
  		rel_id = result.rs().getInt("ID");
		} finally {
			result.close();
		}
				
		// load back
		AnyResultSetContext rs = db.execSelect(
				"SELECT * FROM REL_TERM_RELATION AS A " +
				"LEFT JOIN TRM_TERM AS B ON B.TRM_ID = A.REL_TRM_ID_FROM " + 
				"LEFT JOIN TRM_TERM AS C ON C.TRM_ID = A.REL_TRM_ID_TO " +
				"WHERE REL_ID = ?",
				new Object [] {rel_id});
		TermsBO terms = new TermsBO();
		try {
  		rs.rs().next();

			TermBO _from = new TermBO();
			row2object(rs, "B.", _from);
			
			TermBO _to = new TermBO();
			row2object(rs, "C.", _to);

 			terms.setFrom(from);
			terms.setTo(to);  				
			
			terms.setId(rs.rs().getInt("A.REL_ID"));
			terms.setIndex(rs.rs().getInt("A.REL_INDEX"));
			terms.setType(rs.rs().getString("A.REL_TYPE"));
			terms.setCategory(from.getCategory());
		} finally {
			rs.rs().close();
		}
		
		return terms;
	}
	
	/**
	 * Apply diff item. All logical errors if any are communicated diff items.
	 * Description contain error explanantion. 
	 */
	public static List<DiffItemBO> apply(AnyDB db, ProjectionBO proj, List<DiffItemBO> items) 
			throws SQLException {
		List<DiffItemBO> result = new ArrayList<DiffItemBO>();
		
		db.trxBegin();
//		for(DiffItemBO item : items){
//			
//		}
		db.trxEnd();
		
		return result;
	}
	
	/**
	 * Integer in the index of toTerm in proj.getTerms() value of which is being changed,
	 * String is a new value; null indicates deletion if old value was not null.
	 * This does not handle addition of fromDimention. Only changes to terms
	 * in toDimention or links.
	 */
	public static List<DiffItemBO> diff(AnyDB db, ProjectionBO proj, List<TermEditBO> edits) {
		List<DiffItemBO> result = new ArrayList<DiffItemBO>();
		
		for (TermEditBO edit : edits){
			int index = edit.getId();
			TermsBO terms = proj.getTerms().get(index);
			String updated = edit.getValue();
			String original = null;
			if (terms.getTo() != null){
				original = terms.getTo().getValue();
			}
			
			if (updated != null && updated.trim().length() == 0){
				updated = null;
			}
			
			if (original == updated || (original != null && original.equals(updated))){
				continue;
			}
			
			DiffItemBO item = new DiffItemBO();
			item.setId(index);
			item.setOldValue(original);
			item.setNewValue(updated);
			
			if (original == null) {
				if (updated != null) {
					item.setDescription("+@" + index + ": NULL >> " + updated);
				}
			} else {
				if (updated == null) {
					item.setDescription("-@" + index + ": " + original + " >> NULL");
				} else {
					item.setDescription("!@" + index + ": " + original + " >> " + updated);
				}
			}
			
			result.add(item);
		}
		
		return result;
	}
	
	public static ProjectionBO project(AnyDB db, CorpusBO corpus, String dimentionFrom, String dimentionTo)
	throws SQLException {
		return project(db, corpus, dimentionFrom, dimentionTo, null);
	}
	
	public static ProjectionBO project(AnyDB db, CorpusBO corpus, String dimentionFrom, String dimentionTo, String type)
		throws SQLException {
		ProjectionBO result = new ProjectionBO();
		
		result.setCorpus(CorpusDAO.load(db, corpus.getId()));
		result.setDimentionFrom(dimentionFrom);
		result.setDimentionTo(dimentionTo);
		result.setRelationType(type);		
		
		Map<Integer, TermBO> all = new HashMap<Integer, TermBO>();
		
		project(db, dimentionFrom, dimentionTo, type, false, result, all);
		project(db, dimentionFrom, dimentionTo, type, true, result, all);
			
		orphanex(db, result, dimentionFrom, false, all);
		orphanex(db, result, dimentionTo, true, all);
		
		return result;
	}

	private static void orphanex(AnyDB db, ProjectionBO result, String dimention, boolean inverted, Map<Integer, TermBO> all)
	    throws SQLException {  
		
		AnyResultSetContext rs = db.execSelect(
				"SELECT * FROM TRM_TERM " + 
				"WHERE TRM_COR_ID = ? AND TRM_DIMENTION = ?",
				new Object [] {result.getCorpus().getId(), dimention});
		try {
  		while (rs.rs().next()){
  			TermBO term = new TermBO();
  			row2object(rs, "", term);

  			if (all.containsKey(term.getId())){
  				continue;
  			}
  			
  			TermsBO terms = new TermsBO();
  			terms.setId(-1);
  			terms.setIndex(-1);
  			terms.setType(term.getType());
  			terms.setCategory(term.getCategory());

  			if (inverted){
  				terms.setTo(term);
  			} else {
  				terms.setFrom(term);
  			}
  			
  			result.getTerms().add(terms);
  		}
		} finally {
			rs.close();
		}
  }
	
	private static void project(AnyDB db, String dimentionFrom, String dimentionTo, String type, boolean inverted, 
			ProjectionBO result, Map<Integer, TermBO> all)
		throws SQLException {
		
		String _from = dimentionFrom;
		String _to = dimentionTo;
		if (inverted){
			_from = dimentionTo;
			_to = dimentionFrom;
		}
		
		Object [] args;
		String where = "";
		if (type != null){
			args = new Object [] {type, _from, _to, result.getCorpus().getId(), result.getCorpus().getId()};
			where = "REL_TYPE = ? AND";
		} else {
			args = new Object [] {_from, _to, result.getCorpus().getId(), result.getCorpus().getId()};
		}
		
		AnyResultSetContext rs = db.execSelect(
				"SELECT * FROM REL_TERM_RELATION AS A " +
				"LEFT JOIN TRM_TERM AS B ON B.TRM_ID = A.REL_TRM_ID_FROM " + 
				"LEFT JOIN TRM_TERM AS C ON C.TRM_ID = A.REL_TRM_ID_TO " +
				"WHERE " + where + " B.TRM_DIMENTION = ? AND C.TRM_DIMENTION = ? " +
				"AND B.TRM_COR_ID = ? AND C.TRM_COR_ID = ? " +
				"ORDER BY REL_INDEX",
				args);
		try {
  		while (rs.rs().next()){
  			TermsBO terms = new TermsBO();
  			
  			TermBO from = new TermBO();
  			row2object(rs, "B.", from);
  			
  			TermBO to = new TermBO();
  			row2object(rs, "C.", to);

  			if (inverted){
    			terms.setFrom(to);
    			terms.setTo(from);
  			} else {
    			terms.setFrom(from);
    			terms.setTo(to);  				
  			}
  			
  			terms.setId(rs.rs().getInt("A.REL_ID"));
  			terms.setIndex(rs.rs().getInt("A.REL_INDEX"));
  			terms.setType(from.getType());
  			terms.setCategory(from.getCategory());
  			
  			all.put(from.getId(), from);
  			all.put(to.getId(), to);
  			
  			result.getTerms().add(terms);
  		}
		} finally {
			rs.close();
		}		
	}
	
}
