package com.oy.tv.model.learn;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ProgressCalculator {
	
	public static void accumulate(Map<Integer, Integer> sum, String unitData){
		Map<Integer, Integer> more = ProgressCalculator.parseUnitData(unitData);
		for (int id : more.keySet()){
			int count = more.get(id);
			Integer has = sum.get(id);
			if (has != null){
				sum.put(id, has + count);
			} else {
				sum.put(id, count);
			}
		}  			
	}
	
	public static Map<Integer, Integer> parseUnitData(String unitData){
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(); {
  		if (unitData != null && unitData.length() > 2){
  			unitData = unitData.substring(1, unitData.length() - 1).trim();
  			
  			String [] parts = unitData.split(",");
  			for (String part : parts){
  				String [] terms = part.split(":");
  				result.put(Integer.parseInt(terms[0]), Integer.parseInt(terms[1]));
  			}
  		}
		}
		return result;
	}

	public static void mergeUnitData(Map<Integer, Integer> sum, Map<Integer, Integer> delta){
		for (int id : delta.keySet()){
			Integer count = sum.get(id);
			if (count == null){
				sum.put(id, delta.get(id));
			} else {
				sum.put(id, count + delta.get(id));
			}
		}
	}
	
	
	public static class Verb {
		static final String EP = "ep";
		static final String MP = "mp";
		static final String EF = "ef";
		static final String MF = "mf";
		
		String name;
		Map<Integer, Integer> id2count = new HashMap<Integer, Integer>();
		
		public String getName(){
			return name;
		}

		public Set<Integer> getIds(){
			return id2count.keySet();
		}
		
		public boolean passed(){
			return EP.equals(name) || MP.equals(name);
		}
		
		public String toString(){
			return name + ": {" +  id2count.toString() + "}";
		}
	}

	public static Map<Integer, Integer> parseUnitDeltaData(String delta){
		return parseUnitDeltaData(delta, new HashMap<String, Verb>());
	}
	
	public static Map<Integer, Integer> parseUnitDeltaData(String delta, Map<String, Verb> rhs){
		Map<Integer, Integer> sum = new HashMap<Integer, Integer>();
		
		// parse delta
		rhs.clear();
		{
  		Properties d = new Properties();
  		try {  
  			if (delta != null){
  				d.load(new StringBufferInputStream(delta));
  			}
  		} catch (Exception e){
  			throw new RuntimeException();
  		}
  		
  		for (Object key : d.keySet()){
  			String name = (String) key;
  			Verb verb = new Verb();
  			verb.name = name;
  			
  			String value = d.getProperty(name);
  			value = value.substring(1, value.length() - 1).trim();
  			if (value.length() == 0){
  				continue;
  			}
  
  			String [] parts = value.split(",");
  			for (String part : parts){
  				verb.id2count.put(Integer.parseInt(part), 1);
  			}
  			rhs.put(name, verb);
  		}
		}
		
		// add
		for (Verb verb : rhs.values()){
			for (int id : verb.id2count.keySet()){
				Integer count = sum.get(id);
				if (count == null){
					count = 0;
				}				
				
				if (Verb.EP.equals(verb.name) || Verb.MP.equals(verb.name)){
					sum.put(id, count + 1);					
					continue;
				}
				if (Verb.EF.equals(verb.name) || Verb.MF.equals(verb.name)){
					sum.put(id, count - 1);
					continue;
				}
				
				throw new RuntimeException("Unknown verb: " + verb.name);
			}
		}
		
		return sum;
	}

	public static String mergeUnitData(String sum, String delta){
		Map<Integer, Integer> _sum = parseUnitData(sum);
		Map<Integer, Integer> _delta = parseUnitDeltaData(delta);
		mergeUnitData(_sum, _delta);
		return renderUnitData(_sum);
	}
		
	public static String renderUnitData(Map<Integer, Integer> data){
		// sort
		List<Integer> ids = new ArrayList<Integer>();
		ids.addAll(data.keySet());
		Collections.sort(ids);

		if (ids.size() == 0){
			return null;
		}
		
		// render
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean has = false;
		for (int id : ids){
			if (has){
				sb.append(",");
			}
			
			sb.append(id);
			sb.append(":");
			sb.append(data.get(id));
		
			has = true;
		}
		sb.append("}");
		
		return sb.toString();		
	}
	
	public static String mergeUnitData(String source, Map<Integer, Integer> delta){
		Map<Integer, Integer> sum = ProgressCalculator.parseUnitData(source);
    if (delta != null){
  		mergeUnitData(sum, delta);
    }
		return renderUnitData(sum);
	}
	
	public static class Summary {
		String data;
		int completed;
		
		public String getData(){
			return data;
		}
		
		public int getCompleted(){
			return completed;
		}
	}
	
	public static String renderUnitSummary(Set<Integer> all){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean has = false;
		for (int id : all){
			if (has){
				sb.append(",");
			}
			has = true;
			
			sb.append(id);
		}
		sb.append("}");
		return sb.toString();
	}
		
	public static Set<Integer> parseUnitSummary(String data){
		Set<Integer> all = new HashSet<Integer>();
		if (data != null && data.length() > 2){
			data = data.substring(1, data.length() - 1).trim();
			String [] ids = data.split(",");
			for (String id : ids){
				all.add(Integer.parseInt(id));
			}
		}
		return all;
	}

	public static class RangeContainer {
		Map<Integer, Range> left = new HashMap<Integer, Range>();
		Map<Integer, Range> right = new HashMap<Integer, Range>();
	
		public void clear(){
			left.clear();
			right.clear();
		}
		
		public int getRangeCount(){
			return left.size();
		}

		public int size(){
			int count = 0;
			for (Range range : left.values()){
				count += range.right - range.left + 1;
			}
			return count;
		}
		
		public String render(){
			StringBuffer sb = new StringBuffer();
			sb.append("{");

			List<Integer> values = new ArrayList<Integer>();
			values.addAll(left.keySet());
			Collections.sort(values);
						
			boolean has = false;
			for (Integer value : values){
				if (has){
					sb.append(",");
				}
				has = true;
			
				Range range = left.get(value);
				if (range.left == range.right){
					sb.append(range.left);
				} else {
					sb.append(range.left);
					sb.append("|");
					sb.append(range.right);
				}
			}
			
			sb.append("}");
			return sb.toString();
		}

		public boolean contains(int value){
			for (Range range : this.left.values()){
				if (range.left <= value && value <= range.right){
					return true;
				}
			}
			return false;
		}
		
		public void add(int value){
			if (contains(value)){
				return;
			}
			
			Range right = this.right.get(value - 1);
			if (right != null){
				this.right.remove(right.right);
				right.right = value;
				this.right.put(right.right, right);
			}
			
			Range left = this.left.get(value + 1);
			if (left != null){
				this.left.remove(left.left);
				left.left = value;
				this.left.put(left.left, left);
			}
			
			if (left != null && right != null){
				this.right.remove(right.right);
				right.right = left.right;
				this.right.put(right.right, right);
				
				this.left.remove(left.left);
				this.left.put(right.left, right);
			}
			
			if (right == null && left == null){
				Range range = new Range();
				range.left = value;
				range.right = value;
				
				this.left.put(range.left, range);
				this.right.put(range.right, range);
			}			
		}
	}
	
	public static void parseUnitSummaryWithRanges(String data, RangeContainer container){
		if (data != null && data.length() > 2){
			if (!data.startsWith("{") || !data.endsWith("}")){
				throw new IllegalArgumentException("Expected {...}");
			}
		
			data = data.substring(1, data.length() - 1).trim();
			String [] ranges = data.split(",");
			for (String range : ranges){
				int idx = range.indexOf("|");				
				Range item = new Range();
				if (idx == -1){
					int value = Integer.parseInt(range);
					item.left = value;
					item.right = value;
				} else {
					item.left = Integer.parseInt(range.substring(0, idx));
					item.right = Integer.parseInt(range.substring(idx + 1));
					
					if (item.left > item.right){
						throw new IllegalArgumentException("Bad range: " + range);
					}
				}
				
				if (container.left.containsKey(item.left)){
					throw new IllegalArgumentException("Overalpping range: " + range);
				}
				container.left.put(item.left, item);

				if (container.right.containsKey(item.right)){
					throw new IllegalArgumentException("Overalpping range: " + range);
				}
				container.right.put(item.right, item);
			}
		}
	}
	
	public static class Range {
		int left;
		int right;
		
		public int getLeft(){
			return left;
		}
		
		public int getRight(){
			return right;
		}
		
		public String toString(){
			return left + "|" + right;
		}
	}

	public static Summary mergeUnitSummary(String data, Map<String, ProgressCalculator.Verb> verbs){
		RangeContainer rc = new RangeContainer();
		parseUnitSummaryWithRanges(data, rc);

		Summary result = new Summary();

		// append passing ids
		for (Verb verb : verbs.values()){
			if (verb.passed()){
  			for (int id : verb.getIds()){
  				rc.add(id);
  			}
			}
		}

		result.data = rc.render();
		result.completed = rc.size();
		
		return result;
	}
}
