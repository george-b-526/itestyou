package com.oy.tv.vocb.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Plan {
	// count to list of indexes
	Map<Integer, List<Integer>> all = new HashMap<Integer, List<Integer>>();
	
	// all counts in order
	List<Integer> counts = new ArrayList<Integer>();

	// current count
	private int countIndex = 0;
	
	// current index in count'th list
	private int idIndex = 0;
	
	private Plan(){ }
	
	/**
	 * Returns next id in order from the lowest count to largest.
	 * If no ids in this group, moves to the next group with higher count.
	 * Returns null if no more values available.
	 */
	public Integer nextId(){
		if (countIndex < counts.size()){
			List<Integer> ids = all.get(counts.get(countIndex));
			if (idIndex < ids.size()){
				Integer result = ids.get(idIndex); 
				idIndex++;
				return result;
			} else {
				idIndex = 0;
				countIndex++;
				return nextId();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Groups all ids by count. Shuffles ids in each group.
	 */
	public static Plan newPlan(TimePeriodDiff diff, Random rnd){
		Plan plan = new Plan();
		
		// sort by level
		for (Integer key : diff.all.keySet()){
			Integer value = diff.all.get(key);

			List<Integer> ids = plan.all.get(value);
			if (ids == null){
				ids = new ArrayList<Integer>();
				plan.all.put(value, ids);
			}
			ids.add(key);
		}
		
		// shuffle within each level
		for (Integer count : plan.all.keySet()){
			Collections.shuffle(plan.all.get(count), rnd);
			plan.counts.add(count);
		}
		Collections.sort(plan.counts);
		
		return plan;
	}
}
