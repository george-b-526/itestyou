package com.oy.tv.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil {

	public static String getAttr(Node node, String name){
		Node attr = node.getAttributes().getNamedItem(name);
		if (attr != null){
			return attr.getNodeValue();			
		} else {  
			return null;
		}
	}
	
	public static void cloneNode(Node from, Node to){
		NodeList _list = from.getChildNodes();
		int _sz = _list.getLength();
		for (int j=0; j < _sz; j++){
			Node _node = _list.item(j);
			to.appendChild(_node.cloneNode(true));
		}
	}
	
	public static void removeAllNodesBy(Document doc, String selector) throws Exception {
		NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(doc, selector);
		int sz = list.getLength();
		for (int i=0; i < sz; i++){
			Node node = list.item(i);
			node.getParentNode().removeChild(node);
		}
	}
	
	public static void expandToDepth(Node node, List<Node> all, int maxNodes) throws IllegalArgumentException {
		if (all.size() > maxNodes){
			throw new IllegalArgumentException("Too many nodes " + maxNodes);
		}
		
		NodeList nl = node.getChildNodes();  
		int sz = nl.getLength();
		for (int i=0; i < sz; i++){
			Node item = nl.item(i);
			
			if (item.getNodeType() == Node.ELEMENT_NODE || item.getNodeType() == Node.TEXT_NODE){
				all.add(item);
				expandToDepth(item, all, maxNodes);
			}
		}
	}
	
	public static List<Node> expandToDepth(Document doc, int maxNodes) throws IllegalArgumentException {
		List<Node> all = new ArrayList<Node>();
		all.add(doc.getDocumentElement());
		expandToDepth(doc.getDocumentElement(), all, maxNodes);
		return all;  
	}
	  
	public static Document createDomDocument() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.newDocument();
        return doc;
    }
	
	public static void writeXmlTo(Node node, Writer sw) throws Exception {
        // Prepare the DOM document for writing
        Source source = new DOMSource(node);
        
        // Prepare the output file
        Result result = new StreamResult(sw);
        
        // Write the DOM document to the file   
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        xformer.transform(source, result);
    }  
	
	public static void writeXmlFile(Document doc, String fileName) throws Exception {
        // Prepare the DOM document for writing
        Source source = new DOMSource(doc);
        
        // Prepare the output file
        File file = new File(fileName);
        Result result = new StreamResult(file);
        
        // Write the DOM document to the file   
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    }
	
	public static Document loadXml(String fileName) throws Exception {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();		
		fact.setNamespaceAware(false);
		fact.setValidating(false);
		fact.setExpandEntityReferences(false);
		
		DocumentBuilder docb = fact.newDocumentBuilder();
        
		return docb.parse(new FileInputStream(fileName));
	}
	
	public static Document loadXmlFrom(String xml) throws Exception {
		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();		
		fact.setNamespaceAware(false);
		fact.setValidating(false);
		fact.setExpandEntityReferences(false);
		DocumentBuilder docb = fact.newDocumentBuilder();
		return docb.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	}
	
}
