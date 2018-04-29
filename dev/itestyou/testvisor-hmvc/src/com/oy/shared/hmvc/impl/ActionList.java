/*
	Hierarchical Model View Controller (OY-HMVC)
	Copyright (C) 2005-2008 Pavel Simakov
	http://www.softwaresecretweapons.com

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/


package com.oy.shared.hmvc.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.oy.shared.hmvc.IAction;
import com.oy.shared.hmvc.IActionList;

public class ActionList implements IActionList {
	
	private List<IAction> m_List = new LinkedList<IAction>();
	private Map<Integer, IAction> m_Map = new HashMap<Integer, IAction>();
	
	public void clear(){
		m_List.clear();
		m_Map.clear();
	}
	 
	public void addAction(IAction action){
		if (contains(action)){
			throw new RuntimeException("Error adding, already added.");
		}
		
		m_List.add(action);
		m_Map.put(new Integer(action.getId()), action);
	}
	
	public void removeAction(IAction action){
		if (!contains(action)){
			throw new RuntimeException("Error removing, not added.");
		}
		
		m_List.remove(action);
		m_Map.remove(new Integer(action.getId()));
	}
			
	public int size(){
		return m_List.size();
	}
	
	public IAction getActionById(int id){
		return (IAction) m_Map.get(new Integer(id));
	}
	
	public IAction getAction(int index){
		return (IAction) m_List.get(index);
	}
	
	public boolean contains(IAction action){ 
		return m_Map.containsKey(new Integer(action.getId()));
	}

}
