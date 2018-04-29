package com.oy.tv.model.words.toefl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class ToelfMain {

	public static void main(String [] args) throws Exception {
			BufferedReader in = new BufferedReader(new FileReader(new File(
					"src/com/oy/tv/model/words/toefl/toefl.txt")));		

			StringBuffer sb = new StringBuffer();
	    while(true){
	    	String line = in.readLine();
	    	if (line == null) break;
	    	sb.append(line + "\n");
	    }		

	    String [] lines = sb.toString().split("\n");
	    
	    List<Word> words = new ArrayList<Word>();
	    for (String line : lines){
	    	line = line.trim();
	    	if (line.length() < 2){
	    		continue;
	    	}
	    	
	    	Word word = new Word();
	    	
	    	
	    	StringNavigator sn = new StringNavigator(line);
	    	
	    	try {
  	    	sn.next(". ");
  	    	sn.next(". ");
	    	} catch (Exception e){
	    		System.err.println("Bad line: " + line);
	    		continue;
	    	}
	    	
	    	String m = sn.next();
	    	
	    	String term = sn.prev();
	    	int idx = term.lastIndexOf(" ");
	    	String e = term.substring(0, idx); 
	    	String t = term.substring(idx + 1);
	    	
	    	if ("v".equals(t) || "adj".equals(t) || "n".equals(t) || "adv".equals(t)){
	    		
	    	} else {
	    		System.err.println("Bad type: " + line);
	    		continue;
	    	}
	    	
	    	// remove ending '.'
	    	if (m.endsWith(".")){
	    		m = m.substring(0, m.length() - 1);
	    	}

	    	word.setWord(e.toLowerCase());
	    	word.setDefinition(m);
	    	word.setType(t);
	    	
	    	words.add(word);
	    }
	    
	    WordRepository.toXML(
	    		"TOEFL 2000", words, new File("src/com/oy/tv/model/words/toefl/toefl.xml"), true);
	}
	
}
