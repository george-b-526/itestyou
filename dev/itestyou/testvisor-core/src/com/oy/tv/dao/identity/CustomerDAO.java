package com.oy.tv.dao.identity;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.dao.runtime.EventDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EEventType;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.util.UtilMD5;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;

public class CustomerDAO {

	final static String ID = "1000";
	final static String NAME = "admin";	// admin
	final static String PWD_HASH = "d5e22931763a508d5a3bacb82b5e9425";	// nimda
	
	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS CUS_CUSTOMER (" +
			"	  CUS_ID INTEGER NOT NULL," +			
			"	  CUS_NAME VARCHAR(64) NOT NULL," +
			"	  CUS_PWD_HASH VARCHAR(64) NOT NULL," +
			"	  CUS_CREATED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  CUS_PWD_RESET VARCHAR(64)," +
			"	  CUS_RESET_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  CUS_VERIFIED SMALLINT NOT NULL," +	
			"	  CUS_VERIFIED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  CUS_PROPERTIES MEDIUMBLOB," +
			"	  CUS_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (CUS_ID)," +
			"	  UNIQUE INDEX idxNAME (CUS_NAME)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);  	 	    
		
		// make sure default admin account exists if db is empty
		AnyResultSetContext rs = db.execSelect("SELECT COUNT(*) AS COUNT FROM CUS_CUSTOMER");		
		rs.rs().next();
		if (rs.rs().getInt("COUNT") == 0){
			db.execUpdate(    
				"INSERT INTO CUS_CUSTOMER (" + 
				"CUS_ID, CUS_NAME, CUS_PWD_HASH, CUS_CREATED_ON, " + 
				"CUS_VERIFIED, CUS_STATE" + 
				") VALUES (?, ?, ?, ?, ?, ?)",
				new Object [] {
					ID, NAME, PWD_HASH, new Date(),  
					false, EObjectState.ACTIVE.value()
				}  
			);
		}
	}
	
	public static String hashPassword(String password){
		return UtilMD5.string2md5HMA(password, "ity");
	}
	
	public static CustomerBO resetPassword(AnyDB db, String name) 
			throws SQLException, PolicyException {
		CustomerBO customer = CustomerDAO.loadCustomer(db, name);
		if (customer != null){
			db.trxBegin();
			try {  
				db.execUpdate(
  				"UPDATE CUS_CUSTOMER SET CUS_PWD_RESET = ?, CUS_RESET_ON = ? WHERE CUS_ID = ?",
  				new Object [] {EntityPolicy.makeRandomPassword(), new Date(), customer.getId()}
  			);
			} finally {
				db.trxEnd();
			}
			customer = loadCustomer(db, customer.getId());
		}
		return customer;
	}
	
	public static CustomerBO completePasswordReset(AnyDB db, CustomerBO customer) 
			throws SQLException, PolicyException {
		if (customer.getPasswordReset() == null){
			throw new PolicyException("Password reset not started.");
		}
		
		db.trxBegin();
		try {
			db.execUpdate(
				"UPDATE CUS_CUSTOMER SET CUS_PWD_HASH = ?, CUS_PWD_RESET = NULL, CUS_RESET_ON = 0 WHERE CUS_ID = ?",
				new Object [] {hashPassword(customer.getPasswordReset()), customer.getId()}
			);
			
			// wipe all existing tokens
			db.execUpdate(
					"UPDATE AUT_AUTH_TOKEN SET AUT_STATE = ? WHERE AUT_CUS_ID = ?",
					new Object [] {EObjectState.DELETED.value(), customer.getId()});

		} finally {
			db.trxEnd();
		}
		
		return loadCustomer(db, customer.getId());
	}

	public static CustomerBO login(AnyDB db, String name, String pwd) 
			throws SQLException, PolicyException { 
		CustomerBO customer = CustomerDAO.loadCustomer(db, name, pwd);
  	if (customer != null && customer.getPasswordHash().equals(CustomerDAO.hashPassword(pwd))){
  		return customer;
  	}
		return null;
	}
	
