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


package com.oy.shared.hmvc.util;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringEscapeUtils;

import com.oy.shared.hmvc.IPropertyProvider;

public class HtmlUtil {

	public static String escapeJavaScript(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.escapeJavaScript(text);
		}
	}
	
	public static String unescapeJavaScript(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.unescapeJavaScript(text);
		}
	}
	
	public static String escapeHTML(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(text);
		}		
	}
	
	public static String unescapeHTML(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.unescapeHtml(text);
		}
	}
	
	public static void restoreObjectState(Object object, IPropertyProvider provider, String in_param_prefix){
		
		Class clazz = object.getClass();
		
		while(clazz != null){
			
			Field [] fields = clazz.getDeclaredFields();                
			for (int i = 0; i < fields.length; i++){  
				String name = fields[i].getName();                       
				if (name.startsWith(in_param_prefix)){
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
								   					System.out.println("Unable to marshall parameter of this type (" + object.getClass().getName() + ", " + name + ").");
								   				}
								   			}
										}
								   }
							   }
						   }
						}                                                            
					} catch(Exception e){ 
					   //System.err.println("Access denied writing object property (" + object.getClass().getName() + ", " + name + ").");                   
					}                
				}           
			}
		
			clazz = clazz.getSuperclass();
		}
	}
	
}
