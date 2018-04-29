package com.oy.tv.db;

import java.sql.SQLException;

public interface IAnyDB {

	public int execUpdate(String sql) throws SQLException;
	public int execUpdate(String sql, Object[] args) throws SQLException;
	public AnyResultSetContext execSelect(String sql) throws SQLException;
	public AnyResultSetContext execSelect(String sql, Object[] args) throws SQLException;
	public void trxBegin() throws SQLException;
    public void trxEnd() throws SQLException;
	
	class MockAnyJDBC implements IAnyDB {
		public int execUpdate(String sql) throws SQLException{
			return 0;
		}
		
		public int execUpdate(String sql, Object[] args) throws SQLException{
			return 0;
		}
		
		public AnyResultSetContext execSelect(String sql) throws SQLException{
			return null;
		}
		
		public AnyResultSetContext execSelect(String sql, Object[] args) throws SQLException{
			return null;
		}
		
		public void trxBegin() throws SQLException {}
	    public void trxEnd() throws SQLException {}
	}
	
}