	public static CustomerBO loadCustomer(AnyDB db, String name, String pwd) 
			throws SQLException, PolicyException {
		CustomerBO customer = CustomerDAO.loadCustomer(db, name);
		
		// complete password reset
		if (customer != null && 
				customer.getPasswordReset() != null && 
				customer.getPasswordReset().equals(pwd)){
			customer = completePasswordReset(db, customer);
		}
		
		return customer;
	}
	
	public static CustomerBO loadCustomer(AnyDB db, int id) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT *, UNCOMPRESS(CUS_PROPERTIES) AS PROPS FROM CUS_CUSTOMER WHERE CUS_ID = ?",
			new Object [] {id}
		);
		try {
			return load(result);
		} finally {
			result.close();
		}	
	}
	
	public static CustomerBO loadCustomer(AnyDB db, String name) throws SQLException {
		AnyResultSetContext result = db.execSelect(
			"SELECT *, UNCOMPRESS(CUS_PROPERTIES) AS PROPS FROM CUS_CUSTOMER WHERE CUS_NAME = ?",
			new Object [] {name}
		);
		try {
			return load(result);
		} finally {
			result.close();
		}
	}
	
		
	private static CustomerBO load(AnyResultSetContext result) throws SQLException {
		CustomerBO customer = null;  
		if (result.rs().next()){
			customer = new CustomerBO();
			customer.setId(result.rs().getInt("CUS_ID"));
			customer.setName(result.rs().getString("CUS_NAME"));
			customer.setPasswordHash(result.rs().getString("CUS_PWD_HASH"));
			
			customer.setCreatedOn(Calendar.getInstance());
			customer.getCreatedOn().setTime(result.rs().getTimestamp("CUS_CREATED_ON"));
			
			customer.setPasswordReset(result.rs().getString("CUS_PWD_RESET"));

			try {
				customer.setResetOn(Calendar.getInstance());
				customer.getResetOn().setTime(result.rs().getTimestamp("CUS_RESET_ON"));
			} catch (SQLException e){
				customer.setResetOn(null);
			}
			customer.setVerified(result.rs().getInt("CUS_VERIFIED") == 1);
			customer.setProperties(result.rs().getString("PROPS"));
			customer.setState(EObjectState.fromValue(result.rs().getInt("CUS_STATE")));
		}  
		return customer;
	}

	public static void repassword(AnyDB db, CustomerBO customer, String newPwd) 
		throws SQLException, PolicyException {		
		db.execUpdate(    
			"UPDATE CUS_CUSTOMER SET CUS_PWD_HASH = ?, CUS_RESET_ON = ?, CUS_PWD_RESET = ? WHERE CUS_ID = ?",
			new Object [] {
				hashPassword(newPwd), new Date(), null, customer.getId()
			}
		);
	}
	
	public static CustomerBO register(AnyDB db, String name, String pwd) 
			throws SQLException, PolicyException {		
		CustomerBO customer;
		
		EntityPolicy.assertNewAccountData(name, pwd);		
		try {
			int id;
			
  		db.trxBegin();
  		try {
  			id = AnyDAO.nextId(db, "CUS_CUSTOMER", "CUS_ID");
  			
  			// start with 1000
  			if (id < 1000){
  				id = 1000;
  			}
  			
  			db.execUpdate(    
  				"INSERT INTO CUS_CUSTOMER (" + 
  				"CUS_ID, CUS_NAME, CUS_PWD_HASH, CUS_CREATED_ON, " + 
  				"CUS_VERIFIED, CUS_STATE" + 
  				") VALUES (?, ?, ?, ?, ?, ?)",
  				new Object [] {
  					id, name, hashPassword(pwd), new Date(),  
  					false, EObjectState.ACTIVE.value()
  				}  
  			);
  			  			
  		} finally {
  			db.trxEnd();
  		}						

  		customer = loadCustomer(db, id);
  		
			EventDAO.queueEvent(db, customer, EEventType.JOINED, CustomerDAO.class.getName(), null);
  		
		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
		
		return customer;
	}   

}
