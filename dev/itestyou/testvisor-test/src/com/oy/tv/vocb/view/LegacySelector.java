package com.oy.tv.vocb.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.oy.tv.dao.local.UnitCache;
import com.oy.tv.dao.runtime.QueueProcessorThread;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.AnyResultSetContext;
import com.oy.tv.model.vocb.Word;

public class LegacySelector implements IChallengeSelector {
	
	private VocabViewContext vctx = new VocabViewContext();
	private Random rand;
	private AnyDB db;
	private int userId;
	
	LegacySelector(Random rand, AnyDB db, int userId){
		this.rand = rand;
		this.db = db;
		this.userId = userId;
	}
	
	public VocabViewContext newChallenge(int unitId, int mode, int lastWordId) {
		// get the word set
		getWordSet(db, vctx, unitId);

		// figure out pass and fail from current and previous weeks
		Set<Integer> pass = new HashSet<Integer>();
		List<Integer> fail = new ArrayList<Integer>();
		classifyWords(db, vctx, userId, unitId, pass, fail);
				
		// pick new challenge word
		pickNewChallengeWord(vctx, pass, fail, lastWordId, rand);
				
		// pick a list of decoys
		pickDecoys(vctx, fail, rand);
		
		// set right answer
		vctx.answerIdx = rand.nextInt(vctx.decoys.length);
		vctx.decoys[vctx.answerIdx] = vctx.word;
		vctx.decoyIdxs[vctx.answerIdx] = vctx.word.getId();
		
		// choose direction for mode
		if (mode == 1){
			vctx.inv = true;
		} else {
			if (mode == 2){
				vctx.inv = false;
			} else {
				if (mode == 0){
					vctx.inv = rand.nextBoolean();
				} else {
					throw new RuntimeException("Unknown mode.");
				}
			}				
		}
		
		return vctx;
	}

	void getWordSet(AnyDB db, VocabViewContext vctx, int unitId) {
		vctx.ws = UnitCache.getWordSet(db, unitId);
  }

	void classifyWords(AnyDB db, VocabViewContext vctx, 
			int userId, int unitId, Collection<Integer> pass, Collection<Integer> fail){
		TimePeriodDiff tpd = getPlayHistory(db, new Date(), userId, unitId);
		tpd.partition(pass, fail, vctx.ws);		
	}

	TimePeriodDiff getPlayHistory(AnyDB db, Date now, int userId, int unitId){
		return getPlayHistory(db, "ITY_RUNTIME", now, userId, unitId);
	}
	
	TimePeriodDiff getPlayHistory(AnyDB db, String ns, Date now, int userId, int unitId){
		TimePeriodDiff tpd = new TimePeriodDiff();
		try {
			AnyResultSetContext result = QueueProcessorThread.getMostRecentRowsFor(
  				db, ns, userId, unitId, HISTORY_WEEK_COUNT_FOR_MERGE);
			try {
				int i=0;
    		while (result.rs().next() && i <= HISTORY_WEEK_COUNT_FOR_MERGE){
    			String data = result.rs().getString("UNIT_DATA");
    			if (data != null && data.trim().length() !=0){
    				tpd.accumulate(data);
    			}
  				i++;
    		}
			} finally {
				result.close();
			}			
		} catch (SQLException e){
			e.printStackTrace();
		}
		return tpd;
	}

	void pickNewChallengeWord(VocabViewContext vctx, Set<Integer> pass, List<Integer> fail, 
			int lastWordId, Random rnd){
		int idx;
		if (fail.size() >= REPEAT_AFTER){
			
			// pick word out of failed words if we failed too many times
			// don't repeat the last one 
			int index;
			int i=0;
			do {
				index = rnd.nextInt(fail.size());
				if (fail.get(index) != lastWordId){
					break;
				}
				i++;
			} while (i < MAX_PICK_TRIES);

			idx = fail.get(index);
			fail.remove(index);
		} else {
			
			// pick new word out of all words, filter out words we have seen
			int i=0;
			do {
				idx = rnd.nextInt(vctx.ws.getWords().size());
				if (!pass.contains(vctx.ws.getWords().get(idx).getId()) && 
						vctx.ws.getWords().get(idx).getId() != lastWordId){
					break;
				}
				i++;
			} while (i < MAX_PICK_TRIES);			
		}
		
		vctx.word = vctx.ws.getWords().get(idx);
	}
	
