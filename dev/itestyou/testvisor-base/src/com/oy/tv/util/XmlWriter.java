package com.oy.tv.util;

import java.io.FileWriter;
import java.io.IOException;

public class XmlWriter {

	public final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
	
	public FileWriter fw;
	
	private int arity;
	
	public void begin(String title, String [] names, String [] types, String [] descs) throws IOException {
		if (names.length != types.length || names.length != descs.length){
			throw new RuntimeException("Expected arrays of identical size");
		}
		arity = names.length;
		
		fw.write(XML_HEAD);  
		     
		fw.write("<table version='1.0'>\n");
		fw.write("  <schema name='" + title + "'>\n");
		for (int i = 0; i < arity; i++){  
			fw.write("    <column name='" + names[i] + " ' type='" + types[i] + "' desc='" + descs[i] + "'/>\n");
		}
		fw.write("  </schema>\n");
		fw.write("  <data>\n");
	}    
  	
	public void write(String [] values) throws IOException {
		if (arity != values.length){
			throw new RuntimeException("Expected arrays of identical size");
		}
		  
		fw.write("    <item");
		for (int i=0; i < arity; i++){
			fw.write(" _" + i + "='" + values[i] + "'");
		}        
		fw.write("/>\n");
	}
	
	public void end() throws IOException {
		fw.write("  </data>\n");
		fw.write("</table>");
		fw.close();
	}
	
}
