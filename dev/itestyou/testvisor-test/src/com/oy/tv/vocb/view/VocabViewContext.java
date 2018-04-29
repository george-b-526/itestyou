package com.oy.tv.vocb.view;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordSet;

public class VocabViewContext {

	WordSet ws;							// set of words
	Word word;							// picked word
	Word [] decoys;					// decoy array
	int [] decoyIdxs;			  // index of words in the decoy array
	int answerIdx;					// index of the picked word in the decoy array
	boolean inv;
	
}
