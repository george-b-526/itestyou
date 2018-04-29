package com.oy.tv.dao.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.TagBO;
import com.oy.tv.schema.core.UnitBO;

public class TagsDAO {

	public static class TagMap {
		TagBO tag;
		boolean tagged;
		
		public TagBO getTag() {
			return tag;
		}
		
		public boolean getTagged() { 
			return tagged;
		}
	}
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS TAG_TAG (" +
			"	  TAG_ID INTEGER NOT NULL AUTO_INCREMENT," +
			"	  TAG_NS VARCHAR(255)," +
			"	  TAG_PARENT_TAG_ID INTEGER," +
			"	  TAG_NAME VARCHAR(255)," +
			"	  TAG_BODY MEDIUMBLOB," +
			"	  UNIQUE INDEX idxID (TAG_ID)," +  
			"	  		 INDEX idxNAME (TAG_NAME)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);
		
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS UTA_UNIT_TAG (" +
			"	  UTA_TAG_ID INTEGER NOT NULL," +
			"	  UTA_UNIT_ID INTEGER NOT NULL," +
			"	  UNIQUE INDEX idxFWD (UTA_TAG_ID, UTA_UNIT_ID)," +
			"	  UNIQUE INDEX idxREV (UTA_UNIT_ID, UTA_TAG_ID)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		); 
	}

	public static TagBO addTag(AnyDB db, TagBO tag) throws SQLException {
		db.trxBegin();

		db.execUpdate(
			"INSERT INTO TAG_TAG (TAG_NS, TAG_PARENT_TAG_ID, TAG_NAME, TAG_BODY) VALUES (?, ?, ?, ?)",
			new Object []{tag.getNs(), tag.getParentId(), tag.getName(), tag.getBody()}
		);
		
		AnyResultSetContext result = db.execSelect("SELECT LAST_INSERT_ID() AS ID");
		result.rs().next();
		
		tag.setId(result.rs().getInt("ID"));

		db.trxEnd();
		
		return tag;
	}
	
	public static void tagUnit(AnyDB db, UnitBO unit, int [] tagIds){
		try {
			db.trxBegin();
			
			db.execUpdate(
				"DELETE FROM UTA_UNIT_TAG WHERE UTA_UNIT_ID = ?",
				new Object []{unit.getId()}
			);
			
			for (int i=0; i < tagIds.length;  i++){
				db.execUpdate(
					"INSERT  INTO UTA_UNIT_TAG (UTA_TAG_ID, UTA_UNIT_ID) VALUES (?, ?)",
					new Object  [] {tagIds[i], unit.getId()}
				);
			}  
			
			db.trxEnd();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}   

	public static List<TagBO> getAllTagsFor(AnyDB db, String ns) throws SQLException {
		List<TagBO> all = new ArrayList<TagBO>();

		AnyResultSetContext rs = db.execSelect(
			"SELECT * FROM TAG_TAG WHERE TAG_NS = ?", 
			new Object [] {ns}
		);

		while(rs.rs().next()){
			TagBO tag = new TagBO();
			readFrom(rs.rs(), tag);
			all.add(tag);
		}
		
		return all;
	}
	
	public static List<TagMap> getAllTagsFor(AnyDB db, UnitBO unit){
		List<TagMap> all = new ArrayList<TagMap>();
	
		try { 
			AnyResultSetContext rs = db.execSelect(
				"SELECT " +   
				"TAG_ID, TAG_PARENT_TAG_ID, TAG_NS, TAG_NAME, TAG_BODY, " +
				"(SELECT COUNT(B.UTA_TAG_ID) FROM UTA_UNIT_TAG AS B " + 
				"WHERE A.TAG_ID = B.UTA_TAG_ID AND B.UTA_UNIT_ID = ?) AS TAG_COUNT " +
				"FROM TAG_TAG AS A ORDER BY TAG_NAME",
				new Object [] {unit.getId()}
			);
			try {
				while(rs.rs().next()){
					TagBO tag = new TagBO();
					readFrom(rs.rs(), tag);
					
					TagMap map = new TagMap();
					map.tag = tag;
					map.tagged = rs.rs().getInt("TAG_COUNT") != 0;

					all.add(map);
				}
			} finally {
				rs.close();
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		
		return all;
	}

	private static void readFrom(ResultSet rs, TagBO tag) throws SQLException {
		tag.setId(rs.getInt("TAG_ID"));
		tag.setNs(rs.getString("TAG_NS"));
		tag.setParentId(rs.getInt("TAG_PARENT_TAG_ID"));
		tag.setName(rs.getString("TAG_NAME"));
		{
			byte [] bytes = rs.getBytes("TAG_BODY");
			if (bytes != null){
				tag.setBody(new String(bytes));
			}
		}
		
	}
	
}
