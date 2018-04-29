package com.oy.tv.model.words.dicts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class Parse {

	public static void main(String [] args) throws Exception {
		String index = load("index.php");
		
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
			if ("english".equals(ns)) continue;
			if ("ukrainian".equals(ns)) continue;
		
	    List<Word> words = new ArrayList<Word>();
			
			System.out.println(ns);
			
			String content = load(ns + ".html");
			
			StringNavigator sn = new StringNavigator(content);
			sn.next("<a name");
			sn.next("<b>");
			sn.next("</b>");
			
			String cat = sn.prev();
			
			sn.next("\n");
			while(sn.tryNext("\n")){
				String line = sn.prev();
				StringNavigator snx = new StringNavigator(line);
				
				if (line.startsWith("<a title=\"")){
					snx.next("<a title=\"");
					snx.next("\">");
					String other = snx.prev();
					snx.next("</a>");
					String en = snx.prev();

					String type = "";
					String catx = cat;
					
					// clean
					other = other.replaceAll("([ ]*)[;]", ";");
					en = en.replaceAll("([ ]*)[;]", ";");
					
					// cat
					if ("Abstract".equals(cat)){
						catx = "Abstract";
						type = "n";
					} else
					if ("Adjective".equals(cat)){
						catx = "Adjectives";
						type = "adj";
					} else 
					if ("Adjective-basic".equals(cat)){
						catx = "Adjectives";
						type = "adj";
					} else 
					if ("Adjective-people".equals(cat)){
						catx = "Adjective";
						type = "adj";
					} else
					if ("Adverb".equals(cat)){
						type = "adv";
					} else
					if ("Anatomy".equals(cat)){
						type = "noun";
					} else
					if ("Animal".equals(cat)){
						catx = "Animals";
						type = "noun";
					} else
					if ("Art".equals(cat)){
						type = "n";
					} else
					if ("Article".equals(cat)){
						type = "";
					} else
					if ("Business".equals(cat)){
  					type = "n";
					} else
					if ("City".equals(cat)){
						type = "n";
					} else
					if ("Clothes".equals(cat)){
						type = "n";
					} else
					if ("Color".equals(cat)){
						catx = "Colors";
						type = "adj";
					} else						
					if ("Communication".equals(cat)){
						type = "n";
					} else
					if ("Conjunction".equals(cat)){
						type = "conj";
					} else						
					if ("Container".equals(cat)){
						catx = "Containers";
						type = "n";
					} else
					if ("Conversation".equals(cat)){
						type = "";
					} else						
					if ("Country".equals(cat)){
						catx = "Countries";
						type = "n";
					} else
					if ("Device".equals(cat)){
						catx = "Devices";
						type = "n";
					} else						
					if ("Drink".equals(cat)){
						catx = "Drinks";
						type = "n";
					} else
					if ("Education".equals(cat)){
						type = "n";
					} else						
					if ("Environment".equals(cat)){
						type = "n";
					} else						
					if ("Family".equals(cat)){
						type = "n";
					} else						
					if ("Feeling".equals(cat)){
						catx = "Feelings";
						type = "n";
					} else						
					if ("Food".equals(cat)){
						type = "n";
					} else						
					if ("Fruit".equals(cat)){
						catx = "Fruits";
						type = "n";
					} else						
					if ("Furniture".equals(cat)){
						type = "n";
					} else						
					if ("Geography".equals(cat)){
						type = "n";
					} else						
					if ("House".equals(cat)){
						type = "n";
					} else						
					if ("Language".equals(cat)){
						catx = "Languages";
						type = "n";
					} else						
					if ("Material".equals(cat)){
						catx = "Materials";
						type = "n";
					} else						
					if ("Mathematics".equals(cat)){
						type = "n";
					} else						
					if ("Medicine".equals(cat)){
						type = "n";
					} else						
					if ("Nature".equals(cat)){
						type = "n";
					} else						
					if ("Number".equals(cat)){
						catx = "Numbers";
						type = "n";
					} else						
					if ("Object".equals(cat)){
						catx = "Objects";
						type = "n";
					} else						
					if ("Physics".equals(cat)){
						type = "n";
					} else						
					if ("Preposition".equals(cat)){
						catx = "Prepositions";
						type = "prep";
					} else						
					if ("Profession".equals(cat)){
						catx = "Professions";
						type = "n";
					} else						
					if ("Pronoun".equals(cat)){
						catx = "Pronouns";
						type = "pron";
					} else						
					if ("Relations".equals(cat)){
						type = "n";
					} else						
					if ("Science".equals(cat)){
						type = "n";
					} else						
					if ("Society".equals(cat)){
						type = "n";
					} else						
					if ("Sports".equals(cat)){
						type = "n";
					} else						
					if ("Time".equals(cat)){
						type = "n";
					} else						
					if ("Tool".equals(cat)){
						catx = "Tools";
						type = "n";
					} else						
					if ("Transport".equals(cat)){
						type = "n";
					} else						
					if ("Vegetable".equals(cat)){
						catx = "Vegetables";
						type = "n";
					} else						
					if ("Verb".equals(cat)){
						catx = "Verbs";
						type = "v";
					} else						
					if ("Verb-basic".equals(cat)){
						catx = "Verbs";
						type = "v";
					} else						
					if ("Weather".equals(cat)){
						type = "n";
					} else						
						throw new RuntimeException(cat);
						
					Word w = new Word();
					w.setCategory(catx.toLowerCase());
					w.setWord(other);
					w.setDefinition(en);
					w.setType(type);  
					
					words.add(w);
					
					continue;
				}

				if (line.startsWith("<br/>")){
					snx.next("<b>");
					snx.next("</b>");
					cat = snx.prev();
				}
				
			}
			
	    WordRepository.toXMLByHand(
	    		langs.get(ns), words, new File(HOME + "xml/" + ns + ".xml"), true);
		}
		
	}
	
	static final String HOME = "src/com/oy/tv/model/words/dicts/";
	
	public static String load(String fn) throws Exception {
		Reader sr = new InputStreamReader(new FileInputStream(HOME + fn), "UTF-8");
		BufferedReader in = new BufferedReader(sr);		

    StringBuffer sb = new StringBuffer();
    while(true){
    	String line = in.readLine();
    	if (line == null) break;
    	sb.append(line + "\n");
    }		

    return sb.toString();
	}
	
}
