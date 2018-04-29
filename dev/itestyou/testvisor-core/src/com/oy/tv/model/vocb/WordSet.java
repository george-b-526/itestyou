package com.oy.tv.model.vocb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSet implements Serializable {
  private static final long serialVersionUID = 1L;

	String name;
	String lang;
	List<Word> words;
	
	private Map<String, List<Word>> byType;
	
	WordSet(){}
	
	public String getName() {
  	return name;
  }

	public String getLang() {
  	return lang;
  }
	
	public List<Word> getWords() {
  	return words;
  }

	public void safeIndex(){
		index(false);
	}
	
	void index(){
		index(true);
	}
	
	void index(boolean updateId){
		byType = new HashMap<String, List<Word>>();
		int idx = 0;
		for (Word word : getWords()){
			String type = word.getType();
			List<Word> words = byType.get(type);
			if (words == null){
				words = new ArrayList<Word>();
				byType.put(type, words);
			}
			
			if (updateId){
				word.setId(idx);
			}
			
			words.add(word);
			idx++;
		}
	}
	
	public List<Word> getAllOfType(String type){
		return byType.get(type);
	}
	
}
