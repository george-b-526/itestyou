package com.oy.tv.model.words.es;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oy.tv.model.vocb.Word;
import com.oy.tv.model.vocb.WordRepository;
import com.oy.tv.util.StringNavigator;

class Comment {
	String from;
	String to;
}

class WordEx {
	String term;
	String meaning;
	List<Comment> comments = new ArrayList<Comment>();
}

public class MnemosyneImport {

	static boolean is(Node node, String name){
		if (node != null && node.getNodeName() != null){
			return name.toUpperCase().equals(node.getNodeName().toUpperCase());
		} else {
			return false;
		}
	}
		
	static Map<String, WordEx> map = new HashMap<String, WordEx>();
	
	public static void main(String [] args) throws Exception {
				
    List<Word> wordList = new ArrayList<Word>();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse (new FileInputStream(new File(
    		"src/com/oy/tv/model/words/es/1001-Most-Useful-Spanish-Words.xml")));
    
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
      
    	if (a != null && q != null && c != null){    		
    		StringNavigator sn;
    		
    		// extract comments
    		{
      		sn = new StringNavigator(a);
      		if (sn.tryNext("\n")) {
      			a = sn.prev();
  
      			sn = new StringNavigator(sn.next().trim());
      			while(sn.tryNext("\n\n")) {
      				examples.add(sn.prev().trim());
      			}    			
      			examples.add(sn.next().trim());      			
      		}
    		}

    		// validate
    		{
      		sn = new StringNavigator(a);
      		if (sn.tryNext("\n")) throw new Exception(a);
  
      		sn = new StringNavigator(q);
      		if (sn.tryNext("\n")) throw new Exception(q);
    		}
    		
    		// create
    		WordEx word = new WordEx();
    		word.term = q;
    		word.meaning = a; 		
    		if (examples.size() != 0){
      		for (String comment : examples){
          	String [] parts = comment.split("\n");
          	if (parts.length != 2) {
          		throw new Exception(comment);
          	}
          	
          	Comment comm = new Comment();
          	comm.from = parts[0].trim();
          	comm.to = parts[1].trim();
          	
          	if (comm.from.length() < 7 || comm.to.length() < 7) {
          		throw new Exception(comment);
          	}
          	
          	word.comments.add(comm);
          }
    		}
    		
    		// map
    		if ("1001 Most Useful Spanish Words".equals(c.trim())){
    			c = "1001 Most Useful Spanish Words - Común";
    		}    		
    		c = c.substring(33);
    		
    		
    		Word w = new Word();
    		w.setWord(word.term);
    		w.setDefinition(word.meaning);
    		w.setType(c);
    		w.setCategory(c);
    		    		
    		wordList.add(w);
    		
    		map.put(c.trim(), word);
    	} else {
    		throw new RuntimeException("Problem: " + id + ": " + a + q + c);
    	}    	
    }

    WordRepository.toXML(
    		"ES-EN 1001", wordList, new File("d:/tmp/ES-1001.xml"), true);
    
    System.out.println(map);
	}

}
