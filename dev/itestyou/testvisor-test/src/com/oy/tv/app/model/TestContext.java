package com.oy.tv.app.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oy.tv.app.IExitStrategy;
import com.oy.tv.schema.core.BundleBO;
import com.oy.tv.schema.core.TestBO;
import com.oy.tv.schema.core.UserBO;

public class TestContext implements Serializable {
	
	static final long serialVersionUID = 0;
	
	InteractiveOptions options = new InteractiveOptions();
	
	UserBO owner;  
	BundleBO bun;
	TestBO test;
	
	boolean persistent;
	
	int currIndex;  
	List<Step> steps;  
		  
	transient IExitStrategy onExit;
}

class UnitGroup implements Serializable {
	
	static final long serialVersionUID = 0;
	
	int unitId;
	int count;
}

class Step implements Serializable {
	
	static final long serialVersionUID = 0;
	
	UnitGroup group;
	int variationId;   
	int choiceCount;
     	
	Date startOn;
	Date complletedOn;

	Map<Integer, Integer> new2old = new HashMap<Integer, Integer>();
	
	int answerIndex;
	
	public int getChoiceCount(){
		return choiceCount;
	}
}