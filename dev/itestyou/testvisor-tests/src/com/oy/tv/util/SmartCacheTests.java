package com.oy.tv.util;

import junit.framework.TestCase;

public class SmartCacheTests extends TestCase {

	public void testItemsExpire() throws Exception {
		SmartCache<String, Integer> cache = new SmartCache<String, Integer>(100, 100, 
				new SmartCacheMonitor(SmartCacheTests.class, "Test", "")); 
		
		cache.put("foo", 1);
		assertEquals((Integer) 1, cache.get("foo"));
		  
		Thread.sleep(250);
		assertEquals(null, cache.get("foo"));
	}

	public void testSizeBound(){
		SmartCache<String, Integer> cache = new SmartCache<String, Integer>(5 * 1000, 100,
				new SmartCacheMonitor(SmartCacheTests.class, "Test", ""));
		
		for (int i=0; i < 1000; i ++){
			cache.put("key-" + i, i);
		}
		
		assertEquals(100, cache.size());
		assertEquals(null, cache.get("key-0"));
		assertEquals(null, cache.get("key-899"));
		assertEquals((Integer) 900, cache.get("key-900"));
		assertEquals((Integer) 999, cache.get("key-999"));
		assertEquals(null, cache.get("key-1000"));
	}
	
}
