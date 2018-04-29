package com.oy.tv.vocb.view;

import java.util.Random;
import com.oy.tv.db.AnyDB;

public interface IChallengeSelector {
	
	public static final int REPEAT_AFTER = 3;
	public static final int MAX_PICK_TRIES = 10;
	public static final int HISTORY_WEEK_COUNT_FOR_MERGE = 4;

	VocabViewContext newChallenge(int unitId, int mode, int lastWordId);
	
	public static class Factory {
		public static IChallengeSelector newChallengeSelector(Random rand, AnyDB db, int userId){
			return new LegacySelector(rand, db, userId);
		}
	}	
}
