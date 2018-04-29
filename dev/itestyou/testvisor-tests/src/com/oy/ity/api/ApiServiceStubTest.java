package com.oy.ity.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.oy.tv.api.ApiServiceStub;
import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.identity.AllDAO;
import com.vokamis.ity.rpc.ApiService.LookupEnvelope;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;
import com.vokamis.ity.rpc.ApiService.RepasswordEnvelope;

public class ApiServiceStubTest extends DAOTestBase {

	public ApiServiceStubTest() {
		super("ITY_IDENTITY", new AllDAO());
	}	

	public void testMustBeExistingUserOrFail() throws Exception {		
		ApiServiceStub stub = new ApiServiceStub(db, null);
		
		RegisterEnvelope env = stub.register(
				"unknown53245324.nk@bar.com", "qwerty", true, "{098765}", "192.167.1.1", "IE");
		
		assertEquals(RegisterEnvelope.Status.FAILED, env.getStatus());
	}
	
	public void testRegistrationWhenNotRegistered() throws Exception {		
		ApiServiceStub stub = new ApiServiceStub(db, null);
		
		RegisterEnvelope env = stub.register(
				"foo5@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "IE");
		
		assertEquals(env.getStatus(), RegisterEnvelope.Status.SUCCESS);
	}
	
	public void testLookup() throws Exception {
		ApiServiceStub stub = new ApiServiceStub(db, null);

		RegisterEnvelope register = stub.register(
				"foo5@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "IE");
		

		LookupEnvelope success = stub.lookup(register.getToken(), "deviceId", "ipAddress", "userAgent");
		assertEquals(success.getStatus(), LookupEnvelope.Status.SUCCESS);
		assertEquals(success.getReason(), null);
		
		LookupEnvelope deny = stub.lookup("my token", "deviceId", "ipAddress", "userAgent");
		assertTrue(deny.getReason() != null);
	}
		
	public void testResponseRendering() throws Exception {
		StringWriter sw;
		Action action = new Action("foo"); 
		
		{
  		sw = new StringWriter();
  		action.reply(new PrintWriter(sw), 12345, "no reason");
  		assertEquals(sw.toString(), 
  				"<ity-api-result verb='foo' ver='1.0'><status>12345</status><reason>no reason</reason></ity-api-result>");
		}
					
		{
  		sw = new StringWriter();
  		action.reply(new PrintWriter(sw), 12345, "no reason", new NameValue("name1", "value1"));
  		assertEquals(sw.toString(), 
  				"<ity-api-result verb='foo' ver='1.0'><status>12345</status><reason>no reason</reason><name1>value1</name1></ity-api-result>");
		}
	}
	
	public void testRegisterWhenAlreadyRegistered() throws Exception {
		ApiServiceStub stub = new ApiServiceStub(db, null);

		RegisterEnvelope try1 = stub.register(
				"foo6@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "IE");

		RegisterEnvelope try2 = stub.register(
				"foo6@bar.com", "bad password", false, "{098765}", "192.167.1.1", "Android");

		RegisterEnvelope try3 = stub.register(
				"foo6@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "FF");

		assertEquals(try1.getStatus(), RegisterEnvelope.Status.SUCCESS);
		assertEquals(try2.getStatus(), RegisterEnvelope.Status.FAILED_EXISTS_FAILED_AUTH);
		assertEquals(try3.getStatus(), RegisterEnvelope.Status.FAILED_EXISTS_SUCCESS_AUTH);
		assertFalse(try1.getToken().equals(try3.getToken())); 
	}
	
	public void testPro() throws Exception {
		ApiServiceStub stub = new ApiServiceStub(db, null);

		{
  		RegisterEnvelope re = stub.register(
  				"foo7@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
  
  		LookupEnvelope le = stub.lookup(re.getToken(), "{12345}", "192.167.1.1", "bad agent");
  		
  		assertTrue(le.isPro());
		}

		{
  		RegisterEnvelope re = stub.register(
  				"foo7@bar.com", "qwerty", false, "{098765}", "192.167.1.1", "app: ity-1.1");
  
  		LookupEnvelope le = stub.lookup(re.getToken(), "{12345}", "192.167.1.1", "bad agent");
  		
  		assertFalse(le.isPro());
		}
	}

	public void testRepassword() throws Exception {
		ApiServiceStub stub = new ApiServiceStub(db, null);

		// create control acountr
		RegisterEnvelope control1 = stub.register(
			"foo777@bar.com", "qwerty123", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(control1.getStatus(), RegisterEnvelope.Status.SUCCESS);
		
		// create account
		RegisterEnvelope try1 = stub.register(
				"foo77@bar.com", "qwerty123", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(try1.getStatus(), RegisterEnvelope.Status.SUCCESS);

		// check bad password fails
		RepasswordEnvelope try2 = stub.repassword(
				"foo77@bar.com", "bad password", "qwerty678", "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(try2.getStatus(), RepasswordEnvelope.Status.FAILED_AUTH);

		// change password
		RepasswordEnvelope try3 = stub.repassword(
				"foo77@bar.com", "qwerty123", "qwerty678", "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(try3.getStatus(), RepasswordEnvelope.Status.SUCCESS);

		// check old password fails
		RegisterEnvelope try4 = stub.register(
				"foo77@bar.com", "qwerty123", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(try4.getStatus(), RegisterEnvelope.Status.FAILED_EXISTS_FAILED_AUTH);
 
		// check new password works
		RegisterEnvelope try5 = stub.register(
				"foo77@bar.com", "qwerty678", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(try5.getStatus(), RegisterEnvelope.Status.FAILED_EXISTS_SUCCESS_AUTH);

		// control did not change
		RegisterEnvelope control2 = stub.register(
				"foo777@bar.com", "qwerty123", false, "{098765}", "192.167.1.1", "app: ity-pro-1.1");
		assertEquals(control2.getStatus(), RegisterEnvelope.Status.FAILED_EXISTS_SUCCESS_AUTH);
	}
}
