/*
	CVS2SQL - CVS Log to SQL Database Transformer
	
	Copyright (C) 2007 Pavel Simakov
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

package com.oy.tv.util;

public class StringNavigator implements ITextNavigator {
 
	class TextBlok {
		private String text;
		
		public TextBlok(String text){
			this.text = text;
		}
		 
		public int indexOf(String value){			 
			return text.toUpperCase().indexOf(value.toUpperCase());
		}
		
		public TextBlok substring(int from, int to){
			return new TextBlok(text.substring(from, to));
		}
		
		public TextBlok substring(int from){
			return new TextBlok(text.substring(from));
		}
	}
	
	private TextBlok prev;
	private TextBlok next;
	
	public String prev(){
		return prev.text;
	}
	 
	public String next(){
		return next.text;
	}
	
	public int firstOf(String [] separators){
		int first = -1;
		int firstIdx = -1;
		     
		for (int i=0; i < separators.length; i++){
			int idx = next.indexOf(separators[i]);
			if (idx != -1){
				if (idx < firstIdx){
					firstIdx = idx;
					first = i;
  				}
			}
		}  
		
		return first;
	}
	
	public StringNavigator(String value){
		next = new TextBlok(value);
	}
	
	private static boolean hasNext(StringNavigator current, String separator){		
		if (current.next == null){
			return false;
		}
		
		int idx = current.next.indexOf(separator);
		if (idx == -1){
			return false;
		}
		
		current.prev = current.next.substring(0, idx);
		current.next = current.next.substring(idx + separator.length());
		return true;
	}

	private static void next(StringNavigator current, String separator){
		if (!hasNext(current, separator)){
			throw new RuntimeException("Failed for get next " + separator + " for " + current + " to '" + separator + "'");
		}
	}
	
	public boolean hasNext(String separator){
		if (next == null){
			return false;
		}
		
		return next.indexOf(separator) != -1;
	}
	
	public boolean tryNext(String separator){
		return hasNext(this, separator);
	}
	
	public void next(String separator){
		next(this, separator);
	}
	
}
