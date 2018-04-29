package com.oy.tv.model.words.jp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class Jp {

	static boolean is(Node node, String name){
		if (node != null && node.getNodeName() != null){
			return name.toUpperCase().equals(node.getNodeName().toUpperCase());
		} else {
			return false;
		}
	}
	
	public static void main(String [] args) throws Exception {
    List<Word> wordList = new ArrayList<Word>();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse (new FileInputStream(new File(
    		"src/com/oy/tv/model/words/jp/genki/Genki I.xml")));
    
    NodeList words = doc.getElementsByTagName("item");
    int sz = words.getLength();
    for (int i=0; i < sz; i++){
    	Node node = words.item(i);
    	String id = node.getAttributes().getNamedItem("id").getTextContent();
    	
    	String a = null, q = null, c = null;
    	List<String> examples = new ArrayList<String>();
    	
    	NodeList kids = node.getChildNodes();
    	int szj = words.getLength();
      for (int j=0; j < szj; j++){
      	Node nodex = kids.item(j);
      	
      	if (is(nodex, "CAT")){
      		c = nodex.getTextContent();
      		continue;
      	}
      	if (is(nodex, "Q")){
      		q = nodex.getTextContent();
      		continue;
      	}
      	if (is(nodex, "A")){
      		a = nodex.getTextContent();
      		continue;
      	}      	
      }
    
      // process
      if (a != null && q != null){    		
      	int idx = a.indexOf(q);
      	if (idx != -1){
    			String sub = a.substring(idx + q.length()).trim();
    			System.out.println("Reduced: " + id);
    			a = sub; 
    		} else {
    			a = "[" + a.replaceAll("[\t]", "] ");    			
    		}
      }
      
      // rename
      c = c.replaceAll("[C][h][a][p][t][e][r]", "set");
            
      // add
  		Word w = new Word();
  		w.setWord(q);
  		w.setDefinition(a);
  		w.setType(c);
  		w.setCategory(c);

  		wordList.add(w);
    }

    WordRepository.toXMLByHand(
    		"JP'EN \"1001", wordList, new File("d:/tmp/JP-EN-600.xml"), true);
		
	}
	
}
