package com.vokamis.ity.rpc;

import java.util.HashMap;
import java.util.Map;

import com.vokamis.ity.util.StringNavigator;

public class XmlParserLight {

	private Map<String, String> all;
	
	public void parse(String responseText){
		all = new HashMap<String, String>();
		
		StringNavigator sn = new StringNavigator(responseText);
		sn.next("<ity-api-result").next(">").next("</ity-api-result");
		
		sn = new StringNavigator(sn.prev);
		while (sn.tryNext("<")){
			sn.next(">");
			String name = sn.prev;
			sn.next("<");
			String value = sn.prev;
			sn.next(">");
			
			all.put(name, value);
		}
	}
		
	public String get(String name, String _default){
		if (all.containsKey(name)){
			return all.get(name);
		} 
		return _default;
	}
	
	public int get(String name, int _default){
		if (all.containsKey(name)){
			return Integer.valueOf(all.get(name));
		} 
		return _default;
	}
	
	public <T> T get(String name, T [] values, T _default){
		if (all.containsKey(name)){
			int idx = Integer.valueOf(all.get(name));
			if (idx >=0 && idx < values.length) {
				return values[idx];
			}
		} 
		return _default;
	}
	
}
