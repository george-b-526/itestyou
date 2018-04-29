package com.oy.tv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class AnyDB implements IAnyDB {
 
	private String driver;
	private Connection conn;

	boolean inTrx = false;
	boolean shutdown = false;
	
	private void open(String driver, String connStr) throws Exception {
		this.driver = driver;
		
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection(connStr);
	}
	
	public void open_hsql(String folder) throws Exception {
		open(  
			"org.hsqldb.jdbcDriver",
			"jdbc:hsqldb:" + folder + "/data/" 
		);
	}
	   
	public void open_derby(String folder) throws Exception {
		open( 
			"org.apache.derby.jdbc.EmbeddedDriver",
			"jdbc:derby:" + folder + ";create=true"
		);
	}
 	
	public void open_mysql(String connStr, String dbname) throws Exception {
		open(  
			"com.mysql.jdbc.Driver",
			connStr
		);
		
		execUpdate("CREATE DATABASE IF NOT EXISTS " + dbname + ";");
		execSelect("USE " + dbname + ";");
	}
	
	private void log(Exception e){
		e.printStackTrace();
	}
	
	void execUpdateEx(String sql) throws SQLException {
		System.out.println("SQL >> " + sql);
		execUpdate(sql);
	} 
	
	public int execUpdate(String sql) throws SQLException {
		return 	execUpdate(sql, new Object[] {});
	}
	
	public int execUpdate(String sql, Object[] args) throws SQLException {
        int count = -1;
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParams(1, stmt, args);
            count = stmt.executeUpdate();
        } finally {
	        if (stmt != null){
	        	try {
	        		stmt.close();
	        	} catch (Exception ee){
	        		log(ee);
	        	}
	        }
        } 

        return count;
    }
	
	public AnyResultSetContext execSelectLarge(String sql) throws SQLException {
		return execSelectLarge(sql, null);
	}
	
	public AnyResultSetContext execSelectLarge(String sql, Object[] args) throws SQLException {
		AnyResultSetContext ars;
		  
        ResultSet rs = null;
        try {
        	PreparedStatement lastStmt = conn.prepareStatement(
         		sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY
        	);         
        	lastStmt.setFetchSize(Integer.MIN_VALUE);
        	if (args != null){  
        		setParams(1, lastStmt, args);
        	}

            rs = lastStmt.executeQuery();
            ars = new AnyResultSetContext(lastStmt, rs);
        } catch (SQLException e) {
        	log(e);
            throw e;
        }

        return ars;
    }
	
	public AnyResultSetContext execSelect(String sql, Object[] args) throws SQLException {
		AnyResultSetContext ars;
		
		ResultSet rs = null;
        try {
        	PreparedStatement lastStmt = conn.prepareStatement(sql); 
        	if (args != null){
        		setParams(1, lastStmt, args);
        	}
   
            rs = lastStmt.executeQuery();

            ars = new AnyResultSetContext(lastStmt, rs);
        } catch (SQLException e) {
        	log(e);
            throw e;
        }
 
        return ars;
    }
	
	private void setParams(int index, PreparedStatement stmt, Object [] args) throws SQLException {
		for (int i=0; i < args.length; i++){			
			Object obj = args[i];
			while (true){		
			    if (obj instanceof Boolean) {
			    	stmt.setBoolean(index, ((Boolean) obj).booleanValue());
		    		break;
			    }  
				
				if (obj instanceof Double) {
			        stmt.setDouble(index, ((Double) obj).doubleValue());
			        break;
			    }
			    
			    if (obj instanceof Float) {
			        stmt.setFloat(index, ((Float) obj).floatValue());
			        break;
			    }
			    
			    if (obj instanceof Integer) {
			        stmt.setInt( index, ((Integer) obj).intValue());
			        break;
			    }
			    
			    if (obj instanceof Long) {
			        stmt.setLong( index, ((Long) obj).longValue());
			        break;
			    }
			    
			    if (obj instanceof String) {
			        stmt.setString(index, ((String) obj));
			        break;
			    }
			    
			    if (obj instanceof Date) {
			        stmt.setTimestamp(index,  new Timestamp( ((Date) obj).getTime()) );
			        break;
			    }
			    
			    if (obj == null ) {
			        stmt.setObject(index,  null );
			        break;
			    }
			    
			    if (obj instanceof byte []){  
		    		byte [] bytes = (byte []) obj;
		    		stmt.setBytes(index, bytes);
		    		break;
			    }
			    
			    if (obj instanceof Object []) {
		    		setParams(index, stmt, (Object []) obj);
		    		break;
			    }
			    			        	        
			    throw new IllegalArgumentException(
		            "Unsupported parameter type. SQL parameter N "+ index + ", "+ 
		            obj.getClass() 
		    	);
			}
		    index++;
		}
    }	
	  
	public AnyResultSetContext execSelect(String sql) throws SQLException {
		return execSelect(sql, new Object[] {});
	}	
	 
	public int max(String table, String column) throws SQLException {
		AnyResultSetContext ctx = execSelect("SELECT MAX(" + column + ") AS MAX FROM " + table);
		
		int max = 0;
		try {
			ctx.rs().next();
			max = ctx.rs().getInt("MAX");
		} finally {
			ctx.close();
		}
		   
		return max;
	}
	
    private void shutdown() throws SQLException {
    	
    	// Hypersonic requires shutdown command
    	if (driver.equals("org.hsqldb.jdbcDriver")){
    		System.out.println("Forcing shutdown for " + driver);
    		
	        Statement st = conn.createStatement();	
	        st.execute("SHUTDOWN");
    	}
         
    	// Derby requires shutdown 
    	if(driver.equals("org.apache.derby.jdbc.EmbeddedDriver")){
    		System.out.println("Forcing shutdown for " + driver);
    		
    		try {
    			DriverManager.getConnection("jdbc:derby:;shutdown=true");
    		} catch (SQLException e) {
    			if (!"Derby system shutdown.".equals(e.getMessage())){
    				throw new RuntimeException(e);
    			}
    		}
    	}
    	
    }
    
    public void trxBegin() throws SQLException {
    	execSelect("SET AUTOCOMMIT = 0;");
    	inTrx = true;
    }
      
    public void trxEnd() throws SQLException {
    	inTrx = true;
    	execSelect("SET AUTOCOMMIT = 1;");
    }   
    
    public boolean trxIsActive(){
    	return inTrx;
    }
	
	public void close() {
		try {
			if (shutdown){
				shutdown();
			}
			 
			conn.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	} 
	
	public static String formatNS(String ns){
		if (ns != null){
			return ns + ".";
		} else {
			return "";
		}
	}
	
}
