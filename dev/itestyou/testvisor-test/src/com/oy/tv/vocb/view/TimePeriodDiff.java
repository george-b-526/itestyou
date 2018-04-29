package com.oy.tv.vocb.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.oy.tv.model.learn.ProgressCalculator;
import com.oy.tv.model.vocb.WordSet;

public class TimePeriodDiff {
	Map<Integer, Integer> all = new HashMap<Integer, Integer>();

	public void accumulate(String unitData){
		ProgressCalculator.accumulate(all, unitData);
	} 

	public void partition(Collection<Integer> pass, Collection<Integer> fail){
		partition(pass, fail, null);
	}
	
	public void partition(Collection<Integer> pass, Collection<Integer> fail, WordSet ws){
		for (int id : all.keySet()){
			
			// validate all ids are still valid in case dictionary has changed
			if (ws != null){
  			if (id < 0 || id >= ws.getWords().size()){
  				continue;
  			}
			}
			
			int count = all.get(id);
			if (count > 0){
				pass.add(id);
			} else {
				fail.add(id);
			}
		}
	}
}
