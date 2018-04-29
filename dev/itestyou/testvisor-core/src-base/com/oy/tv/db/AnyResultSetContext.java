package com.oy.tv.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AnyResultSetContext {

	private PreparedStatement ps;
	private ResultSet rs;
	
	public AnyResultSetContext(PreparedStatement ps, ResultSet rs){
		this.ps = ps;
		this.rs= rs;
	}

	public ResultSet rs(){
		return rs; 
	}
	
	public void close() throws SQLException {
		ps.close();
		rs.close();
		
		ps = null;
		rs= null;
	}
	
}
