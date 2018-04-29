package com.oy.tv.dao.core;

import java.sql.SQLException;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;

public class CommentsDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS COM_COMMENT (" +
			"	  COM_ID INTEGER NOT NULL AUTO_INCREMENT," +
			"	  COM_USR_ID INTEGER NOT NULL," +
			"	  COM_UNI_ID INTEGER NOT NULL," +
			"	  COM_LANG VARCHAR(8)," +
			"	  COM_NAME VARCHAR(255)," +
			"	  COM_BODY MEDIUMBLOB," +
			"	  COM_POSTED_ON TIMESTAMP," +			
			"	  COM_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (COM_ID)," +  
			"	  		 INDEX idxUSR (COM_USR_ID)," +
			"	  		 INDEX idxUNI (COM_UNI_ID)," +
			"	  		 INDEX idxPOSTED_ON (COM_POSTED_ON)" +  
			") TYPE=" + AnyDAO.TBL_ENG + ";"
		);  
	}
	
}
