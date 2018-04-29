package com.oy.tv.model.words.ru;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class Ru {

	private static String loadFile(String ns) throws Exception {	
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
				"src/com/oy/tv/model/words/ru/most_common_words_" + ns + ".htm"), "UTF-8"));
		
		StringBuffer sb = new StringBuffer();
    while(true){
    	String line = in.readLine();
    	if (line == null) break;
    	sb.append(line + "\n");
    }		
	
    return sb.toString();
	}
    
    public static void main(String [] args) throws Exception {

    	List<Word> wordList = new ArrayList<Word>();
    	
  		int idx=1;
    	for (int i=1; i <=12; i++){
    		String s = loadFile("" + i);
    		
    		StringNavigator sn = new StringNavigator(s);
    		sn.next("<table class=\"topwords\">");
    	
    		while(true){
    			if (!sn.tryNext("<td class=\"number\">")) break;
    			sn.next(".&nbsp;</td>");
    			
    			String id = sn.prev();
    			if (!id.equals("" + idx)){
    				throw new RuntimeException("Bad id " + id);
    			}
        			
    			sn.next("<td class=\"word\">");
    			sn.next("</td>");
    			String ru = sn.prev(); 

      		StringNavigator snx = new StringNavigator(ru);
      		if (snx.tryNext("<a ")){
      			snx.next(">");
      			snx.next("</a>");
      			ru = snx.prev();
      		}
      		
      		if (ru.endsWith("&nbsp;")){
      			int k = ru.lastIndexOf("&nbsp;");
      			ru = ru.substring(0, k);
      		}
    
      		sn.next("<td>");
      		sn.next("</td>");
      		String en = sn.prev();

      		if (en.endsWith(")")){
      			snx = new StringNavigator(en);
      			snx.next("(");
      			en = snx.prev();
      		}
      		
      		sn.next("<td>");
      		sn.next("</td>");
      		String type = sn.prev();

      		// types
      		if("noun".equals(type)){
      			type = "n";
      		} else 
      		if("adjective".equals(type) || "adj".equals(type) || "adverb, conj".equals(type) || "adverb, particle".equals(type)){
      			type = "adj";
      		} else 
      		if("verb".equals(type) || "verb of motion".equals(type)){
      			type = "v";
      		} else 
      		if("adverb".equals(type)){
      			type = "adv";
      		} else
       		if(type.indexOf("conj, ") != -1 || type.indexOf("conjunction") != -1 || "conjunction".equals(type) || "conj".equals(type) || "conj, misc".equals(type) || "conj,  misc".equals(type)){
       			type = "conj";
       		} else
       		if("preposition".equals(type) || "preposition, particle".equals(type)){
       			type = "prep";
       		} else       		
       		if(type.indexOf("pronoun, ") != -1 || type.indexOf("pron, ") != -1 || "pronoun".equals(type)){
       			type = "pron";
       		} else       		
       		if("particle".equals(type)){
       			type = "part";
       		} else       		
       		if("ordinal number".equals(type) || "cardinal number".equals(type) || "numeral".equals(type) || "cardinal numberinal number".equals(type)){
       			type = "n";
       		} else
       		if("misc".equals(type) || "misc (impersonal)".equals(type)){
      			type = "";
      		} else {
      			System.out.println(id + " | " + type + " | " + ru + " | " + en);
      		}

      		
    			Word w = new Word();
    			w.setWord(ru);
    			w.setDefinition(en);
    			w.setType(type);
    			
    			wordList.add(w);
    			
      		
    			idx++; 
    		}
    	}
    	
      WordRepository.toXML(
      		"RU-EN 1000", wordList, new File("d:/tmp/ru-1001.xml"), true);
    }
	
}
