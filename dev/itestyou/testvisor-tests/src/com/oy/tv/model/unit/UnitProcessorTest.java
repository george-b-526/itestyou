package com.oy.tv.model.unit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.yacas.CYacas;
import net.sf.yacas.YacasEvaluatorEx;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.schema.core.VariationBO;
import com.oy.tv.util.StreamHelper;
import com.oy.tv.util.XmlUtil;

public class UnitProcessorTest extends DAOTestBase {
  
	public UnitProcessorTest(){
		super(AllDAO.NS_DEFAULT);
	}	
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		init();
	}

	public static void init() throws Exception {
		YacasEvaluatorEx.init("../testvisor-lisp/dist/" + 
				ResourceLocator.getCommonAlgebraLangPackJarName()
		);
	}
	
	public static UnitProcessor eval(String xml, String values) throws Exception {
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {
			UnitProcessor up = new UnitProcessor();
		  	
			up._doc = XmlUtil.loadXmlFrom(xml);
			up._ctx = new UnitContext();
			up._rtx = new RenderContext();
			up._rtx.showAnswer = true;
			up._ctx.values = values;
			up._log = System.out;
			up._eval = eval;
	
			up.evaluate();

			YacasEvaluatorEx.leaseComplete(eval);

			return up;
		} catch (Exception e){
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException("Failed to render unit.", e);
		}		
	}
	
	public void testAnswerCanBeFirst() throws Exception {
		String xml = StreamHelper.fetch(new File(
				"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/201.xml"));
		UnitProcessor up = eval(xml, "{1,2,3}");
		
		assertEquals(null, up._ctx.errors);
		assertEquals(1, up._ctx.answerIndexes.size());
		assertTrue(up._ctx.answerIndexes.contains(0));
	}
	
	public void testAnswerCanBeNotFirst() throws Exception {
		String xml = StreamHelper.fetch(new File(
				"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/202.xml"));
		UnitProcessor up = eval(xml, "{1,2}");

 		assertEquals(null, up._ctx.errors);
		assertEquals(1, up._ctx.answerIndexes.size());
		assertTrue(up._ctx.answerIndexes.contains(2));
	}
	
	public void testValueCountMustMathBinding() throws Exception {
		String xml = StreamHelper.fetch(new File(
				"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/202.xml"));
 		UnitProcessor up = eval(xml, "{1,2,3,4,5,6}");

 		assertEquals(1, up._ctx.errors.size());
	}
	
	public void testShufflePreservesAnswer() throws Exception {
		String xml = StreamHelper.fetch(new File(
				"E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/unit/202.xml"));
		UnitProcessor up = eval(xml, "{1,2}");

		int choiceIndex = up._ctx.answerIndexes.iterator().next();
		String choice = up._ctx.choices.get(choiceIndex);
		List<String> choices = new ArrayList<String>();
		choices.addAll(up._ctx.choices);
		
		Map<Integer, Integer> new2old = new HashMap<Integer, Integer>();
		UnitContext.shuffleEx(up._ctx, new2old, new Random(12345));
		
		int newChoiceIndex = up._ctx.answerIndexes.iterator().next();
		String newChoice = up._ctx.choices.get(newChoiceIndex);
		 
		assertEquals(choice, newChoice);
		assertTrue(new2old.get(newChoiceIndex) == choiceIndex);
		assertSameInAnyOrder(choices, up._ctx.choices);
	}
	
	private void assertSameInAnyOrder(Collection<String> a, Collection<String> b){
		for (Object o : a){
			assertTrue(b.contains(o));
		}
		for (Object o : b){
			assertTrue(a.contains(o));
		}
	}	

	public static UnitContext evalUnit(UnitBO unit) throws Exception {
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {
  		UnitContext ctx = new UnitContext();		
  		UnitProcessor up = new UnitProcessor();
  		
  		up._doc = XmlUtil.loadXmlFrom(unit.getXml());
  		up._pp = null;
  		up._ctx = ctx;
  		up._ctx.values = up._doc.getDocumentElement().getAttribute("defaults");
  		up._log = System.out;
  		up._eval = eval;
  					    
  		up.evaluate();  
  		
  		YacasEvaluatorEx.leaseComplete(eval);
  		
  		return ctx;
  	} catch (Exception e){
  		YacasEvaluatorEx.leaseFail(eval);
  		throw new RuntimeException(e);
  	}		
	}

	private void testEval(UnitBO unit) throws Exception {
		CYacas.ENABLE_UTF_8_SUPPORT = false;
		YacasEvaluatorEx.flushLeased();
		UnitContext ctx1 = evalUnit(unit);
		
		CYacas.ENABLE_UTF_8_SUPPORT = true;
		YacasEvaluatorEx.flushLeased();
		UnitContext ctx2 = evalUnit(unit);
		
		assertEquals(ctx1.question, ctx2.question);
		assertEquals(ctx1.choices.size(), ctx2.choices.size());
		for (int i=0; i < ctx1.choices.size(); i++) {
			assertEquals(ctx1.choices.get(i), ctx2.choices.get(i));
		}
	}
	
	private void testParsing(UnitBO unit) throws Exception {
		VariationBO var = VariationDAO.loadRndVariation(db, unit);
		UnitProcessor up = UnitProcessorTest.eval(unit.getXml(), var.getValues());
		
 		assertEquals(null, up._ctx.errors);
		assertEquals(1, up._ctx.answerIndexes.size());
		assertTrue(up._ctx.answerIndexes.size() != 0);		
	}
	
	private TranslationContext testIdenityWeaving(UnitBO unit) throws Exception {
		TranslationContext ctx = TranslationWeaver.extractTranslatable(unit); 
		ctx.resetToOriginal(); 		
		UnitBO weaved = TranslationWeaver.weaveTranslatable(unit, ctx);
		
		assertEquals(0, ctx.weaveErrors.size());
		assertEquals(0, ctx.parseErrors.size());

		String xml1 = TranslationWeaver.renderToXml(XmlUtil.loadXmlFrom(unit.getXml())); 
		String xml2 = TranslationWeaver.renderToXml(XmlUtil.loadXmlFrom(weaved.getXml()));

		assertEquals(TranslationWeaver.removeWhiteSpace(xml1), 
				TranslationWeaver.removeWhiteSpace(xml2));		
	
		return ctx;
	}

	private void testTranslation(TranslationContext ctx, UnitBO unit, String lang) throws Exception {
		TranslationBO tln = TranslationDAO.get(db, lang, unit);
		if (tln != null && tln.getData() != null) {
			System.out.print("+" + lang);
			
  		ctx.resetToOriginal();
			TranslationContext.updateFromXml(ctx, tln.getData());  			
			TranslationWeaver.weaveTranslatable(unit, ctx);

			assertEquals(0, ctx.weaveErrors.size());
  		assertEquals(0, ctx.parseErrors.size());
		}
	}
	
	public void testAllUnitsAreParsable() throws Throwable {
		UserBO owner = new UserBO();
		owner.setId(1);
		
		PagedKeyList pkl = UnitDAO.getAllPaged(db, owner, Integer.MAX_VALUE, 0);
		assertEquals(487, pkl.ids.size());
		
		for (int i=0; i < pkl.ids.size(); i++){			
			int id = pkl.ids.get(i);
						
			UnitBO unit = UnitDAO.loadUnit(db, id);
			if (unit.getType() != 1){
				continue;
			}			
			
			try {
				System.out.print("Parsing math " + id + "...");
				
				testParsing(unit);
				if (false) {
					testEval(unit);
				}

				TranslationContext ctx = testIdenityWeaving(unit);				
				
  			testTranslation(ctx, unit, TranslationDAO.LANG_NL);
  			testTranslation(ctx, unit, TranslationDAO.LANG_RU);
  			testTranslation(ctx, unit, TranslationDAO.LANG_ZH);
  			testTranslation(ctx, unit, TranslationDAO.LANG_ES);
			} catch (Throwable t){
				System.out.println("Unit failed: " + id + ", i=" + i);
				throw t;
			}
			
			System.out.println(" Done!");
		}
	}	

  public static interface Apply<T> {
  	public void apply(T t) throws Exception;
 
  	public static class ForEach<T> {
  		public void apply(int threadCount, List<T> items, final Apply<T> applicator)
  				throws Exception {
  	  	ExecutorService executor = Executors.newFixedThreadPool(threadCount,
  					Executors.defaultThreadFactory());
  			List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
  			for (final T item : items) {
  				tasks.add(new Callable<Void>() {
  					public Void call() throws Exception {
  						applicator.apply(item);
  						return null;
  					}
  				});
  			}
  			for (Future<Void> future : executor.invokeAll(tasks)) {
  				future.get();
  			}  			
  			executor.shutdown();
  		}
  	}
  }

}
