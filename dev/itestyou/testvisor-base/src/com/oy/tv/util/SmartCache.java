package com.oy.tv.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * HashMap that is size bound and expires items after a period of time. 
 */

public class SmartCache<K, V>{
	
	class Entry {
		long cachedOn;
		V t;
	}
	
	private SmartCacheMonitor monitor;
	private Map<K, Entry> cache = new TreeMap<K, Entry>();
	private List<K> list = new LinkedList<K>();
	
	private long cacheForMillis;
	private int maxSize;
	
	public SmartCache(long cacheForMillis, int maxSize, SmartCacheMonitor monitor){
		this.cacheForMillis = cacheForMillis;
		this.maxSize = maxSize;
		this.monitor = monitor;
	}
	
	public void put(K key, V t){
		long now = System.currentTimeMillis();
		
		list.add(key);
		
		Entry e = new Entry();
		e.cachedOn = now;
		e.t = t;
		
		cache.put(key, e);
		monitor.incPut();
		
		if (cache.size() > maxSize){
			remove(list.get(0));
		}
		
		monitor.setSize(size());
	}

	public V get(K key){
		long now = System.currentTimeMillis(); 
		
		Entry e = cache.get(key);
		if (e == null){
			monitor.incMiss();
			return null;
		} else {
			monitor.incHit();
		}
		
		if (now - e.cachedOn <= cacheForMillis){
			return e.t;
		} else {  
			remove(key);
			monitor.incExpire();
			return null;
		}
	}

	public void remove(K key){
		list.remove(key);
		cache.remove(key);
		
		monitor.incEject();
		monitor.setSize(size());
	}
	
	public void clear(){
		list.clear();
		cache.clear();

		monitor.setSize(size());
	}

	public int size(){
		return cache.size();
	}
	
}
