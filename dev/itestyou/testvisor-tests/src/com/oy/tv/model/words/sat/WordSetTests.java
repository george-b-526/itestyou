package com.oy.tv.model.words.sat;

import junit.framework.TestCase;

import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;

public class WordSetTests extends TestCase {

	public void testEncoding() throws Exception {
		WordSet ws = WordRepository.load();
		assertEquals(5201, ws.getWords().size());
	}
	
}
