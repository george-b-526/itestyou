package com.oy.tv.model.unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.oy.tv.model.core.NumberKind;

public class UnitContext implements Serializable {
	
	static final long serialVersionUID = 0;

	public String values;
	public String question;
	public List<String> choices = new ArrayList<String>();
	public Set<Integer> answerIndexes = new HashSet<Integer>();
	public List<String> warnings;
	public List<String> errors;
	
	public NumberKind kind = new NumberKind();
	
	public void addWarning(String warning){
		if (warnings == null){
			warnings = new ArrayList<String>();
		}
		warnings.add(warning);
	}
	
	public void addError(String warning){
		if (errors == null){
			errors = new ArrayList<String>();
		}
		errors.add(warning);
	}

	/**
	 * This has a bug for cases when answerIdeX != 0
	 * and should not be used.
	 */
	@Deprecated
	public static void shuffle(UnitContext ctx, Map<Integer, Integer> new2old, Random rnd){
		List<String> choices = new ArrayList<String>();
		Set<Integer> answerIndexes = new HashSet<Integer>();
		  
		int i=0;
		new2old.clear();
		while(ctx.choices.size() > 0) {
			int index = rnd.nextInt(ctx.choices.size());
			  
			choices.add(ctx.choices.remove(index));
			if (ctx.answerIndexes.remove(index)){
				new2old.put(i, index);
				answerIndexes.add(i);
			}
			
			i++;
		}
		     
		ctx.choices = choices;
		ctx.answerIndexes = answerIndexes;
	}

	
	public static void shuffleEx(UnitContext ctx, Map<Integer, Integer> new2old, Random rnd){
		// list all ids
		List<Integer> ids = new ArrayList<Integer>();
		for (int i=0; i < ctx.choices.size(); i++){
			ids.add(i);
		}
		Collections.shuffle(ids, rnd);

		// allocate new 
		List<String> choices = new ArrayList<String>();
		Set<Integer> answerIndexes = new HashSet<Integer>();

		// map
		new2old.clear();
		for (int i=0; i < ids.size(); i++){
			int index = ids.get(i);
			  
			choices.add(ctx.choices.get(index));

			if (ctx.answerIndexes.contains(index)){
				new2old.put(i, index);
				answerIndexes.add(i);
			}			
		}
		     
		// replace
		ctx.choices = choices;
		ctx.answerIndexes = answerIndexes;
	}

}
