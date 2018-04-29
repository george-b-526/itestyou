package com.oy.tv.model.vocb;

import java.io.Serializable;

public class Word implements Serializable {
  private static final long serialVersionUID = 1L;

  int id;
	String word;
	String type;	// v, n, adj, ...
	String definition;
	String cat;		// category

	void setId(int id) {
  	this.id = id;
  }
	public int getId(){
		return id;
	}
	public String getWord() {
  	return word;
  }
	public void setWord(String word) {
  	this.word = word;
  }
	public String getType() {
  	return type;
  }
	public void setType(String type) {
  	this.type = type;
  }
	public String getDefinition() {
  	return definition;
  }
	public void setCategory(String cat) {
  	this.cat = cat;
  }
	public String getCategory() {
  	return cat;
  }
	public void setDefinition(String definition) {
  	this.definition = definition;
  }

	public String toString(){
		return "" + id + ":" + word + "@" + definition;
	}
}