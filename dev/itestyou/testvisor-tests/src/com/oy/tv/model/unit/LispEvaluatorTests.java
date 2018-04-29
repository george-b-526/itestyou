package com.oy.tv.model.unit;

import junit.framework.TestCase;
import net.sf.yacas.CYacas;
import net.sf.yacas.YacasEvaluatorEx;

import com.oy.tv.ns.ResourceLocator;

public class LispEvaluatorTests extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		YacasEvaluatorEx.init("E:/dev/eclipse-root/testvisor-lisp/dist/" + 
				ResourceLocator.getCommonAlgebraLangPackJarName()
		);
	}

	public static String eval(String input) throws Exception {
		return eval(input, "en");
	}
	
	public static String eval(String input, String locale) throws Exception {
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {
			UnitProcessor up = new UnitProcessor();
		  	
			up._ctx = new UnitContext();
			up._log = System.out;
			up._eval = eval;
			
			up._eval.setLocale(locale);
			String result = up.echo(input);

			YacasEvaluatorEx.leaseComplete(eval);

			return result.trim();
		} catch (Exception e){
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException("Failed to render unit.", e);
		}		
	}
	
	public void testNTimes() throws Exception {
		assertEquals("\"5x5x5x5x5x\"", eval("RepeatNTimes(\"5x\", 5)"));
		assertEquals("\"5x5x5x5x5x6\"", eval("RepeatNTimes(\"5x\", 5, \"6\")"));
	}
	
	public void testBasics() throws Exception {
		assertEquals("5", eval("3 + 2"));
	}
	
	public void testSimplify() throws Exception {
		assertEquals("2*x", eval("Simplify(x + x)"));
	}

	public void testLocalization() throws Exception {
		assertEquals("FIVE", eval("FormatE(5)"));
		assertEquals("ПЯТЬ", eval("FormatE(5)", "ru"));		
	}
	
	public void testRussianNumbers() throws Exception {
		YacasEvaluatorEx.flushLeased();
		assertEquals("ПЯТЬ", eval("FormatRuE(5)"));
		
		CYacas.ENABLE_UTF_8_SUPPORT = false;
		YacasEvaluatorEx.flushLeased();
		assertEquals("ÐÐ¯Ð¢Ð¬", eval("FormatRuE(5)"));
	}
}
