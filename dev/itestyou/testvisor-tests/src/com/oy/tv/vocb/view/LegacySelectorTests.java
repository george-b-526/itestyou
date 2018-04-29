package com.oy.tv.vocb.view;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import com.oy.tv.db.AnyDB;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;

public class LegacySelectorTests extends TestCase {

	class CustomSelector extends LegacySelector {
		TimePeriodDiff tpd;
		WordSet ws;
		
		CustomSelector (){
			super(new Random(12345), null, 0);
		}
		
		@Override
		void getWordSet(AnyDB db, VocabViewContext vctx, int unitId) {
			vctx.ws = ws;
	  }
		
		@Override
		TimePeriodDiff getPlayHistory(AnyDB db, String ns, Date now, int userId, int unitId){
			return tpd;
		}
	}
	
	CustomSelector selector;
	Set<Integer> learned;
	
	@Override
	public void setUp() throws Exception {
		File file = new File("E:/dev/eclipse-root/testvisor-tests/data/sat-words.xml");

		learned = new HashSet<Integer>();
		
		selector = new CustomSelector();
		selector.tpd = new TimePeriodDiff();
		selector.ws = WordRepository.fromXML(new FileInputStream(file));		
	}

	public void testMustLearnPortionBeforeExploringToNewWords() throws Exception {
		for (int i=0; i < 1000; i++){
			VocabViewContext vctx = selector.newChallenge(0, 1, -1);
			Integer count = selector.tpd.all.get(vctx.word.getId());
			if (count == null){
				count = 0;
			}
			selector.tpd.all.put(vctx.word.getId(), count - 1);
		}

		assertEquals(IChallengeSelector.REPEAT_AFTER, selector.tpd.all.size());
	}

	public void testNotAnsweringStudentExploresAllWords() throws Exception {
		int lastWordId = -1;
		for (int i=0; i < 1000; i++){
			VocabViewContext vctx = selector.newChallenge(0, 1, lastWordId);
			learned.add(vctx.word.getId());
			
			lastWordId = vctx.word.getId();
		}

		assertEquals(918, learned.size());
	}

	public void testPerfectStudentExploresAllWords() throws Exception {
		int lastWordId = -1;
		for (int i=0; i < 1000; i++){
			VocabViewContext vctx = selector.newChallenge(0, 1, lastWordId);
			
			Integer count = selector.tpd.all.get(vctx.word.getId());
			if (count == null){
				count = 0;
			}
			selector.tpd.all.put(vctx.word.getId(), count + 1);
			
			lastWordId = vctx.word.getId();
		}
		
		assertEquals(1000, selector.tpd.all.size());
	}

	void runLearningPlayerSimulation() throws Exception {
		Random rand = new Random(678);
		int lastWordId = -1;
		for (int i=0; i < 1000; i++){
			VocabViewContext vctx = selector.newChallenge(0, 1, lastWordId);
			
			Integer count = selector.tpd.all.get(vctx.word.getId());
			if (count == null){
				count = 0;
			}
			
			if (learned.contains(vctx.word.getId()) || rand.nextBoolean()){
				// guess word right is we have made mistake in it before
				selector.tpd.all.put(vctx.word.getId(), count + 2);
			} else {
				// guess one decoy, which fails both the decoy and the word
				int decoyId = rand.nextInt(vctx.decoyIdxs.length);
				selector.tpd.all.put(vctx.decoys[decoyId].getId(), count - 1);
				selector.tpd.all.put(vctx.word.getId(), count - 1);													
				
				learned.add(vctx.word.getId());
			}
			
			lastWordId = vctx.word.getId();
		}
	}
	
	public void testLearningPlayerSimulation() throws Exception {
		// run simulation
		runLearningPlayerSimulation();
		
		// the score distribution per word is not even; this is the key problem of this selector
		Plan plan = Plan.newPlan(selector.tpd, new Random(4321));
		
		// get sizes per count
		List<Integer> sizes = new ArrayList<Integer>();
		for (int count : plan.counts){
			sizes.add(plan.all.get(count).size());
		}

		// player explores about 50% of new words
		assertEquals(521, selector.tpd.all.size());

		// validate count distribution
		assertEquals("[-2, 0, 1, 2]", plan.counts.toString());
		
		// note how most of words in count 1 and 2 and -1 is only at 3, which is 
		// the limit of unknown words
		assertEquals("[1, 1, 287, 232]", sizes.toString());
	}
}