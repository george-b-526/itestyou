package com.oy.tv.model.unit;

import java.io.File;

import junit.framework.TestCase;

import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.util.StreamHelper;

public class TranslationWeaverTest extends TestCase {

	public void testUnLisp() {
		WeaveableTranslation lc = new WeaveableTranslation();
		assertEquals("foo <$0/> bar", TranslationWeaver.unlisp("foo ${1+2} bar", lc));
		assertEquals("foo $<$1/> <$2/> bar", TranslationWeaver.unlisp("foo $${1+2} ${a} bar", lc));
	}
	
	public void testTrimSpace(){
		assertEquals("foo bar", TranslationWeaver.trimSpace("foo   \t  \n\t   \t\n\t   bar"));
		assertEquals("foo bar", TranslationWeaver.trimSpace("foo\tbar"));
		assertEquals("foo bar", TranslationWeaver.trimSpace("foo\nbar"));
		assertEquals("<foo> bar", TranslationWeaver.trimSpace("<foo>     bar"));
		assertEquals("<foo>bar", TranslationWeaver.trimSpace("<foo>bar"));
		assertEquals("<foo> bar", TranslationWeaver.trimSpace("<foo>\n bar"));
		assertEquals("<foo> bar", TranslationWeaver.trimSpace("<foo>\nbar"));
	}

	public void testExtractTranslatable1() throws Exception {
		String xml = StreamHelper.fetch(new File(
    		"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/999.xml"));

		UnitBO unit = new UnitBO();
		unit.setXml(xml);
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit);

