package com.oy;

import junit.framework.TestCase;

import com.oy.tv.web.WebFlow;
import com.vokamis.ity.rpc.ApiService.LookupEnvelope;
import com.vokamis.ity.rpc.ApiService.RecoverEnvelope;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;

public class RobotSuite extends TestCase {
	
	// Pro Apple user: mac9@outplay.com
	private final static String CUS_ID_1737 =
		"{38627250e9f2d19f7e41ef8962963e05-bd7f43837fc81efaac3b85b994c36d44}";
	
	private final static String PRO_TOKEN = CUS_ID_1737; 
	
	private final static String NON_PRO_TOKEN = 
		"{ff5496c7a34b71a220dba76efaff3b11-e13c954a97caa917efffa37aed5e5435}";
	
	private WebFlow wf;
	
	@Override
	public void setUp(){
		wf = new WebFlow(null, "http://www.itestyou.com", null);
		wf.execute();		
	}  
	
	public void testRecoverApi(){
		RecoverEnvelope result;
		
		wf.get("/api/identity?verb=recover");
		result = RecoverEnvelope.valueOf(wf.getLastPage());
		assertEquals(RecoverEnvelope.Status.FAILED_UNKNOWN_CUSTOMER, result.getStatus());

		wf.get("/api/identity?verb=recover&name=admin@itestyou.com");
		result = RecoverEnvelope.valueOf(wf.getLastPage());
		assertEquals(result.getStatus(), RecoverEnvelope.Status.SUCCESS);
	}

	public void testRegisterApi(){
		RegisterEnvelope result;
		
		wf.get("/api/identity?verb=register");
		result = RegisterEnvelope.valueOf(wf.getLastPage());
		assertEquals(result.getStatus(), RegisterEnvelope.Status.FAILED);
		  
		wf.get("/api/identity?verb=register&name=admin@itestyou.com&device-id=qwerty");
		result = RegisterEnvelope.valueOf(wf.getLastPage());
		assertEquals(RegisterEnvelope.Status.FAILED_EXISTS_FAILED_AUTH, result.getStatus());
	}

	public void testRawApi(){
		try {
			wf.get("/api/identity?");
			fail("Must have failed with 400 Bad request.");
		} catch (RuntimeException e){
			assertEquals("HttpException", e.getCause().getClass().getSimpleName());
		}		
	}

	public void testRogueNonProAndProLookup(){
		LookupEnvelope result;

		wf.get("/api/identity?verb=lookup");
		result = LookupEnvelope.valueOf(wf.getLastPage());
		assertEquals(LookupEnvelope.Status.FAILED, result.getStatus());
		
		wf.get("/api/identity?verb=lookup&token=" + NON_PRO_TOKEN);
		result = LookupEnvelope.valueOf(wf.getLastPage());
		assertEquals(LookupEnvelope.Status.SUCCESS, result.getStatus());
		assertFalse(result.isPro());

		wf.get("/api/identity?verb=lookup&token=" + PRO_TOKEN);
		result = LookupEnvelope.valueOf(wf.getLastPage());
		assertEquals(LookupEnvelope.Status.SUCCESS, result.getStatus());
		assertTrue(result.isPro());
	}

	public void testNonProSeesAdsProDoesNot(){
		{
  		wf.get("/view/leaderboard?app_session=" + PRO_TOKEN);
  		String page = wf.getLastPage();
  		assertTrue(page.indexOf("<ity-inline-ad />") == -1);
  		assertTrue(page.indexOf("google_ad_client") == -1);
		}
		
		{
  		wf.get("/view/leaderboard?app_session=" + NON_PRO_TOKEN);
  		String page = wf.getLastPage();
  		assertTrue(page.indexOf("<ity-inline-ad />") == -1);
  		assertTrue(page.indexOf("google_ad_client") == -1);
		}  
	}

	public void testSiteIsUp(){
		wf = new WebFlow(null, null, null);
		wf.execute();		

		final String text = "<a href=\"/cms/terms-of-use/\">Terms of use</a>";
		
		wf.get("http://www.itestyou.com");
		assertTrue(wf.getLastPage().indexOf(text) != -1);
		
		wf.get("https://www.itestyou.com");
		assertTrue(wf.getLastPage().indexOf(text) != -1);
	}
	
	public void testMathBot(){
		wf = new WebFlow(null, null, null);
		wf.execute();		

		wf.get("http://www.itestyou.com/test/wdgt?action_id=0&inGradeId=1");
		assertTrue(wf.getLastPage().indexOf(
				"<title>Math Worksheet Challenge - I Test You</title>") != -1);
	}
	
	public void testSatBotPlay(){
		wf = new WebFlow(null, null, null);
		wf.execute();		

		wf.get("http://www.itestyou.com/test/vocb?action_id=0&inUnitId=115");
		assertTrue(wf.getLastPage().indexOf(
				"<title>SAT 5000 Vocabulary - I Test You</title>") != -1);
	}
}