package com.oy.tv.model.words.dicts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.InputSource;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.util.StringNavigator;

public class Research {

	
	public static void main(String [] args) throws Exception {
		String index = Parse.load("index.php");
		
		Map<String, String> langs = new HashMap<String, String>(); {
  		StringNavigator sn = new StringNavigator(index);
  		sn.next("<form name=\"search\"");
  		sn.next("<select ");
  		while(sn.tryNext("<option ")){
  			sn.next("value=\"");
  			sn.next("\"");
  			String ns = sn.prev().trim();
  			sn.next("</option>");
  			String name = sn.prev().trim();
  			
  			langs.put(ns, name);
  		}
		}
				
		for (String ns : langs.keySet()){
			// if (!ns.equals("ukrainian")) continue;
			if (ns.equals("english")) continue;
			//if (!ns.equals("esperanto")) continue;

			Reader in = new InputStreamReader(new FileInputStream(new File(Parse.HOME + "xml/" + ns + ".xml")), "UTF-8");
	    WordSet ws = WordRepository.fromXML(new InputSource(in));

	    Map<String, Integer> types = new HashMap<String, Integer>();
	    Map<String, Word> full = new HashMap<String, Word>();
	    Map<String, Word> term = new HashMap<String, Word>();
			int i=0, j=0;
	    for (Word word: ws.getWords()){
	    	Integer t = types.get(word.getType());
	    	if (t == null){
	    		t = 0;
	    	}
	    	types.put(word.getType(), t + 1);
	    	
	    	Word other = term.get(word.getWord()); 
	    	if (other != null){
	    		i++;
	    		//System.out.println("Dup: " + word.getId() + " and " + other.getId());
	    	} else {
	    		term.put(word.getWord(), word);
	    	}
	    	
	    	String _full = word.getWord() + "\n" + word.getDefinition();
	    	other = full.get(_full);
	    	if (other != null){
	    		j++;
	    		//System.out.println("Full Dup: " + word.getId() + " and " + other.getId());
	    	} else {
	    		full.put(_full, word);
	    	}
	    }
	    System.out.println(ns.toUpperCase() + ": words=" + ws.getWords().size() +  ", pdups=" + i + ", fdups=" + j + ", types=" + types.toString());
		}
	
	}
			
}