		assertEquals("The steps Quentin took to evaluate the expression <#0/> when m = <$0/> are " + 
				"shown below.<span class='box'><#1/><#2/><#3/><#4/><#5/><#6/><#7/><#8/></span> What should " + 
				"Quentin have done differently in order to evaluate the expression?", 
				ctx.getQuestion().getOriginalValue());
		assertEquals("subtracted (<$0/> <#0/> <$1/>) from <$2/>",
				ctx.choices.get(0).getOriginalValue());
	}
	
	public void testExtractTranslatable2() throws Exception {
		String xml = StreamHelper.fetch(new File(
    		"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/202.xml"));

		UnitBO unit = new UnitBO();
		unit.setXml(xml);
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit);
		
		assertEquals("Which property is used in the equation below? <#0/>",
				ctx.getQuestion().getOriginalValue());
		assertEquals("Associative Property of Addition",
				ctx.choices.get(0).getOriginalValue());
	}

	public static final String XML_HEAD = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n";
	
	public static final String XML_BODY =
		"<unit bindings=\"{a,b,c}\" defaults=\"{2,3,5}\">\r\n" +
		"<question>What the ${hell} is going <latex>on</latex> here?</question>\r\n" +
		"<choices>\r\n" +
		"<answer>this uses ${xml} and <draw>other</draw> magic</answer>\r\n" +
		"<decoy>this does not use ${fire} or <draw>ice</draw> magic</decoy>\r\n" +
		"</choices>\r\n" +
		"</unit>\r\n";

	public static final String XML_BODY_2 =
		"<unit bindings=\"{a,b,c}\" defaults=\"{2,3,5}\">\r\n" +
		"<question>What the ${hell} <span> is <br/> not</span>going <latex>on</latex> here?</question>\r\n" +
		"<choices>\r\n" +
		"<answer>this uses ${xml} and <draw>other</draw> magic</answer>\r\n" +
		"<decoy>this does not use ${fire} or <draw>ice</draw> magic</decoy>\r\n" +
		"</choices>\r\n" +
		"</unit>\r\n";
	
	public static final String XML = XML_HEAD + XML_BODY;
	public static final String XML_2 = XML_HEAD + XML_BODY_2;

	public void testWeaverWeavesNestedTag(){
		UnitBO unit = new UnitBO();	
		unit.setXml(XML_2);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
	
		assertEquals("What the <$0/> <span> is <#0/> not</span>going <#1/> here?",
				ctx.question.getOriginalValue());
		
		testWeave(unit, ctx,
				"foo <#0/> bar <br /> <$0/> <span> zee <#1/> <br />" + 
				"<br /> zoo </span> fee", 0,
				
				"<question>foo <br/> bar <br/> ${hell} <span> zee <latex>on</latex> <br/> " + 
				"<br/> zoo </span> fee</question>");
	}
	
	public void testWeaverWeavesItselfToIdentity(){
		UnitBO unit = new UnitBO();	
		unit.setXml(XML);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
		ctx.resetToOriginal();
		UnitBO result = TranslationWeaver.weaveTranslatable(unit, ctx);
		
		assertEquals(0, ctx.weaveErrors.size());
		assertTrue(TranslationWeaver.removeWhiteSpace(result.getXml()).endsWith(
				TranslationWeaver.removeWhiteSpace(XML_BODY)));
	}
	
	public void testWeaverWeavesQuestion(){
		UnitBO unit = new UnitBO();	
		unit.setXml(XML);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 

		testWeave(unit, ctx,
				"Какого происходит <#0/> черта <$0/> здесь?", 0,
				"<question>Какого происходит <latex>on</latex> черта ${hell} здесь?</question>");

		// mark up is not escaped
		testWeave(unit, ctx,
				"Which number <#0/> does <i>not</i> have <$0/> a reciprocal?", 0,
				"<question>Which number <latex>on</latex> does <i>not</i> have ${hell} a reciprocal?</question>");
						
		// test <latex/> is the only question
		testWeave(unit, ctx,
				"<latex>\\frac {${a}}{${b}} \\times ${c}=</latex>", 2,
				"<question><latex>\\frac {${a}}{${b}} \\times ${c}=</latex></question>");
				
		testWeave(unit, ctx,
				"<draw>${a} + ${b}</draw>", 2, 
				"<question><draw>${a} + ${b}</draw></question>");
		
		// weave just the inine expression by itself
		testWeave(unit, ctx,
				"<#0/>", 1,
				"<question><latex>on</latex></question>");
		
		// terminal inside <span>
		testWeave(unit, ctx,
				"<span><#0/><$0/></span>", 0,
				"<question><span><latex>on</latex>${hell}</span></question>");
		
		// terminal inside <span> with text around
		testWeave(unit, ctx,
				"baz <span> <#0/><$0/></span> foo", 0,
				"<question>baz <span><latex>on</latex>${hell}</span> foo</question>");

		// terminal inside <span> with text inside and around
		testWeave(unit, ctx,
				"baz <span> bee <#0/> zee <$0/></span> foo", 0,
				"<question>baz <span>bee<latex>on</latex> zee ${hell}</span> foo</question>");
	
		// here is &times; is encoded
		testWeave(unit, ctx,
				"foo <#0/> bar &amp;times; fee <$0/> zee", 0,
				"foo <latex>on</latex> bar &amp;times; fee ${hell} zee");

		// here is &times; raw; it fails for now and we need to fix it
		try {
  		testWeave(unit, ctx,
  				"foo <#0/> bar &times; fee <$0/> zee", 0,
  				"foo <latex>on</latex> bar &amp;times; fee ${hell} zee");
  		fail();
		} catch (Exception e){}
	}
	
	private static void testWeave(UnitBO unit, TranslationContext ctx, String translation, 
			int errorCount, String weave) {
		ctx.resetToOriginal();
		ctx.getQuestion().setNewValue(translation);
		
		UnitBO result = TranslationWeaver.weaveTranslatable(unit, ctx);
		assertEquals(errorCount, ctx.weaveErrors.size());
		
		if (weave != null) {
  		assertTrue(TranslationWeaver.removeWhiteSpace(result.getXml()).indexOf(
  				TranslationWeaver.removeWhiteSpace(weave)) != -1);
		}
	}
	
	public static void testTranslationMustHaveAllTerms(){
		UnitBO unit = new UnitBO();	
		unit.setXml(XML);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
		
		testWeave(unit, ctx,
				"Какого происходит <#0/> черта здесь?", 1, null);
		
		testWeave(unit, ctx,
				"Какого происходит черта <$0/> здесь?", 1, null);

		testWeave(unit, ctx,
				"Какого происходит черта здесь?", 2, null);
	}
	
	public void testWeaverWeavesChoices(){
		UnitBO unit = new UnitBO();	
		unit.setXml(XML);
		
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
		ctx.resetToOriginal();
		ctx.choices.get(0).setNewValue("это использует <$0/> и <#0/> магия");
		ctx.choices.get(1).setNewValue("это не используе <#0/> или <$0/> магия");
		
		UnitBO result = TranslationWeaver.weaveTranslatable(unit, ctx);
		assertEquals(0, ctx.weaveErrors.size());
		assertTrue(TranslationWeaver.removeWhiteSpace(result.getXml()).indexOf(
				TranslationWeaver.removeWhiteSpace(
				"<answer>это использует ${xml} и <draw>other</draw> магия</answer>")) != -1);
		assertTrue(TranslationWeaver.removeWhiteSpace(result.getXml()).indexOf(
				TranslationWeaver.removeWhiteSpace(
			"<decoy>это не используе <draw>ice</draw> или ${fire} магия</decoy>")) != -1);
	}	
	
	public void testUtf8() throws Exception {
		// this is the string that is from webform and looks like UTF-8 in debugger, but 
		// is trully crapped out; I took sml.getBytes() in debugger and pasted it here
		// looks like this neither UTF or ISO
		final String goal = "Какое из следующих выражений равна";
		final String badGoal = "????? ?? ????????? ????????? ?????";
		
		byte [] isoBytes = new byte [] {60, 63, 120, 109, 108, 32, 118, 101, 114, 115, 105, 111, 110, 61, 34, 49, 46, 48, 34, 32, 101, 110, 99, 111, 100, 105, 110, 103, 61, 34, 85, 84, 70, 45, 56, 34, 63, 62, 13, 10, 60, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10, 60, 113, 117, 101, 115, 116, 105, 111, 110, 62, 63, 63, 63, 63, 63, 32, 63, 63, 32, 63, 63, 63, 63, 63, 63, 63, 63, 63, 32, 63, 63, 63, 63, 63, 63, 63, 63, 63, 32, 63, 63, 63, 63, 63, 32, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 63, 60, 47, 113, 117, 101, 115, 116, 105, 111, 110, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 47, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 47, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10};
		byte [] utfBytes = new byte [] {60, 63, 120, 109, 108, 32, 118, 101, 114, 115, 105, 111, 110, 61, 34, 49, 46, 48, 34, 32, 101, 110, 99, 111, 100, 105, 110, 103, 61, 34, 85, 84, 70, 45, 56, 34, 63, 62, 13, 10, 60, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10, 60, 113, 117, 101, 115, 116, 105, 111, 110, 62, -48, -102, -48, -80, -48, -70, -48, -66, -48, -75, 32, -48, -72, -48, -73, 32, -47, -127, -48, -69, -48, -75, -48, -76, -47, -125, -47, -114, -47, -119, -48, -72, -47, -123, 32, -48, -78, -47, -117, -47, -128, -48, -80, -48, -74, -48, -75, -48, -67, -48, -72, -48, -71, 32, -47, -128, -48, -80, -48, -78, -48, -67, -48, -80, 32, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 63, 60, 47, 113, 117, 101, 115, 116, 105, 111, 110, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 99, 104, 111, 105, 99, 101, 62, 38, 108, 116, 59, 35, 48, 47, 38, 103, 116, 59, 60, 47, 99, 104, 111, 105, 99, 101, 62, 13, 10, 60, 47, 99, 104, 111, 105, 99, 101, 115, 62, 13, 10, 60, 47, 109, 97, 116, 104, 45, 116, 108, 110, 62, 13, 10};

		{
  		String iso = new String(isoBytes, "iso-8859-1");
  		String utf = new String(isoBytes, "UTF-8");
  		
  		assertTrue(iso.indexOf(goal) == -1);
  		assertTrue(utf.indexOf(goal) == -1);
  		
  		assertTrue(iso.indexOf(badGoal) != -1);
  		assertTrue(utf.indexOf(badGoal) != -1);
		}
		
		{
  		String iso = new String(utfBytes, "iso-8859-1");
  		String utf = new String(utfBytes, "UTF-8");
  		
  		assertTrue(iso.indexOf(goal) == -1);
  		assertTrue(utf.indexOf(goal) != -1);

  		assertTrue(iso.indexOf(badGoal) == -1);
  		assertTrue(utf.indexOf(badGoal) == -1);
		}
		
		
	}
}
