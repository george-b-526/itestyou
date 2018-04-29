package com.oy.tv.dao.identity;

import java.sql.SQLException;
import java.util.Date;

import com.oy.tv.dao.AnyDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.EObjectState;
import com.vokamis.ity.rpc.PolicyException;

public class DeviceActivationDAO {

	static void init(AnyDB db) throws SQLException {
		db.execUpdate(   
			"CREATE TABLE IF NOT EXISTS DEA_DEVICE_ACTIVATION (" +
			"	  DEA_ID INTEGER NOT NULL," +
			"	  DEA_CUS_ID INTEGER NOT NULL," +
			"	  DEA_ATTACHED_ON TIMESTAMP NOT NULL default '0000-00-00 00:00:00'," +
			"	  DEA_IP_ADDRESS VARCHAR(32) NOT NULL," +
			"	  DEA_USER_AGENT VARCHAR(1024) NOT NULL," +
			"	  DEA_DEVICE_ID VARCHAR(255) NOT NULL," +
			"	  DEA_STATE TINYINT NOT NULL," +
			"	  UNIQUE INDEX idxID (DEA_ID)," +
			"	  INDEX idxDEA_CUS_ID (DEA_CUS_ID)" +
			") TYPE=" + AnyDAO.TBL_ENG + ";"  
		);  	 	    
	}

	public static void activateDevice(AnyDB db, CustomerBO customer, String ipAddress, String userAgent, String deviceId) throws SQLException, PolicyException {
		if (deviceId == null || deviceId.length() == 0){
			throw new PolicyException("Bad device ID.");
		}
		if (ipAddress == null || ipAddress.length() == 0){
			throw new PolicyException("Bad ip address.");
		}
		if (userAgent == null || ipAddress.length() == 0){
			userAgent = "uknown";
		}
		
		int id;
		try {
  		db.trxBegin();
  		try {
  			id = AnyDAO.nextId(db, "DEA_DEVICE_ACTIVATION", "DEA_ID");
  
  			db.execUpdate(
  					  "INSERT INTO DEA_DEVICE_ACTIVATION ("
  			    + "DEA_ID, DEA_CUS_ID, DEA_ATTACHED_ON, "
  			    + "DEA_IP_ADDRESS, DEA_USER_AGENT, DEA_DEVICE_ID, DEA_STATE"
  			    + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
  			    new Object[] {id, customer.getId(), new Date(), 
  			    		ipAddress, userAgent, deviceId, EObjectState.ACTIVE.value()});
  		} finally {
  			db.trxEnd();
  		}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
