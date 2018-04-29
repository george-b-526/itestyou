package com.oy.tv.model.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.oy.tv.util.StringNavigator;
import com.oy.tv.util.TestUtil;

public class VariationGenerator {

	class Level {
		void iterateLevel(List<Level> stack, int idx, List<String> all, String pre){
			
		}
	}
	
	class IntLevel extends Level {
		int from;
		int to;
		int step;
		int curr;
		  
		void iterateLevel(List<Level> stack, int idx, List<String> all, String pre){
			for (int i=from; i <= to; i += step){
				if (idx < stack.size() - 1){
					iterateLevelEx(stack, idx + 1, all, pre + i + ",");
				} else {
					all.add("{" + pre + i + "}");
				}
			}
		}
	}
	  
	class DoubleLevel extends Level {
		BigDecimal from;
		BigDecimal to;
		BigDecimal step;
		BigDecimal curr;
	
		void iterateLevel(List<Level> stack, int idx, List<String> all, String pre){
			BigDecimal d = from;
			while (true){
				if (idx < stack.size() - 1){
					iterateLevelEx(stack, idx + 1, all, pre + d + ",");
				} else {
					all.add("{" + pre + d.doubleValue() + "}");
				}
  
				d = d.add(step);
				if (!(d.compareTo(to) <= 0)){
					break;
				}
			}
		}
	}
	
	Level parseIntLevel(String from, String to, String step){
		IntLevel level = new IntLevel();
		try {
			level.from = Integer.parseInt(from);
			level.to = Integer.parseInt(to);
			level.step = Integer.parseInt(step);
		} catch (Exception e){
			return null;
		}
		return level;
	}
	
	Level parseDoubleLevel(String from, String to, String step){
		DoubleLevel level = new DoubleLevel();
		try {
			level.from = BigDecimal.valueOf(Double.parseDouble(from));
			level.to = BigDecimal.valueOf(Double.parseDouble(to));
			level.step = BigDecimal.valueOf(Double.parseDouble(step));
		} catch (Exception e){
			return null;
		}
		return level;
	}
	
	private List<Level> buildStack(String input){
		List<Level> lall = new ArrayList<Level>();
		
		StringNavigator sn = new StringNavigator(input);
		sn.next("{");
		boolean done = false;
		while(!done){
			boolean has = sn.tryNext(",");
			if (!has) {
				sn.next("}");
				done = true;
			}
		
			Level level;
			String term = sn.prev().trim();
			{
				String from;
				String to;
				{
					StringNavigator snx = new StringNavigator(term);
					if (snx.tryNext("..")){
						from = snx.prev().trim();
						to = snx.next().trim();
					} else {
						from = sn.prev().trim();
						to = from;
					}
				}
				
				String step;
				{
				StringNavigator snx = new StringNavigator(to);
					if (snx.tryNext(":")){
						to = snx.prev();
						step = snx.next();
					} else { 
						step = "1";
					}
				}  
				
				level = parseIntLevel(from, to, step);
				if (level == null){
					level = parseDoubleLevel(from, to, step);
					if (level == null){
						throw new RuntimeException("Failed to parse term: " + term);
					}
				}
			}
			lall.add(level);
		}
	
		return lall;
	}
	    
	private void iterateLevelEx(List<Level> stack, int idx, List<String> all, String pre){
		Level level = stack.get(idx);
		level.iterateLevel(stack, idx, all, pre);
	}
    	  
	private List<String> iterateStack(List<Level> stack){
		List<String> all = new ArrayList<String>();
		iterateLevelEx(stack, 0, all, "");
		return all;
	}
	
	/**
	 * Converts range expression {1..3,2..5} into all possible variation permutations:
	 * 
	 * {1,2}{1,3}{1,4}{1,5}{2,2}{2,3}{2,4}{2,5}{3,2}{3,3}{3,4}{3,5}
	 * 
	 * @param input
	 * @return
	 */
	public List<String> generate(String input){
		return iterateStack(buildStack(input));
	}
	
	public static String toString(List<String> all){
		StringBuffer sb = new StringBuffer();
		for (int i=0; i < all.size(); i++){
			if (i != 0){
				sb.append("\n");	
			}
			sb.append(all.get(i));
		}    
		return sb.toString();
	}
	
	public static void main(String [] args){
		VariationGenerator vg = new VariationGenerator();
		{
			String input = "{1..5,2..7,1..8,10}";
			List<String> all = vg.generate(input);
			
			TestUtil.assertSame(all.size(), 240);
			TestUtil.assertSame(all.get(0), "{1,2,1,10}");
			TestUtil.assertSame(all.get(all.size() - 1), "{5,7,8,10}");
			TestUtil.assertSame(all.get(120), "{3,5,1,10}");
			
			System.out.println(all);
		}
		
		{
			String input = "{0.2..0.8:0.2, 0.05..0.25:0.05}";
			List<String> all = vg.generate(input);
			
			TestUtil.assertSame(all.size(), 20);  
			TestUtil.assertSame(all.get(0), "{0.2,0.05}");
			TestUtil.assertSame(all.get(all.size() - 1), "{0.8,0.25}");
			TestUtil.assertSame(all.get(10), "{0.6,0.05}");
			    
			System.out.println(all);   
		}
		
	}
	
}
