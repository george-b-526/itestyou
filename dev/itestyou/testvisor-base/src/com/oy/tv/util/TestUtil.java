package com.oy.tv.util;

public class TestUtil {

	public static void fail(String msg){
		throw new RuntimeException("Assertion failed: " + msg);
	}
	
	public static void assertSame(int i, int j){
		assertSame(new Integer(i), new Integer(j), "Not same: " + i + " and " + j);
	}
	
	public static void assertSame(Object obj1, Object obj2){
		assertSame(obj1, obj2, "NOt the same: " + obj1 + " and " + obj2);
	}
	
	public static void assertSame(Object obj1, Object obj2, String msg){
		if (obj1 == obj2){
			return;
		}
		if (obj1 == null && obj2 != null){
			fail(msg);
		}
		if (obj2 == null && obj1 != null){
			fail(msg);
		}
		if (!obj1.equals(obj2)){
			fail(msg);
		}
	}
	
}
