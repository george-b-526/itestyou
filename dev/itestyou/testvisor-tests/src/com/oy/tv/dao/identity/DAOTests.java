package com.oy.tv.dao.identity;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.schema.core.AuthTokenBO;
import com.oy.tv.schema.core.CustomerBO;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;

public final class DAOTests extends DAOTestBase {

	public DAOTests() {
		super("ITY_IDENTITY", new AllDAO());
	}
	
	public void testNextIdGenerator () throws Exception {
		CustomerBO prev = CustomerDAO.register(db, "next1@bar.com", "123456");
		CustomerBO next = CustomerDAO.register(db, "next2@foo.com", "0987654");
	
		assertEquals(prev.getId() + 1, next.getId());
	}
	
	public void testRegistration () throws Exception {
		CustomerBO customer = CustomerDAO.register(db, "foo@bar.com", "123456");
	
		assertEquals(customer.getName(), "foo@bar.com");
		assertEquals(customer.isVerified(), false);
		
		customer  = CustomerDAO.loadCustomer(db, customer.getId());
		assertEquals(customer.getName(), "foo@bar.com");
		assertEquals(customer.isVerified(), false);

		customer  = CustomerDAO.loadCustomer(db, customer.getName());
		assertEquals(customer.getName(), "foo@bar.com");
		assertEquals(customer.isVerified(), false);
	}

	public void testAuthToken () throws Exception {
		CustomerBO customer = CustomerDAO.register(db, "bee3@boo.com", "qwerty");
	
		AuthTokenBO token = AuthTokenDAO.createAuthToken(
			db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-id");
		
		assertEquals(token.getCustomer().getName(), customer.getName());
		
		try {
			token = AuthTokenDAO.createAuthToken(  
					db, customer.getName(), "bad password", "1.2.3.6", "OPERA", "device-id");
			fail("Should have failed for bad password");
		} catch (PolicyException pe){ }
		
		try {
			token = AuthTokenDAO.createAuthToken(  
					db, "noname", "qwerty", "1.2.3.6", "OPERA", "device-id");
			fail("Should have failed for unknown user.");
		} catch (PolicyException pe){ }
		
	}
	
	public void testManyTokensPerDeviceId() throws Exception {
		CustomerBO customer = CustomerDAO.register(db, "bee4@boo.com", "qwerty");
	
		AuthTokenBO token1 = AuthTokenDAO.createAuthToken(
			db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-1");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()));
		
		AuthTokenBO token2 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-1");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()).getUserId());
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token2.getToken()).getUserId());
	
		AuthTokenBO token3 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-1");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()).getUserId());
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token2.getToken()).getUserId());		
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token3.getToken()).getUserId());
	}
	

	public void testManyTokensActiveFormMultipleDevices() throws Exception {
		CustomerBO customer = CustomerDAO.register(db, "bee5@boo.com", "qwerty");
	
		AuthTokenBO token1 = AuthTokenDAO.createAuthToken(
			db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-2");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()));
		
		AuthTokenBO token2 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-3");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()));
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token2.getToken()));
	
		AuthTokenBO token3 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-4");
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()));
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token2.getToken()));		
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token3.getToken()));
	}
	
	public void testPasswordResetWipesExistingTokens() throws Exception {
		CustomerBO customer = CustomerDAO.register(db, "reset@boo.com", "qwerty");
	
		AuthTokenBO token1 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-2");
		AuthTokenBO token2 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-2");
		AuthTokenBO token3 = AuthTokenDAO.createAuthToken(
				db, customer.getName(), "qwerty", "1.2.3.4", "IE", "device-2");
	
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token1.getToken()).getUserId());
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token2.getToken()).getUserId());		
		assertTrue(null != AuthTokenDAO.getCustomerIdFor(db, token3.getToken()).getUserId());
		
		assertEquals(customer.getPasswordReset(), null);
		assertEquals(customer.getResetOn(), null);
		
		CustomerBO reset = CustomerDAO.resetPassword(db, "reset@boo.com");
		assertEquals(reset.getId(), customer.getId());
		assertTrue(reset.getPasswordReset() != null);
		assertTrue(reset.getResetOn() != null);
		
		reset = CustomerDAO.loadCustomer(db, "reset@boo.com", reset.getPasswordReset());
		assertTrue(reset != null);
		
		assertTrue(null == AuthTokenDAO.getCustomerIdFor(db, token1.getToken()).getUserId());
		assertTrue(null == AuthTokenDAO.getCustomerIdFor(db, token2.getToken()).getUserId());		
		assertTrue(null == AuthTokenDAO.getCustomerIdFor(db, token3.getToken()).getUserId());
	}

	public void testRandomPwd() {
		for (int i=0; i < 100; i++){
			EntityPolicy.makeRandomPassword();
		}
	}

	public void testPassdwordResetFlow() throws Exception {
		CustomerBO before = CustomerDAO.register(db, "reset2@outplay.com", "qwerty");
		assertTrue(before.getPasswordReset() == null);
		assertTrue(before.getResetOn() == null);
		
		CustomerBO reset = CustomerDAO.resetPassword(db, "reset2@outplay.com");
		assertTrue(reset.getPasswordReset() != null);
		assertTrue(reset.getResetOn() != null);

		CustomerBO old = CustomerDAO.loadCustomer(db, before.getId());
		assertEquals(CustomerDAO.hashPassword("qwerty"), old.getPasswordHash());
		
		CustomerBO after = CustomerDAO.loadCustomer(db, "reset2@outplay.com", reset.getPasswordReset());
		assertTrue(after.getPasswordReset() == null);
		assertTrue(after.getResetOn() == null);
	}

}
