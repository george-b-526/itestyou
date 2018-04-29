package com.oy.tv.vocb.view;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;

import com.oy.tv.dao.DAOTestBase;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.UnitDAO.UnitType;
import com.oy.tv.dao.runtime.AllDAO;
import com.oy.tv.dao.runtime.ResponseDAO;
import com.oy.tv.dao.runtime.ResponseTests;
import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.vocb.model.VocabViewModel;

public class VocabViewTests extends DAOTestBase {

	private final static String ADMIN_NS = "VocabViewTests_ITY_ADMIN";
	private final static String RUNTIME_NS = "VocabViewTests_ITY_RUNTIME";
	
	private LegacySelector selector;
	
	public VocabViewTests() {
		super(RUNTIME_NS, new AllDAO());
		clearAllData(ADMIN_NS, new com.oy.tv.dao.core.AllDAO());
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		selector = new LegacySelector(null, null, 0);
	}
	
	private void assertSameType(WordSet ws, int [] ids, String type){
		for (int id : ids){
			Word word = ws.getWords().get(id);
			if (!type.equals(word.getType())){
				throw new RuntimeException(type + " vs " + word.getType());
			}
		}
	}
	
	public void testPickDecoys() throws Exception {
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/sat-words.xml");

		VocabViewContext vctx = new VocabViewContext();
		vctx.ws = WordRepository.fromXML(new FileInputStream(file));
		
		List<Integer> fail = new ArrayList<Integer>();
		int [] ids;  
		
		// default
		vctx.word = vctx.ws.getWords().get(0); 
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{4625, 281, 3323, 213, 3665};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());
		
