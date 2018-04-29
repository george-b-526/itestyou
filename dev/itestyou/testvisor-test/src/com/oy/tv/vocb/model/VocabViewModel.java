package com.oy.tv.vocb.model;

import java.io.Serializable;

public class VocabViewModel implements Serializable {
	static final long serialVersionUID = 0;
	    
	public VocabViewModel(String activityId){
		this.activityId = activityId;
	} 
	          
	public String referer;				// referrer URL
	public String activityId;			// unique id of this object instance for tracking  
	public int seed;							// random shuffle seed
	public long createdOn;				// time stamp of the last update
	public int unitId;						// unit id for this vocabulary			
	public int [] wordIds;				// all words in this test
	public int answerIdx;					// correct answer index in wordIds list
	public int mode;							// controls meaning2word, or word2meaning for next challenge
																// 0 - random, 1 - meaning2word, 2 - word2meaning 
	public boolean inv;						// if true meaning2word, else word2meaning
	public int lastVariationId;		// id/index of word shown last time
	
	public int correct;						// number of correct answers in session
	public int incorrect;					// number of incorrect answers in session
	public int intrstclIndex;			// every time we show intrasticial this number goes up by one
	
	public boolean isInverted(){
		return inv;
	}
	
	public int getMode(){
		return mode;
	}
	
	public String getReferer() {
  	return referer;
  }
	
	public String getActivityId() {
  	return activityId;
  }
	
	public int getSeed() {
  	return seed;
  }
	
	public long getCreatedOn() {
  	return createdOn;
  }

	public int getUnitId() {
  	return unitId;
  }

	public int getLastVariationId() {
		return lastVariationId;
	}
	
	public int getVariationId() {
		if (wordIds != null && answerIdx >= 0 && answerIdx < wordIds.length){
	  	return wordIds[answerIdx];
		} else {
			return -1;
		}
  }

}
