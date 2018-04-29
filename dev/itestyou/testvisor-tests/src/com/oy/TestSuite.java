package com.oy;

import junit.framework.Test;

import com.oy.ity.api.ApiServiceStubTest;
import com.oy.ity.rpc.ApiServiceProxyTest;
import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.core.UnitTests;
import com.oy.tv.dao.local.LocalDAOTests;
import com.oy.tv.dao.runtime.ResponseTests;
import com.oy.tv.model.learn.ProgressCalculatorTests;
import com.oy.tv.model.unit.LispEvaluatorTests;
import com.oy.tv.model.unit.TranslationWeaverTest;
import com.oy.tv.model.unit.UnitProcessorTest;
import com.oy.tv.util.SmartCacheTests;
import com.oy.tv.vocb.view.LegacySelectorTests;
import com.oy.tv.vocb.view.VocabViewTests;
import com.oy.tv.wdgt.view.WidgetsTests;

public class TestSuite extends junit.framework.TestSuite {

	public static void main(String [] args) throws Exception {
		DAOTestBase.clearAllData(com.oy.tv.dao.core.AllDAO.NS_DEFAULT, 
				new com.oy.tv.dao.core.AllDAO());
		DAOTestBase.clearAllData(com.oy.tv.dao.runtime.AllDAO.NS_DEFAULT, 
				new com.oy.tv.dao.runtime.AllDAO());
		DAOTestBase.clearAllData(com.oy.tv.dao.identity.AllDAO.NS_DEFAULT, 
				new com.oy.tv.dao.identity.AllDAO());
		DAOTestBase.clearAllData(com.oy.tv.dao.local.AllDAO.NS_DEFAULT, 
				new com.oy.tv.dao.local.AllDAO());
	}
	
	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite();
				
		suite.addTestSuite(com.oy.tv.dao.identity.DAOTests.class);
		suite.addTestSuite(com.oy.tv.dao.core.DAOTests.class);
		suite.addTestSuite(com.oy.tv.dao.know.DAOTests.class);
		suite.addTestSuite(ApiServiceStubTest.class);
		suite.addTestSuite(ApiServiceProxyTest.class);
		suite.addTestSuite(WidgetsTests.class);
		suite.addTestSuite(ResponseTests.class);
		suite.addTestSuite(SmartCacheTests.class);
		suite.addTestSuite(VocabViewTests.class);
		suite.addTestSuite(UnitTests.class);
		suite.addTestSuite(UnitProcessorTest.class);
		suite.addTestSuite(TranslationWeaverTest.class);
		suite.addTestSuite(LispEvaluatorTests.class);
		suite.addTestSuite(LegacySelectorTests.class);
		suite.addTestSuite(ProgressCalculatorTests.class);
		suite.addTestSuite(LocalDAOTests.class);
		
		return suite;
	}

}
