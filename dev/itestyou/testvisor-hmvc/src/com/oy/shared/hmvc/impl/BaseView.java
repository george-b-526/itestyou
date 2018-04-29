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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.oy.shared.hmvc.IAction;
import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IActionList;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.IView;

abstract public class BaseView implements IView { 
	
	public static final String IN_PARAM_PREFIX = "in";
	
	private int m_ActionId;
	private List<IView> m_Views = new LinkedList<IView>();
	private IActionList m_ActionList = new ActionList();
	
	protected int getNextActionUID(){
		return m_ActionId++;
	}
		
	public int countViews(){
		return m_Views.size();
	}
	
	public IView getView(int index){
		return (IView) m_Views.get(index);
	}
	
	public void installView(IView view, int index){
		m_Views.add(index, view);
	}
	
	public void installView(IView view){
		m_Views.add(view);
	}
	
	public void uninstallView(IView view){
		int idx = m_Views.indexOf(view);
		if(idx != -1){
			m_Views.remove(idx);
		}
	}
	
	public void uninstallAllViews(){
		m_Views.clear();
	}
	
	protected void prepareView(IActionDispatchEncoder dispatcher){ }
	
	final public void prepare(IActionDispatchEncoder dispatcher){
		
		// prepare view
		for (int i=0; i < m_Views.size(); i++){
			((BaseView) m_Views.get(i)).prepareView(dispatcher);
		}
		
		// prepare view children
		for (int i=0; i < m_Views.size(); i++){
			((BaseView) m_Views.get(i)).prepare(dispatcher);
		}
	}
	
	private BaseAction findChildActionByName(Object target, int id) {
		return findChildActionByName(target, id, null);
	}
	
	// locates a action nested class for a specific object
	private BaseAction findChildActionByName(Object target, int id, Class actionClass) {
		
		Class clazz = target.getClass();
		BaseAction action = null;
		
		while(clazz != null){			
	        Field [] fields = clazz.getDeclaredFields();                
	        for (int i = 0; i < fields.length; i++){                                   
	        	try {
	        		fields[i].setAccessible(true);
	                if (fields[i].get(target) instanceof BaseAction) {                                                                                                            
	                	Object o = fields[i].get(target);
	                	
	                	boolean matchClass = actionClass != null && actionClass.isInstance(o);
	                	boolean matchId = o.hashCode() == id;
	                	
	                    if (matchClass || matchId){
	                    	action = (BaseAction) o;
	                    	break;
	                    }
	                }                                   
	        	} catch(Exception e){
	        		System.err.println("Access denied reading object property (" + target.getClass().getName() + ", " + id + ").");                   
	        	}                                 
	        }
	        
	        clazz = clazz.getSuperclass();
		}
        return action;
	}

	// restore object private fiedls 
	public static void restoreObjectState(Object object, IPropertyProvider provider){
		Class clazz = object.getClass();
		
		while(clazz != null){
			
			Field [] fields = clazz.getDeclaredFields();                
			for (int i = 0; i < fields.length; i++){  
				String name = fields[i].getName();                       
				if (name.startsWith(IN_PARAM_PREFIX)){
					try{                                      
						if (fields[i].getType().equals(String[].class)){
							String [] values = provider.getPropertyValues(name); 
							fields[i].setAccessible(true);                   
							fields[i].set(object, values); 
						} else {
						   String value = provider.getPropertyValue(name); 
						   if (value != null){              
						       // String              
							   if (fields[i].getType().equals(String.class)){
								   fields[i].setAccessible(true);                   
								   fields[i].set(object, value); 
							   } else {
							   		// int
								   if (fields[i].getType().equals(int.class)){
									   fields[i].setAccessible(true);                   
									   fields[i].set(object, new Integer(Integer.parseInt(value)));
								   } else {
								   		// boolean
								   		if(fields[i].getType().equals(boolean.class)){
								   			fields[i].setAccessible(true);                   
								   			fields[i].set(object, new Boolean(value));
								   		} else {				
								   			// int []
								   			if (fields[i].getType().equals(int[].class)){
								   				String [] values = provider.getPropertyValues(name); 
												fields[i].setAccessible(true);                   							   				
								   				int[] intArray = new int[values.length];
												for(int j=0;j<values.length;j++){
													intArray[j] = new Integer(values[j]).intValue();
												}
												fields[i].set(object, intArray);							   				
								   			} else {
								   				// long []
								   				if (fields[i].getType().equals(long[].class)){
								   					String [] values = provider.getPropertyValues(name); 
													fields[i].setAccessible(true);                   							   				
									   				long[] longArray = new long[values.length];
													for(int j=0;j<values.length;j++){
														longArray[j] = new Long(values[j]).longValue();
													}
													fields[i].set(object, longArray);
								   				} else {  
								   					// double
								   					if (fields[i].getType().equals(double.class)){
														   fields[i].setAccessible(true);                   
														   fields[i].set(object, new Double(Double.parseDouble(value)));
													} else {
														System.out.println("Unable to marshall parameter of this type (" + object.getClass().getName() + ", " + name + ").");
												    }
								   				}
								   			}
										}
								   }
							   }
						   }
						}                                                            
					} catch(Exception e){ 
					   // System.err.println("Access denied writing object property (" + object.getClass().getName() + ", " + name + ").");                   
					}                
				}           
			}
		
			clazz = clazz.getSuperclass();
		}
	}				
	
	protected IActionList getActionList(){
		return m_ActionList;		
	}
	
	protected IAction locateActionInActionList(int actionId) {
		for (int i=0; i < m_ActionList.size(); i++){
			if (m_ActionList.getAction(i).hashCode() == actionId){
				return m_ActionList.getAction(i);
			}
		}		
		return null;
	}
	
	// iterate over inner classes and try to invoke action
	private boolean dispathActionToView(int actionId, IPropertyProvider provider) {
		IAction action = findChildActionByName(this, actionId);
		if (action == null){
			action = locateActionInActionList(actionId);
		}
		if (action != null){  
			restoreObjectState(action, provider);
			action.execute(provider);
			return true;  
		}
		return false;
	}
	
	public boolean dispatchAction(int actionId, IPropertyProvider provider) {
		boolean done = dispathActionToView(actionId, provider);
		if (done){
			return true;
		} else {
			for (int i=0; i < m_Views.size(); i++){
				done = ((BaseView) m_Views.get(i)).dispatchAction(actionId, provider); 
				if (done){
					return true;
				}
			}
			return false;
		}
	} 
	
	public static String toFirstCap(String name){
		return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
	}
	
	protected BaseAction findActionOfClass(Object target, Class actionClass){
		return findChildActionByName(target, -1, actionClass);
	}
	
}
