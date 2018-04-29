package com.oy.tv.model.unit;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.util.StringNavigator;
import com.oy.tv.util.XmlUtil;

public class TranslationWeaver {

	public final static Set<String> nonparse;
	static {
		nonparse = new HashSet<String>();
		nonparse.add("LATEX");
		nonparse.add("DRAW");
	}

	public final static Set<String> leaf;
	static {
		leaf = new HashSet<String>();
		leaf.add("IMG");
		leaf.add("BR");
	}
	
	public final static Set<String> nonterminal;
	static {
		nonterminal = new HashSet<String>();
		nonterminal.add("SPAN");
		nonterminal.add("I");
		nonterminal.add("B");
		nonterminal.add("LARGE");
		nonterminal.add("HTML");
	}
		
	static String escape(String text){
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
	
	static String trimSpace(String text){
		text = text.replaceAll("\t", " ");
		text = text.replaceAll("\n", " ");
		while(true){
			int len = text.length();
			text = text.replaceAll("[ ][ ]", " ");
			if (len == text.length()){
				break;
			}
		}
		return text;
	}
	
	static String unlisp(String text, WeaveableTranslation lc) {
		StringBuffer sb = new StringBuffer();
		StringNavigator sn = new StringNavigator(text);
		while(sn.tryNext("${")){
			sb.append(sn.prev());
			sn.next("}");
			
			sb.append("<$" + lc.expressions.size() + "/>");
			lc.expressions.add("${" + sn.prev() + "}");			
		}
		sb.append(sn.next());
		return sb.toString();
	}
	
	static void linearize(TranslationContext ctx, Node node, WeaveableTranslation lc, String path) {
		NodeList list = node.getChildNodes();
		int sz = list.getLength();
		for (int i=0; i < sz; i++){
			Node item = list.item(i);
			if ("#text".equals(item.getNodeName())){
				String text;
				text = escape(item.getTextContent());
				text = trimSpace(text);
				text = unlisp(text, lc);
				if (text.trim().length() != 0){
					lc.append(text);
				}
			} else {
				if (leaf.contains(item.getNodeName().toUpperCase()) ||
						nonparse.contains(item.getNodeName().toUpperCase())){
					lc.append("<#" + lc.terminals.size() + "/>");
					lc.terminals.put(lc.terminals.size(), path + i);
					continue;
				}
				
				if (nonterminal.contains(item.getNodeName().toUpperCase())){
					lc.append(emitTag(item));
					linearize(ctx, item, lc, path + i + ".");
					lc.append("</" + item.getNodeName()+ ">");
					continue;
				}
				
				ctx.parseErrors.add("Uknown node: " + item.getNodeName());				
			}
		}
	}
	
	static String emitTag(Node node){
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(node.getNodeName());
		
		int sz = node.getAttributes().getLength();
		for (int i=0; i < sz; i++){
			Node attr = node.getAttributes().item(i);
			String value = attr.getNodeValue();
			value = value.replaceAll("[']", "&quote;");
			sb.append(" ");
			sb.append(attr.getNodeName());
			sb.append("=");
			sb.append("'");
			sb.append(value);
			sb.append("'");
		}
		sb.append(">");
				
		return sb.toString();
	}
	
	static WeaveableTranslation linearize(TranslationContext ctx, Node node) {
		WeaveableTranslation lc = new WeaveableTranslation();		
		linearize(ctx, node, lc, "");
		return lc;
	}
	
	private static void addTextNode(String text, Node parent) {
		try {
	    text = "<fragment>" + text + "</fragment>";
	    
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document d = builder.parse(
	        new InputSource(new StringReader(text)));
	    
	    NodeList list = d.getDocumentElement().getChildNodes();
	    int sz = list.getLength();
	    for (int i = 0; i < sz; i++) { 
	    	Node child = list.item(i);
		    Node node = parent.getOwnerDocument().importNode(child, true);
		    parent.appendChild(node);
	    }	    
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
		
	private static String expandInlineExpressions(WeaveableTranslation ctx, 
			List<String> errors) {
		
		// decode encoded encoded inline expressions, ie. <$1/>
		String literal = ctx.getNewValue(); 
		for (int i=0; i < ctx.expressions.size(); i++){
			String expr = ctx.expressions.get(i);
			String term = "<$" + i + "/>";
			if (literal.indexOf(term) != -1){
				literal = literal.replace(term, expr);
			} else {
				errors.add("Failed to resolve <$" + i + "/>.");
			}
		}
		return literal;
	}
	
	private static String replaceAll (String text, String find, String replace){
		StringBuffer sb = new StringBuffer();
		StringNavigator sn = new StringNavigator(text);
		
		if (sn.tryNext(find)) {
			sb.append(sn.prev());
			sb.append(replace);
			sb.append(sn.next());
			
			return sb.toString();
		} else {
			return null;
		}		
	}
	
	static Node getNodeForPath(Node node, String path) {
		int idx = path.indexOf(".");
		if (idx == -1) {
			int index = Integer.parseInt(path);
			NodeList nodes = node.getChildNodes();
			if (index < 0 || index >= nodes.getLength()) {
				throw new RuntimeException("Unable to resolve XML element path: " + path);
			}
			return nodes.item(index);
		} else {
			String prefix = path.substring(0, idx);
			String remainder = path.substring(idx + 1);
			int index = Integer.parseInt(prefix);
			NodeList nodes = node.getChildNodes();
			if (index < 0 || index >= nodes.getLength()) {
				throw new RuntimeException("Unable to resolve XML element path: " + path);
			}
			return getNodeForPath(nodes.item(index), remainder);
		}
	}
	
	static void weave(Node node, WeaveableTranslation ctx, List<String> errors){
		// resolve inline expression, ie. <$1/>
		String literal = expandInlineExpressions(ctx, errors);
		
		// resolve all inline terminals, ie. <#1/>
		Map<Integer, Node> terminals = new HashMap<Integer, Node>();
		{
			for (int id : ctx.terminals.keySet()) {
				String path = ctx.terminals.get(id);
				terminals.put(id, getNodeForPath(node, path));
			}
			
			for (int id : ctx.terminals.keySet()) { 
				String replace = replaceAll(literal, "<#" + id + "/>", 
						renderToXml(terminals.get(id)));
				if (replace != null) {
					literal = replace;
					terminals.remove(id);
				}
			}
		}
		
		// remove all current children
		while (true) {
			Node child = node.getFirstChild();
			if (child == null) {
				break;
			}
			node.removeChild(child);
		}

		// add new children from the text
		addTextNode(literal, node);

		// make sure all terminals are resolved
		while(terminals.size() > 0){
			int id = terminals.keySet().iterator().next();
			errors.add("Failed to resolve <#" + id + "/>.");
			terminals.remove(id);
		}
	}
	
	static Document traverse (UnitBO original, TranslationContext ctx, boolean full) {
		Document doc;
		try {
			doc = XmlUtil.loadXmlFrom(original.getXml());
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		
		// clear errors
		ctx.weaveErrors.clear();
		
		NodeList top = doc.getDocumentElement().getChildNodes();
		int szTop = top.getLength();
		for (int i=0; i < szTop; i++){
			Node node = top.item(i);
			
			if ("QUESTION".equals(node.getNodeName().toUpperCase())){
				if (full){
					weave(node, ctx.getQuestion(), ctx.weaveErrors);
				} else {
  				WeaveableTranslation lctx = linearize(ctx, node);
  				ctx.question = lctx;
				}
			}			

			int choiceIndex = 0;
			if ("CHOICES".equals(node.getNodeName().toUpperCase())){
				NodeList choices = node.getChildNodes();
				int szChoices = choices.getLength();				
				for (int j=0; j < szChoices; j++){
					final Node child = choices.item(j);
					final String name = child.getNodeName().toUpperCase();
					if ("ANSWER".equals(name) ||
							"DECOY".equals(name)){
						if (full){
							weave(child, ctx.choices.get(choiceIndex), ctx.weaveErrors);
						} else {
  						WeaveableTranslation lctx = linearize(ctx, child);
  						ctx.choices.add(lctx);
						}
						choiceIndex++;
					}
				}
			}
		}
		
		return doc;
	}

	public static TranslationContext extractTranslatable(UnitBO unit) {
		TranslationContext ctx = new TranslationContext();
		TranslationWeaver.traverse(unit, ctx, false);
		return ctx;
	}

	public static UnitBO weaveTranslatable(UnitBO unit, TranslationContext ctx) {
		UnitBO newUnit = new UnitBO();
		newUnit.setXml(renderToXml(TranslationWeaver.traverse(unit, ctx, true)));
		return newUnit;
	}
	
	public static String renderToXml(Document doc) {
		try {
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      
      trans.setOutputProperty(OutputKeys.INDENT, "yes");    
      trans.setOutputProperty( OutputKeys.METHOD, "xml");
      trans.setOutputProperty( OutputKeys.ENCODING, "UTF-8");
  
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(doc);
      trans.transform(source, result);
  		
      return sw.toString();
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public static String renderToXml(Node node) {
		try {
		
      // Set up the output transformer
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
  
      // Print the DOM node
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(node);
      trans.transform(source, result);
      return sw.toString();
    
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String removeWhiteSpace(String text) {
		while(true) {
			int len = text.length();
			text = text.replaceAll("\n", " ");
			text = text.replaceAll("\r", " ");
			text = text.replaceAll("\t", " ");
			text = text.replaceAll("[ ][ ]", " ");
			text = text.replaceAll("[ ][<]", "<");
			text = text.replaceAll("[>][ ]", ">");
			text = text.replaceAll("[ ][/][>]", "/>");
			if (len == text.length()) {
				break;
			}
		}
		return text.trim();
	}
}