	void pickDecoys(VocabViewContext vctx, List<Integer> fail, Random srnd){
		final int DECOY_COUNT = 5;

		final List<Word> decoys = new ArrayList<Word>(); 
		{
			final int MAX_DECOY_POOL_SIZE = DECOY_COUNT * 2;

  		final Set<Integer> ids = new HashSet<Integer>();
  		final Set<String> words = new HashSet<String>();
  		final Set<String> definitions = new HashSet<String>();
  		
  		class Picker {
    		void tryPick(Word word, Word decoy, boolean strict){
    			boolean sameWordValue = 
    				words.contains(decoy.getWord()) || 
    				word.getWord().equals(decoy.getWord());
    			boolean sameDefinitionValue = 
    				definitions.contains(decoy.getDefinition()) ||
    				word.getDefinition().equals(decoy.getDefinition());
    			boolean sameType = word.getType().equals(decoy.getType());
    			boolean sameAsChallendgeWord = word.getId() == decoy.getId();
    			boolean sameAsExistingDecoy = ids.contains(decoy.getId());
    		
    			boolean isLoose =     					
  					!sameWordValue && 
  					!sameDefinitionValue && 
  					!sameAsChallendgeWord && 
  					!sameAsExistingDecoy;

    			boolean isStrict = isLoose && sameType;
    			
    			if ((!strict && isLoose) || (strict && isStrict)){
    				decoys.add(decoy);

    				ids.add(decoy.getId());
    				words.add(decoy.getWord());
    				definitions.add(decoy.getDefinition());
    			}
    		}
  		}
  		
  		Picker picker = new Picker();
  		
  		// find failed words with the same type as challenge word
  		for (int id : fail){
  			if (decoys.size() >= MAX_DECOY_POOL_SIZE){
  				break;
  			}
  			Word candidate = vctx.ws.getWords().get(id); 
  			picker.tryPick(vctx.word, candidate, true);
  		}
  		    		
  		// add all other random words of the same type
  		if (decoys.size() < MAX_DECOY_POOL_SIZE){
    		List<Word> sameType = new ArrayList<Word>();
    		sameType.addAll(vctx.ws.getAllOfType(vctx.word.getType()));
    		Collections.shuffle(sameType, srnd);
    		for (int i=0; i < sameType.size(); i++){
    			if (decoys.size() >= MAX_DECOY_POOL_SIZE){
    				break;
    			}
    			Word candidate = sameType.get(i);
    			picker.tryPick(vctx.word, candidate, true);
    		}
  		}
  		
  		// add the rest of words ignoring type :(
			if (decoys.size() < DECOY_COUNT){
    		List<Word> allTypes = new ArrayList<Word>();
    		allTypes.addAll(vctx.ws.getWords());
    		Collections.shuffle(allTypes, srnd);
    		for (int i=0; i < allTypes.size(); i++){
    			if (decoys.size() >= DECOY_COUNT){
    				break;
    			}
    			Word candidate = allTypes.get(i);
    			picker.tryPick(vctx.word, candidate, false);
    		}
			}
		}

		// size
		if (decoys.size() < DECOY_COUNT){
			throw new RuntimeException("Failed to pick decoys for: answerIdx=" + vctx.answerIdx);
		}
		vctx.decoys = new Word [DECOY_COUNT];
		vctx.decoyIdxs = new int [DECOY_COUNT];

		// populate
		Collections.shuffle(decoys, srnd);
		for (int i=0; i < DECOY_COUNT; i++){
			Word decoy = decoys.get(i); 
			vctx.decoys[i] = decoy;
			vctx.decoyIdxs[i] = decoy.getId();
		}		
	}
}