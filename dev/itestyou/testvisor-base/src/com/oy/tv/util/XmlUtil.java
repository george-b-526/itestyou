package com.oy.tv.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class XmlUtil {
	
	public static Document parse(InputSource is) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    
    Document doc;
    try {
    	doc = docBuilder.parse (is);
    } catch (SAXParseException spe){
    	String err = spe.toString() +
        "\n  Line number: " + spe.getLineNumber() +
        "\nColumn number: " + spe.getColumnNumber()+
        "\n Public ID: " + spe.getPublicId() +
        "\n System ID: " + spe.getSystemId() ;
    	System.out.println( err );

    	spe.printStackTrace();
    	throw spe;
    }
    return doc;
	}

	public static Document parse(InputStream is) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    
    Document doc;
    try {
    	doc = docBuilder.parse (is);
    } catch (SAXParseException spe){
    	String err = spe.toString() +
        "\n  Line number: " + spe.getLineNumber() +
        "\nColumn number: " + spe.getColumnNumber()+
        "\n Public ID: " + spe.getPublicId() +
        "\n System ID: " + spe.getSystemId() ;
    	System.out.println( err );

    	spe.printStackTrace();
    	throw spe;
    }
    
    return doc;
	}
	
	public static String xml2string(Document doc) throws Exception {
		StringWriter sw = new StringWriter();
		writeXML(doc, sw);
		return sw.toString();
	}
	
	public static void writeXML(Document doc, Writer sw) throws Exception {
    TransformerFactory transfac = TransformerFactory.newInstance();
    Transformer trans = transfac.newTransformer();
    
    trans.setOutputProperty(OutputKeys.INDENT, "yes");    
    trans.setOutputProperty( OutputKeys.METHOD, "xml");
    trans.setOutputProperty( OutputKeys.ENCODING, "UTF-8");

    StreamResult result = new StreamResult(sw);
    DOMSource source = new DOMSource(doc);
    trans.transform(source, result);
	}
	
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
	
    // Parses a string containing XML and returns a DocumentFragment
    // containing the nodes of the parsed XML.
    public static DocumentFragment textToFragment(Document doc, String fragment) {
		try {
			// Create a DOM builder and parse the fragment
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document d = factory.newDocumentBuilder().parse(
					new InputSource(new StringReader("<foo>" + fragment + "</foo>")));

			// Import the nodes of the new document into doc so that they
			// will be compatible with doc
			Node node = doc.importNode(d.getDocumentElement(), true);

			// Create the document fragment node to hold the new nodes
			DocumentFragment docfrag = doc.createDocumentFragment();

			// Move the nodes into the fragment
			while (node.hasChildNodes()) {
				docfrag.appendChild(node.removeChild(node.getFirstChild()));
			}
 
			// Return the fragment
			return docfrag;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
    public static void writeXmlTo(Node node, Writer sw) throws Exception {
    	if (node.getNodeType() == Node.TEXT_NODE){
    		String value = node.getNodeValue();
    		if (value.startsWith("\"") && value.endsWith("\"")){
    			value = value.substring(1, value.length() - 1);
    		}
    		sw.write(value);	
    	} else {
    		writeXmlToEx(node, sw);
    	}
    }  
    
	public static void writeXmlToEx(Node node, Writer sw) throws Exception {
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

	public static NodeList query(Node node, String expr) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    Object result = xpath.compile(expr).evaluate(node, XPathConstants.NODESET);
    return (NodeList) result;
	}
		
	public static String getNodeAttribute(Node node, String xpath, String name)
			throws Exception { 
		NodeList nodes = query(node, xpath);
		if (nodes.getLength() != 1) {
			return null;
		}
		return XmlUtil.getAttr(nodes.item(0), name);
	}
	
}
