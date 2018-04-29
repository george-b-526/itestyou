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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileNavigator implements ITextNavigator {
 
	class TextBlok { 
		private String text;
		
		public TextBlok(){
			this.text = fetchBlock(reader);
		}
		 
		public TextBlok(String text){
			this.text = text;
		}
		
		public int indexOf(String value){
			int idx = text.indexOf(value);
			if (idx == -1){
				String block = fetchBlock(reader); 
				if ("".equals(block)){
					return -1;
				}
				text = text + block;
				return indexOf(value);
			}
			return idx;
		}
		
		public TextBlok substring(int from, int to){
			return new TextBlok(text.substring(from, to));
		}
		  
		public TextBlok substring(int from){
			return new TextBlok(text.substring(from));
		}
		
		private String fetchBlock(BufferedReader in) {
			final int MAX_BLOCK = 64 * 1024;
			  
			try {
		        StringBuffer sb = new StringBuffer();
		        String str;
		        while (true) {
		        	str = in.readLine();		  
		        	if (str == null) break;
		        	sb.append(str);
		        	sb.append("\n");
		        	
		        	if (sb.length() > MAX_BLOCK){
		        		break;
		        	} 
		        }
		        return sb.toString();
			} catch (Exception e){
				throw new RuntimeException();
			}
		}
	}
	
	private BufferedReader reader;
	private TextBlok prev;
	private TextBlok next;
	
	public String prev(){
		return prev.text;
	}
	 
	public String next(){
		return next.text;
	}
	
	public FileNavigator(InputStream is) throws IOException{
		reader = new BufferedReader(new InputStreamReader (is));
		
		next = new TextBlok();
	}
	
	public FileNavigator(File file) throws IOException{
		reader = new BufferedReader(new FileReader(file));		
		next = new TextBlok();
	}

	public FileNavigator(File file, String charset) throws IOException{
    reader = new BufferedReader(new InputStreamReader(
    		new FileInputStream(file), Charset.forName(charset)));
	
  	next = new TextBlok();
  }
	
	public void close() throws IOException {
		reader.close();
	}
	
	private static boolean hasNext(FileNavigator current, String separator){		
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

	private static void next(FileNavigator current, String separator){
		if (!hasNext(current, separator)){
			throw new RuntimeException("Failed for get next " + separator + " for " + current);
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
