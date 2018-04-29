package com.oy.tv.util;

import com.oy.shared.lw.perf.agent.BaseMonitor;

public class SmartCacheMonitor extends BaseMonitor {

	private long [] m_Values = new long[]{0, 0, 0, 0, 0, 0};
	private long [] m_Clone = new long[]{0, 0, 0, 0, 0, 0};
	
	public SmartCacheMonitor (Class module, String name, String desc){
		super(
			module, name, desc,
			new String []{"put", "hit", "miss", "eject", "expire", "size"}	
		);
	}	
	
	public synchronized void incPut(){ m_Values[0]++; }
	
	public synchronized void incHit(){ m_Values[1]++; }	

	public synchronized void incMiss(){ m_Values[2]++; }	
	
	public synchronized void incExpire(){ m_Values[3]++; }	

	public synchronized void incEject(){ m_Values[4]++; }	
	
	public synchronized void setSize(int size){ m_Values[5] = size; }
	
	public synchronized long [] getValues(){
		return getValues(m_Values, m_Clone); 
	}
	
}
