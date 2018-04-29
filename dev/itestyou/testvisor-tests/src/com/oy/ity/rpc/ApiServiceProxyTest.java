package com.oy.ity.rpc;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.vokamis.ity.rpc.ApiService;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.rpc.PolicyException;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;

public class ApiServiceProxyTest extends TestCase {

	public void testValidResponse(){
		String response = 
					"<ity-api-result ver='1.0'>"+ 
					"<status>12345</status>" +
					"<reason>Blah!</reason>" +
					"<token>{abc-123}</token>" + 
					"</ity-api-result>";
		
		ApiService.RegisterEnvelope envelope = ApiService.RegisterEnvelope.valueOf(response); 
		
		assertEquals(envelope.getStatus().ordinal(), RegisterEnvelope.Status.FAILED.ordinal());
		assertEquals(envelope.getToken(), "{abc-123}");
		assertEquals(envelope.getReason(), "Blah!");
	}

	public void testNoneResponse(){
		String response = 
					"<ity-api-result ver='1.0'>"+ 
					"<status>1</status>" +
					"</ity-api-result>";

		ApiService.RegisterEnvelope envelope = ApiService.RegisterEnvelope.valueOf(response); 
		
		assertEquals(envelope.getStatus().ordinal(), 1);
		assertEquals(envelope.getToken(), null);
		assertEquals(envelope.getReason(), null);
	}
	
	public void testEmail() throws PolicyException {
		try {
			EntityPolicy.assertValidEmail("foo");
			Assert.fail();
		} catch (Exception e){}
		
		try {
			EntityPolicy.assertValidEmail(" foo@bar.com");
			Assert.fail();
		} catch (Exception e){}
		
		EntityPolicy.assertValidEmail("foo@bar.com");
		EntityPolicy.assertValidEmail("a@b.cn");
		EntityPolicy.assertValidEmail("a+b@c-d.fi");
	}
	
}
