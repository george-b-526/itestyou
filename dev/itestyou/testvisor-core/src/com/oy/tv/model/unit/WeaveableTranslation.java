package com.oy.tv.model.unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeaveableTranslation {

	// text buffer that holds original value
	private StringBuffer sb = new StringBuffer();

	// list of expressions in order
	List<String> expressions = new ArrayList<String>();
	
	// map of expression id to path to xml Node in parent.childNode() that represents 
	// this terminal, ie. "0"=>"1.1", "1" => "3.2"
	// '1' means first child, '1.2' means second child of the first child 
	Map<Integer, String> terminals = new HashMap<Integer, String>();

	// raw new value to be weaved
	private String newValue;
	
	public void append(String text){
		sb.append(text);
	}
	
	public String getOriginalValue(){
		return sb.toString().trim();
	}

	public String getNewValue(){
		return newValue;
	}
	
	public void setNewValue(String value){
		this.newValue = value;
	}
	
	public void resetToOriginal(){
		newValue = getOriginalValue();
	}
	
}
