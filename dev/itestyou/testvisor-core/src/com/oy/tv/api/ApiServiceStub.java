package com.oy.tv.api;

import com.oy.tv.dao.identity.AuthTokenDAO;
import com.oy.tv.dao.identity.CustomerDAO;
import com.oy.tv.dao.identity.DeviceActivationDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.schema.core.AuthTokenBO;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.util.DynamicPropertyManager;
import com.oy.tv.web.WebFlow;
import com.vokamis.ity.rpc.ApiService;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;

public class ApiServiceStub implements ApiService {

	private DynamicPropertyManager dpm;
	private AnyDB db;
  
	public ApiServiceStub(AnyDB db, DynamicPropertyManager dpm){
		this.db = db;
		this.dpm = dpm;
	}
	
	public LookupEnvelope lookup(String token, String deviceId,
	    String ipAddress, String userAgent) throws PolicyException {
		try {
			AuthTokenDAO.CustomerInfo ci= AuthTokenDAO.getCustomerIdFor(db, token);
  		if (ci.getUserId() != null){
  			CustomerBO customer = CustomerDAO.loadCustomer(db, ci.getUserId());
  			return new LookupEnvelope(LookupEnvelope.Status.SUCCESS, null, ci.getUserId(), customer.getName(), ci.isPro());			
  		} else {
  			return new LookupEnvelope(LookupEnvelope.Status.DENY_ACCESS, "Access denied. Please login again.");
  		}
		} catch (Throwable t){  
			t.printStackTrace(System.err);
			throw new PolicyException("Server error. Please try again later.");
		}
	}
	
	public RecoverEnvelope recover(String name, String deviceId,
	    String ipAddress, String userAgent) throws PolicyException {
		try {
			CustomerBO customer = CustomerDAO.resetPassword(db, name);
			if (customer == null){
				return new RecoverEnvelope(RecoverEnvelope.Status.FAILED_UNKNOWN_CUSTOMER, null);				
			}
			try {
  			WebFlow flow = new WebFlow();
  			flow.execute();  
  			flow.setAuthorization(
  					dpm.getPropertyValue("authorize-name"), dpm.getPropertyValue("authorize-pwd"));
  			flow.post(
  					dpm.getPropertyValue("recover-mail-url"),
  					new WebFlow.NameValue("to", customer.getName()),
  					new WebFlow.NameValue("pwd", customer.getPasswordReset()));
  			
  			String status = flow.getLastPage();
  			if (!"OK".equals(status)){
  				throw new RuntimeException("Bad response from email server.");
  			}
			} catch (Throwable t){  
				t.printStackTrace(System.err);
				throw new PolicyException(
						"Unable to send email at this time. Please try again later.");
			}
			return new RecoverEnvelope(
  				RecoverEnvelope.Status.SUCCESS, null);
		} catch (PolicyException pe){
			throw pe;
		} catch (Throwable t){
			System.err.println(t);
			throw new PolicyException("Unknown server error.");
		}
	}
	
	public RegisterEnvelope register(String name, String pwd, boolean existing, String deviceId, 
			String ipAddress, String userAgent) throws PolicyException {
		try {
  		CustomerBO customer = CustomerDAO.loadCustomer(db, name, pwd);
  		if (customer != null){
  			DeviceActivationDAO.activateDevice(db, customer, ipAddress, userAgent, deviceId);
  			if (pwd == null || !customer.getPasswordHash().equals(CustomerDAO.hashPassword(pwd))){
  				return new RegisterEnvelope(RegisterEnvelope.Status.FAILED_EXISTS_FAILED_AUTH, null, null);				
  			} else {
  				AuthTokenBO token = AuthTokenDAO.createAuthToken(
  						db, name, pwd, ipAddress, userAgent, deviceId);
  				return new RegisterEnvelope(RegisterEnvelope.Status.FAILED_EXISTS_SUCCESS_AUTH, token.getToken(), null);
  			}
  		} else {
  			if (existing){
  				return new RegisterEnvelope(RegisterEnvelope.Status.FAILED, null, "Account not found.");
  			}
  			customer = CustomerDAO.register(db, name, pwd);
  			DeviceActivationDAO.activateDevice(db, customer, ipAddress, userAgent, deviceId);
  			AuthTokenBO token = AuthTokenDAO.createAuthToken(
  					db, name, pwd, ipAddress, userAgent, deviceId);
  			return new RegisterEnvelope(RegisterEnvelope.Status.SUCCESS, token.getToken(), null);
  		}
		} catch (PolicyException pe){
			throw pe;
		} catch (Throwable t){
			System.err.println(t);
			throw new PolicyException("Unknown server error.");
		}
	}

	public RepasswordEnvelope repassword(String name, String oldPwd, String newPwd, String deviceId, 
			String ipAddress, String userAgent) throws PolicyException {
		try {
  		CustomerBO customer = CustomerDAO.loadCustomer(db, name, oldPwd);
  		if (customer == null){
  			return new RepasswordEnvelope(RepasswordEnvelope.Status.FAILED, "Account not found.");
  		}
  		
  		if(!EntityPolicy.isValidPassword(newPwd)){
  			return new RepasswordEnvelope(RepasswordEnvelope.Status.FAILED, "Bad password. Password must be " + EntityPolicy.MIN_PWD_LEN + " characters or longer.");
  		}
  		
  		if (oldPwd == null || !customer.getPasswordHash().equals(CustomerDAO.hashPassword(oldPwd))){
  			return new RepasswordEnvelope(RepasswordEnvelope.Status.FAILED_AUTH, null);  			
  		}
  		
  		CustomerDAO.repassword(db, customer, newPwd);
  		return new RepasswordEnvelope(RepasswordEnvelope.Status.SUCCESS, null);
		} catch (PolicyException pe){
			throw pe;
		} catch (Throwable t){
			System.err.println(t);
			throw new PolicyException("Unknown server error.");
		}
	}
	
}
