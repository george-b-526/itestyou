package com.oy.tv.model.words.gmat;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

public class Gmat {

	static boolean is(Node node, String name){
		if (node != null && node.getNodeName() != null){
			return name.toUpperCase().equals(node.getNodeName().toUpperCase());
		} else {
			return false;
		}
	}
	
	public static void main(String [] args) throws Exception {
    List<Word> wordList = new ArrayList<Word>();
    List<Word> wordListEx = new ArrayList<Word>();
		
    
    Set<String> types = new HashSet<String>();
    
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse (new FileInputStream(new File(
    		"src/com/oy/tv/model/words/gmat/GMAT.XML")));
    
    NodeList words = doc.getElementsByTagName("item");
    int sz = words.getLength();
    for (int i=0; i < sz; i++){
    	Node node = words.item(i);
    	
    	String a = null, q = null, _q = null, c = null;
    	
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
    
      // type
      StringNavigator sn = new StringNavigator(a);
      sn.next(".  ");
      String type = sn.prev();
      a = sn.next().trim();
      types.add(type);
      
      // answer
    	_q = q;
      sn = new StringNavigator(a);
      if (sn.tryNext("[")){
      	sn.next("]");
      	q = q + " [" + sn.prev().trim() + "]";
      	a = sn.next().trim();
      }
      
      // E. G.
      sn = new StringNavigator(a);
      if (sn.tryNext("E.G.")){
      	a = sn.prev().trim();
      	q = q + " (e.g., " + sn.next().trim() + ")";      
      } 
      
      // spaces and new lines
      a = a.replaceAll("[ ][ ]", " ");
      a = a.replaceAll("[ \n]", " ");
      q = q.replaceAll("[ ][ ]", " ");
      q = q.replaceAll("[ \n]", " ");
      
      // add
      {
    		Word w = new Word();
    		w.setWord(q);
    		w.setDefinition(a);
    		w.setType(type);
    		w.setCategory("");
  
    		wordListEx.add(w);
      }
      
      {
    		Word w = new Word();
    		w.setWord(_q);
    		w.setDefinition(a);
    		w.setType(type);
    		w.setCategory("");
  
    		wordList.add(w);
      }
    }

    System.out.println(types);
    
    WordRepository.toXMLByHand(
    		"GMAT 1500", wordList, new File("d:/tmp/GMAT-5000.xml"), true);

    WordRepository.toXMLByHand(
    		"GMAT 1500 (with example and forms)", wordListEx, new File("d:/tmp/GMAT-5000-ex.xml"), true);

	}
	
}
