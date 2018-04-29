package com.oy.tv.model.vocb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.oy.tv.util.XmlUtil;

public class WordRepository {

	public static class WordComparator implements Comparator<Word> {
    public int compare(Word o1, Word o2) {
	    return o1.getWord().compareTo(o2.getWord());
    }
	}

	public static WordSet fromXML(InputStream is) throws Exception {
    return fromXML(XmlUtil.parse(is));
	}
	
	public static WordSet fromXML(InputSource is) throws Exception {
    return fromXML(XmlUtil.parse(is));
	}
	
	private static WordSet fromXML(Document doc) throws Exception {
    WordSet result = new WordSet();
    result.words = new ArrayList<Word>();
    
    result.name = doc.getDocumentElement().getAttribute("name");
    result.lang = doc.getDocumentElement().getAttribute("lang");
    
    NodeList words = doc.getElementsByTagName("w");
    int sz = words.getLength();
    for (int i=0; i < sz; i++){
    	Word word = new Word();
    	Node node = words.item(i);
    	
    	word.word = node.getAttributes().getNamedItem("e").getTextContent();
    	word.id = Integer.parseInt(node.getAttributes().getNamedItem("i").getTextContent());
    	word.definition = node.getAttributes().getNamedItem("m").getTextContent();
    	word.type = node.getAttributes().getNamedItem("t").getTextContent();
    	
    	// this is new optional attribute
    	if (node.getAttributes().getNamedItem("c") != null){
    		word.cat = node.getAttributes().getNamedItem("c").getTextContent();
    	}
    	
    	result.words.add(word);
    }
    
    result.index(false);
    
    return result;
	}

	public static void toXML(String name, List<Word> words, Writer fw, boolean updateId) throws Exception {
		WordSet ws = new WordSet();
		ws.name = name;
		ws.words = words;
		ws.index(updateId);

		toXML(ws, fw);
	}
	
	public static String escape(String text){
		if (text == null){
			return "";
		} 
		
		// encode '&', unless encodes entity '&#NNNNN;'
		{
  		StringBuffer sb = new StringBuffer();
  		for (int i=0; i < text.length(); i++){
  			char c = text.charAt(i);
  			if (c == '&'){
  				if (i < text.length() - 1 && text.charAt(i + 1) == '#'){
  					sb.append(c);
  				} else {
  					sb.append("&amp;");
  				}
  			} else {
  				sb.append(c);
  			}
  		}		
  		text = sb.toString();
		}
		
		text = text.replaceAll("[<]", "&lt;");
		text = text.replaceAll("[>]", "&gt;");
		text = text.replaceAll("[\"]", "&quot;");
		return text;
	}

	public static void toXMLByHand(String name, List<Word> words, File file, boolean updateId) throws Exception {
		WordSet ws = new WordSet();
		ws.name = name;
		ws.words = words;
		ws.index(updateId);

		Writer sw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		
		sw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sw.write("<vocb name=\"" + escape(ws.name) + "\">\n");
		sw.write("<!--generated: " + new Date() + " -->\n");
		sw.write("<!--legend: e = expression, i = id, m = meaning, t = type, c = category-->\n");		
		for (Word word : ws.getWords()){
			sw.write("<w c=\"" + escape(word.getCategory()) + "\" e=\"" + escape(word.getWord()) + "\" i=\"" + word.getId() + "\" m=\"" + escape(word.getDefinition()) + "\" t=\"" + escape(word.getType()) + "\" />\n");
		}
		sw.write("</vocb>");
	
    sw.close();
	}
	
	public static void toXML(String name, List<Word> words, File file, boolean updateId) throws Exception {
		toXML(name, words, new FileWriter(file), updateId);
	}

	public static void toXMLWithUtf8(String name, List<Word> words, File file, boolean updateId) throws Exception {
		toXML(name, words, new OutputStreamWriter(new FileOutputStream(file), "UTF-8"), updateId);
	}
	
	public static void toXML(WordSet wordSet, File file) throws Exception {   
    Writer sw = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");    
		toXML(wordSet, sw);
    sw.close();
	}
	
	public static void toXML(WordSet wordSet, Writer sw) throws Exception {
    DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();

    Element root = doc.createElement("vocb");
    root.setAttribute("name", wordSet.getName());
    if (wordSet.getLang() != null){
    	root.setAttribute("lang", wordSet.getLang());
    }
    doc.appendChild(root);

    Comment comment;
    comment = doc.createComment("publisher: Copyright (C) ITestYou.com; All Rights Reserved");
    root.appendChild(comment);
    comment = doc.createComment("modified: " + new Date());
    root.appendChild(comment);
    comment = doc.createComment("legend: e = expression, i = id, m = meaning, t = type, c = category");
    root.appendChild(comment);
    
    for (Word word : wordSet.getWords()){
      Element child = doc.createElement("w");
      child.setAttribute("e", word.getWord());
      child.setAttribute("i", "" + word.getId());
      child.setAttribute("m", word.getDefinition());
      child.setAttribute("t", word.getType());
      child.setAttribute("c", word.getCategory());
      
      root.appendChild(child);    	
    }
    
    XmlUtil.writeXML(doc, sw);
	}

	public static WordSet load() throws Exception {
  	return WordRepository.load(WordRepository.class.getClassLoader().getResourceAsStream(
  			"vocb/sat-words-list.bin"
  	));
	}
	
	public static WordSet load(InputStream in) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(in);
		WordSet ws = (WordSet) ois.readObject();
		ws.index();
		return ws;
	}
	
	public static void save(String name, List<Word> words, File out) throws Exception {
		WordSet ws = new WordSet();
		ws.name = name;
		ws.words = new ArrayList<Word>();
		ws.words.addAll(words);
		
		Collections.sort(ws.words, new WordRepository.WordComparator());
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out));
		oos.writeObject(ws);
		oos.close(); 
	}
	
}
