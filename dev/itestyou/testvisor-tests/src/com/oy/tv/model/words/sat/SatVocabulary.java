package com.oy.tv.model.words.sat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class SatVocabulary extends TestCase {
	
	private void loadFile(List<Word> words, String ns) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(new File(
				"src/com/oy/tv/model/words/sat-words-" + ns + ".html")));		

		StringBuffer sb = new StringBuffer();
    while(true){
    	String line = in.readLine();
    	if (line == null) break;
    	sb.append(line + "\n");
    }		

  	StringNavigator sn = new StringNavigator(sb.toString());
  	sn.next("<p class=\"title\">SAT Vocabulary&nbsp;Words");
  	sn.next("</p>");
  	while(sn.tryNext("<ul>")){
  		sn.next("</ul>");
  		StringNavigator ul = new StringNavigator(sn.prev());
  		while(ul.tryNext("<li>")){
  			ul.next("</li>");

  			StringNavigator line = new StringNavigator(ul.prev());
  			Word word = new Word();

				line.next(".");
				String text = line.prev();
				int idx = text.lastIndexOf("-");

				word.setWord(text.substring(0, idx).trim());
				assertTrue(word.getWord().length() <= 17);
				assertTrue(word.getWord().length() >= 3);
				
				word.setType(text.substring(idx + 1).trim());
  			boolean known =
					"n".equals(word.getType()) || "v".equals(word.getType()) ||
					"a".equals(word.getType()) ||
					"j".equals(word.getType());
				if (!known){
					throw new RuntimeException("Bad type (" + word.getType() + "): " + ul.prev());
				}
  			
  			word.setDefinition(line.next());
  			
  			words.add(word);
  		}
  	}
	
	}
	
	public void testSatWordJoiner() throws Exception {
		List<Word> words = new ArrayList<Word>();
		loadFile(words, "1000");
		loadFile(words, "2000");
		loadFile(words, "3000");
		loadFile(words, "4000");
		loadFile(words, "5000");
		
		WordRepository.save("SAT 5000", words, new File("d:/tmp/sat-words-list.bin"));

		show(words);
	}
	
	public void xxxtestSatWordLoader() throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(new File(
				"src/com/oy/tv/model/words/sat-4000.txt")));		
		
		List<Word> words = new ArrayList<Word>();
		String prev = null;
		while(true){
			String line = in.readLine();
			if (line == null) break;
			
			line = line .trim();
			if (line.length() == 0){
				continue;
			}
			
			if (prev != null){
				line = prev + " " + line;
			}
			
			if (line.endsWith("<br>")){
				prev = null;
				
				Word word = new Word();
				StringNavigator sn = new StringNavigator(line);
				
				sn.next(".");
				String text = sn.prev();
				int idx = text.lastIndexOf(" ");

				word.setWord(text.substring(0, idx).trim());
				assertTrue(word.getWord().length() <= 17);
				assertTrue(word.getWord().length() >= 3);
				
				word.setType(text.substring(idx + 1).trim());
				boolean known =
					"adv&adj".equals(word.getType()) || "n&pl".equals(word.getType()) ||
					"n".equals(word.getType()) || "v".equals(word.getType()) ||
					"adv".equals(word.getType()) || "inter".equals(word.getType()) ||
					"prep".equals(word.getType()) || "interj".equals(word.getType()) ||
					"ad".equals(word.getType()) || "pa".equals(word.getType()) ||
					"Latin".equals(word.getType()) || "Greek".equals(word.getType()) ||
					"adj".equals(word.getType()) || "conj".equals(word.getType());
				if (!known){
					throw new RuntimeException("Bad type: " + line);
				}
				
				sn.next("<br>");		
				word.setDefinition(sn.prev().trim());
				
				// must have forst cap
				char firstLetter = word.getDefinition().charAt(0);
				assertTrue(word.getDefinition(), 'A' <= firstLetter && firstLetter <= 'Z' );
				
				// check non alpha
				for (int i=1; i < word.getDefinition().length(); i++){
					char c = word.getDefinition().charAt(i);
					boolean alpha = 
						('A' <= c && c <= 'Z') || 
						('a' <= c && c <= 'z') ||
						('0' <= c && c <= '9') ||
						(c == ' ' || c == ';' || c == ',' || c == '.' || c == '-' || c == '\'' || c == '"' || c == '(' || c == ')');
					if (!alpha){
						break;				
					}
				}
					
				// check caps inside the definition
				for (int i=1; i < word.getDefinition().length(); i++){
					char c = word.getDefinition().charAt(i);
					if ('A' <= c && c <= 'Z'){
						break;
					}
				}
				
				assertTrue(word.getDefinition(), word.getDefinition().endsWith("."));
				assertTrue(word.getDefinition(), word.getDefinition().indexOf("  ") == -1);
				
				words.add(word);
			} else {
				prev = line;
			}			
		}
	
		show(words);		
	}
	
	private void show(List<Word> words){
		System.out.println("Found " + words.size() + " words:");

		Collections.sort(words, new WordRepository.WordComparator());
		
		int i = 0;
		for (Word word : words){
			i++;
			//if (word.definition.indexOf(" reduce ") == -1){
			//	continue;
			//}
			if (!word.getWord().endsWith("ation")){
				continue;
			}
			System.out.println(
					" // " + i + 
					" // "  + word.getWord()+ 
					" // " + word.getType() + 
					" // " + word.getDefinition());
		}
	}
	
}