		// not pick challenge word
		vctx.word = vctx.ws.getWords().get(3323); 
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{1918, 281, 1607, 3665, 386};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());
		
		// force failed item
		vctx.word = vctx.ws.getWords().get(3593); 
		fail.add(230);
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{1607, 230, 308, 1918, 213};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());

		// force several failed items
		vctx.word = vctx.ws.getWords().get(3593); 
		fail.add(230); fail.add(231); fail.add(233); fail.add(234); fail.add(235);
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{281, 230, 234, 308, 3323};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());
		
		// only failed are picked
		vctx.word = vctx.ws.getWords().get(3593); 
		fail.add(230); fail.add(231); fail.add(233); fail.add(234); fail.add(235);
		fail.add(4807); fail.add(4810); fail.add(4811); fail.add(4815); fail.add(4816);
		selector.pickDecoys(vctx, fail, new Random(3));
		ids = new int []{4810, 4807, 231, 234, 4815};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());
		
		// force words not of the same type
		vctx.word = vctx.ws.getWords().get(3593); 
		vctx.word.setType("type1");
		vctx.ws.getWords().get(250).setType("type1"); 
		vctx.ws.getWords().get(280).setType("type1");
		vctx.ws.safeIndex();
		
		fail.add(230); fail.add(240); fail.add(250); fail.add(260); fail.add(270);
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{3518, 250, 205, 280, 2804};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		try {
  		assertSameType(vctx.ws, ids, vctx.word.getType());
  		fail();
		} catch (Exception e){}
}
	
	public void testSameValueDecoysAreNotPicked() throws Exception {
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/same-defs.xml");

		VocabViewContext vctx = new VocabViewContext();
		vctx.ws = WordRepository.fromXML(new FileInputStream(file));
		
		List<Integer> fail = new ArrayList<Integer>();
		int [] ids;
		
		// none of the duplicates should be picked up
		vctx.word = vctx.ws.getWords().get(0); 
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{5, 7, 9, 6, 8};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		assertSameType(vctx.ws, ids, vctx.word.getType());
	}

	public void testPickBadTypeIfHaveTo() throws Exception {
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/mixed-type.xml");

		VocabViewContext vctx = new VocabViewContext();
		vctx.ws = WordRepository.fromXML(new FileInputStream(file));
		
		List<Integer> fail = new ArrayList<Integer>();
		int [] ids;
		
		// none of the duplicates should be picked up
		vctx.word = vctx.ws.getWords().get(0); 
		selector.pickDecoys(vctx, fail, new Random(1));
		ids = new int []{5, 6, 7, 10, 8};
		Assert.assertArrayEquals(ids, vctx.decoyIdxs);
		try {
  		assertSameType(vctx.ws, ids, vctx.word.getType());
  		fail();
		} catch (Exception e){}
	}
	
	public void testPickNewChallengeWord() throws Exception {
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/sat-words.xml");
		WordSet ws = WordRepository.fromXML(new FileInputStream(file));
		
		Set<Integer> pass = new HashSet<Integer>();
		List<Integer> fail = new ArrayList<Integer>();
		VocabViewContext vctx = new VocabViewContext();
		vctx.ws = ws;
		
		// default
		selector.pickNewChallengeWord(vctx, pass, fail, -1, new Random(1));
		assertEquals(1607, vctx.word.getId());

		// repeat 
		selector.pickNewChallengeWord(vctx, pass, fail, -1, new Random(1));
		assertEquals(1607, vctx.word.getId());

		// previous failed is not picked
		selector.pickNewChallengeWord(vctx, pass, fail, 1607, new Random(1));
		assertEquals(1103, vctx.word.getId());
		
		// if too many fails, next challenge picked from the failures
		fail.clear();
		for (int i=0; i <= LegacySelector.REPEAT_AFTER; i++){
			fail.add(i);
		}
		selector.pickNewChallengeWord(vctx, pass, fail, -1, new Random(1));
		assertEquals(2, vctx.word.getId());
		Assert.assertArrayEquals(new Object [] {0, 1, 3}, fail.toArray());
		
		// too many failures, but don't pick last picked
		fail.clear();
		for (int i=0; i <= LegacySelector.REPEAT_AFTER; i++){
			fail.add(i);
		}
		selector.pickNewChallengeWord(vctx, pass, fail, 2, new Random(1));
		assertEquals(0, vctx.word.getId());
		Assert.assertArrayEquals(new Object [] {1, 2, 3}, fail.toArray());
	}

	
	public void testSatWords() throws Exception {
		WordSet in = WordRepository.load();
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/sat-words.xml");
		WordSet out = WordRepository.fromXML(new FileInputStream(file));

		assertEquals(in.getWords().size(), out.getWords().size());
		for (int i=0; i < in.getWords().size(); i++){
			Word from = in.getWords().get(i);
			Word to = out.getWords().get(i);
			
			assertEquals(from.getId(), to.getId());
			assertEquals(from.getWord(), to.getWord());
			assertEquals(from.getDefinition(), to.getDefinition());
			assertEquals(from.getType(), to.getType());
		}
	}
	
	public void testEncoding(){
		String encoded = VocabRenderer.encodeDefinition("I don't care");
	
		assertEquals("I don&#39;t care", encoded);
	}
	
	public void testMd5 (){
		assertEquals("62bc0c9270f81bf14df0f2bd2d09fade", VocabRenderer.md5("HI, I am Md5!"));
	}
	
	public void testGetResult(){
		VocabViewModel model = new VocabViewModel(null);

		model.wordIds = new int [] {11, 22, 33, 44, 55, 66};
		model.answerIdx = 3;
		
		{
  		Result result = VocabView.getResult(model, "3");	
  		
  		assertEquals(result.score, ResponseDAO.Score.PASS);
  		Assert.assertArrayEquals(new int [] {}, result.fail);
  		
  		String data = VocabView.createUnitData(123, false, result);
  		assertEquals("ep={123}\nmp={123}", data);
		}
		
		{
  		Result result = VocabView.getResult(model, "0;1;2;4;5");	
  		
  		assertEquals(result.score, ResponseDAO.Score.FAIL);
  		Assert.assertArrayEquals(new int [] {11, 22, 33, 55, 66}, result.fail);

  		String data = VocabView.createUnitData(123, false, result);
  		assertEquals("ef={123}\nmf={11,22,33,55,66}", data);
		}

	}
	
	public void testGetWordsToLearn(){
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(2);
		ids.add(3);
		
		assertEquals(ids,
				VocabView.getWordsToLearn("{1:1,2:-1,3:0}"));
	}
	
	private String data(int challengeWordId, int failedWordId){
		Result result = new Result();
		result.fail = new int []{failedWordId};
		result.score = ResponseDAO.Score.FAIL;
		String data = VocabView.createUnitData(challengeWordId, false, result);
		return data;
	}
	
	
	public void testAccumulateJoin(){
		TimePeriodDiff tpd = new TimePeriodDiff();
		List<Integer> pass = new ArrayList<Integer>();
		List<Integer> fail = new ArrayList<Integer>();
		
		tpd.accumulate("{1:0,2:1,3:2}");
		tpd.accumulate("{1:-1,2:-1,3:-1}");
		tpd.partition(pass, fail);

		Collections.sort(fail);
		Assert.assertArrayEquals(new Object []{3}, pass.toArray());
		Assert.assertArrayEquals(new Object []{1, 2}, fail.toArray());
		
		pass.clear();
		fail.clear();
		tpd.accumulate("{1:-1,2:-1,3:-1}");
		tpd.partition(pass, fail);

		Collections.sort(fail);
		Assert.assertArrayEquals(new Object []{}, pass.toArray());
		Assert.assertArrayEquals(new Object []{1, 2, 3}, fail.toArray());

	
		pass.clear();
		fail.clear();
		tpd.accumulate("{1:3,2:4,3:5}");
		tpd.partition(pass, fail);

		Collections.sort(pass);
		Assert.assertArrayEquals(new Object []{1, 2, 3}, pass.toArray());
		Assert.assertArrayEquals(new Object []{}, fail.toArray());
	}
	
	public void testGetWordsToLearnLooksUpLastFourWeeksTillFindsFirstAvailable() throws Exception {
		Date week0 = new Date(Date.parse("2012/01/14 1:00 PM"));
		Date week1 = new Date(Date.parse("2012/01/7 11:00 AM"));
		Date week2 = new Date(Date.parse("2011/12/31 2:00 PM"));

		UserBO user = new UserBO();
		user.setId(1);
		
		db.execSelect("USE " + ADMIN_NS + ";");
		UnitBO unit = UnitDAO.addUnit(db, user);
		UnitDAO.updateUnit(db, unit, "", "", 0, UnitType.VOCB.ordinal(), EObjectState.ACTIVE);
		db.execSelect("USE " + RUNTIME_NS + ";");
		
		List<Integer> fail = new ArrayList<Integer>();
		List<Integer> pass = new ArrayList<Integer>();
		
		// finds data for the active week
		{
			ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), 100, week2, week2, 
					ResponseDAO.Score.FAIL, "", "", "", "", "", data(100, 200), TranslationDAO.LANG_EN);
  		ResponseTests.completAllQueuedWork(db, ADMIN_NS, RUNTIME_NS);
  
  		TimePeriodDiff tpd = selector.getPlayHistory(db, RUNTIME_NS, 
  				week2, user.getId(), unit.getId());
  		fail.clear();
  		pass.clear();
  		tpd.partition(pass, fail);
  		
  		Collections.sort(fail);
  		Assert.assertArrayEquals(new Object [] {100, 200}, fail.toArray());
		}
		
		// move to the next week and confirm we can see same words
		{
			TimePeriodDiff tpd = selector.getPlayHistory(db, RUNTIME_NS, 
					week1, user.getId(), unit.getId());
  		fail.clear();
  		pass.clear();
  		tpd.partition(pass, fail);
  		
  		Collections.sort(fail);
			Assert.assertArrayEquals(new Object [] {100, 200}, fail.toArray());			
		}

		// move to the next week and confirm we can see same words
		{
			TimePeriodDiff tpd = selector.getPlayHistory(db, RUNTIME_NS, 
					week0, user.getId(), unit.getId());
  		fail.clear();
  		pass.clear();
  		tpd.partition(pass, fail);
  		
  		Collections.sort(fail);
  		Assert.assertArrayEquals(new Object [] {100, 200}, fail.toArray());			
		}

		// add fresh votes to current week and look up should stop
		{
  		ResponseDAO.enqueue(db, RUNTIME_NS, user.getId(), unit.getId(), 300, week0, week0, 
  				ResponseDAO.Score.FAIL, "", "", "", "", "", data(300, 400), TranslationDAO.LANG_EN);
  		ResponseTests.completAllQueuedWork(db, ADMIN_NS, RUNTIME_NS);

			TimePeriodDiff tpd = selector.getPlayHistory(db, RUNTIME_NS,
					week0, user.getId(), unit.getId());
  		fail.clear();
  		pass.clear();
  		tpd.partition(pass, fail);
  		
  		Collections.sort(fail);
  		Assert.assertArrayEquals(new Object [] {100, 200, 300, 400}, fail.toArray());			
		}

	}
}
