package com.oy.tv.model.words.gre;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;

public class GreVocabulary {

	/*
		Source (Barrons GRE Wordlist 4,759 words):
			http://quizlet.com/47571/barrons-gre-wordlist-4759-words-flash-cards/
	 */
	
	public static void main (String [] args) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File(
				"src/com/oy/tv/model/words/gre/gre.txt")));		

		StringBuffer sb = new StringBuffer();
    while(true){
    	String line = in.readLine();
    	if (line == null) break;
    	sb.append(line + "\n");
    }		
    
    WordSet satWs = WordRepository.fromXML(
    		new FileInputStream(new File("src/com/oy/tv/model/words/sat-5000.xml")));    
    Map<String, Word> map = new HashMap<String, Word>();
    for (Word word : satWs.getWords()){
    	map.put(word.getWord(), word);
    }
    
    List<Word> words = new ArrayList<Word>();

  	int t = 0;
    String [] lines = sb.toString().split("\n");
    for (int i=0; i <lines.length; i++){
    	String [] parts = lines[i].split("\t");
    	
    	if (parts.length != 2){
    		throw new RuntimeException("Bad line: " + lines[i]);
    	}
    	
    	Word word = new Word();
    	word.setWord(parts[0].trim());

    	String def = parts[1].trim();
    	
    	// normalize
    	def = def.replaceAll("[N][:]", "N.");
    	def = def.replaceAll("[V][:]", "V.");
    	def = def.replaceAll("[A][D][J][:]", "ADJ.");
    	def = def.replaceAll("[C][f][.]", "CF.");

    	// remove
    	def = def.replaceAll("[N][.]", "");
    	def = def.replaceAll("[E][x][.]", "");
    	def = def.replaceAll("[A][D][V][.]", "");
    	def = def.replaceAll("[C][F][.]", "");
    	def = def.replaceAll("[O][P][.]", "");
    	def = def.replaceAll("[A][D][J][.]", "");
    	def = def.replaceAll("[V][.]", "");
    	def = def.replaceAll("[P][L][.]", "");

    	for (int j=0; j < def.length(); j++){
    		char c = def.charAt(j);
    		if (('A' <= c && c <= 'Z')){
    			if (c == 'I'){
    				continue;
    			}
    			//System.err.println("Bad def (" + c + "): " + def);
    			break;
    		}
    	}
    	
    	// quotes ``>>"
    	def = def.replaceAll("[`][`]", "\"");
    	def = def.replaceAll("['][']", "\"");
    	
    	// esp. >> especially
    	def = def.replaceAll("[e][s][p][.][ ]", "especially ");
    	    	
    	// clean up
    	def = def.replaceAll("[;][;]", ";");
    	def = def.replaceAll("[ ][ ]", " ");
    	
    	// trailing ' or ;
    	if (def.endsWith(";") || def.endsWith(".")){
    		def = def.substring(0, def.length() - 1);
    	}
    	
    	word.setDefinition(def);
    	
    	Word w = map.get(word.getWord());
    	if (w != null){
    		word.setType(w.getType());
    		t++;
    	}
    	
    	words.add(word);
    }
  	System.out.println("Resolved: " + t);
    
    WordRepository.toXML(
    		"GRE 5000", words, new File("d:/tmp/barrons-gre-5000.xml"), true);
	}
	
	
}
