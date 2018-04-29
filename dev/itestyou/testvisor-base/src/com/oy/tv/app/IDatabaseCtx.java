package com.oy.tv.app;

import java.sql.SQLException;

import com.oy.tv.db.AnyDB;

public interface IDatabaseCtx {

	public void beginDb(); 
	
	public AnyDB getDb();
	
	public void endDb();
	
	public static class DatabaseCtx implements IDatabaseCtx {

		private AnyDB db;
		
		public DatabaseCtx(AnyDB db){
			this.db = db;
		}
		
		public void beginDb(){
			try {
				db.trxBegin();
			} catch (SQLException e){
				throw new RuntimeException(e);
			}
		}
		
		public AnyDB getDb(){
			return db;
		}
		
		public void endDb(){
			try {
				db.trxEnd();
			} catch (SQLException e){
				throw new RuntimeException(e);
			}
		}
	}
	
}
