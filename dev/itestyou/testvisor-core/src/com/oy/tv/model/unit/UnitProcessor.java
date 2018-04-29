package com.oy.tv.model.unit;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.sf.yacas.YacasEvaluatorEx;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.util.StringNavigator;
import com.oy.tv.util.XmlUtil;

public class UnitProcessor {
	
	public static final String [] LETTERS = new String [] {"A", "B", "C", "D", "E", "F", "G", "H"};

	public UnitContext _ctx;
	public RenderContext _rtx;
	public Document _doc;
	public YacasEvaluatorEx _eval;
	public PrintStream _log;
	public IPropertyProvider _pp;
	
	private boolean _canLog = false;
	
	private String eval(String input) throws Exception {
		if (input == null){
			return null;
		}
		input = input.trim();   
		if (input.length() == 0){
			return null;
		}
		
		_eval.flush();
		String out = _eval.eval(input);
		String echo = _eval.flush();

		if(_canLog && _log != null){
			_log.println("In> " + input);
			_log.println("Echo>" + echo);
			_log.println("Out>" + out);
		}
		
		classifyEcho(echo);
		
		return echo;
	}
	
	private void stmt(Node node) throws Exception {
		String input = node.getTextContent();
		eval(input);
   
		node.getParentNode().removeChild(node);
	}
	
	private void classifyEcho(String echo){
		if (echo.indexOf("Sqrt(") != -1){
			_ctx.kind.hasSqrt = true;
		}
		
		if (echo.indexOf("/") != -1){
			_ctx.kind.hasFraction = true;
		}
		
		if (echo.indexOf("-") != -1){
			_ctx.kind.hasMinus = true;
		}
		 
		if (echo.indexOf("Complex(") != -1){
			_ctx.kind.hasComplex = true;
		}
	}

	public void evalRaw(String input) throws Exception {
		eval(input);
	}
	
	public String echo(String input) throws Exception {
		return eval("Echo(" + input + ")");
	}
	
	private void echoNodeAttributes(Node node) throws Exception {
		NamedNodeMap map = node.getAttributes();
		if (map != null){
			int sz = map.getLength(); 
			for (int i=0; i < sz; i++){
				Node item = map.item(i);
				
				String value = item.getNodeValue();
				String echo = evalEcho(value);
				item.setNodeValue(echo);
			}
		}
	}
	
	private String evalEcho(String input) throws Exception {
		StringNavigator sn = new StringNavigator(input);
		StringBuffer sb = new StringBuffer();
		while(true){
			boolean has = sn.tryNext("${");
			if (!has) break;
			sb.append(sn.prev());
			sn.next("}");
			
			String echo = echo(sn.prev()).trim();
			sb.append(echo);
		}
		sb.append(sn.next());
  
		return sb.toString();
	}
	
	private void echoTextNode(Node node) throws Exception {
		node.setTextContent(evalEcho(node.getTextContent()));
	}
		    
	private void draw(Node node, boolean eval) throws Exception {
		String input = node.getTextContent();
		    
		String echo;
		if (eval){
			echo = eval("Echo(TeXForm(Hold(" + input + ")))");

			// remove leading '$' and trailing '$'
			if(echo != null && echo.trim().length() >= 2){
				echo = echo.trim().substring(1).trim();
				echo = echo.substring(0, echo.length() - 1);
				echo = echo.trim();
			}  
		} else{ 
			echo = input;
		}
		
		if ("latex".equals(node.getParentNode().getNodeName())){
			Node text = node.getOwnerDocument().createTextNode(echo);
			
			node.getParentNode().insertBefore(text, node);
			node.getParentNode().removeChild(node);
		} else {
			Element img = node.getOwnerDocument().createElement("img");
			img.setAttribute("src", ResourceLocator.getMathMLBaseHREF(_pp) + echo);
			img.setAttribute("style", "vertical-align: middle;");
	  		  
			node.getParentNode().insertBefore(img, node);
			node.getParentNode().removeChild(node); 
		}
	} 
	
	private void yacasPhase() throws Exception {  
		List<Node> all = XmlUtil.expandToDepth(_doc, 5000);
		
		if (all.size() == 0){
			throw new UnitParseException("Expected non empty XML document.");
		}
		
		if (!"unit".equals(all.get(0).getNodeName())){
			throw new UnitParseException("Expected top level element 'unit'.");
		}

		for (int i=1; i < all.size(); i++){
			Node node  = all.get(i);
			
			echoNodeAttributes(node);
			
			if ("eval".equals(node.getNodeName())){
				continue;
			}  
			
			if ("stmt".equals(node.getNodeName())){
				stmt(node);
				continue;
			}			
  			
			if ("#text".equals(node.getNodeName())){
				echoTextNode(node);
				continue;
			}
		}
 		
 		XmlUtil.removeAllNodesBy(_doc, "//eval");
	}
	
	private void envPhase() throws Exception {
		String bindings = XmlUtil.getAttr(_doc.getDocumentElement(), "bindings");
		String defaults = XmlUtil.getAttr(_doc.getDocumentElement(), "defaults");
		
		if (bindings == null || bindings.trim().length() == 0){
			throw new RuntimeException("Expected attribute 'bindings'.");
		}
		if (defaults == null || defaults.trim().length() == 0){
			throw new RuntimeException("Expected attribute 'defaults'.");
		}  

		String bindingDefault = bindings + " := " + defaults;
		eval(bindingDefault);    
		if (_ctx.values != null){			  
			int countDefault = bindings.split(",").length;
			int coutCustom = _ctx.values.split(",").length;
			if (countDefault != coutCustom){  
				_ctx.addError("Number of variables '" + bindings + "' and the custom values '" + _ctx.values + "' does not match, using default values only.");
			} else {     
				String bindingCustom = bindings + " := " + _ctx.values;
				eval(bindingCustom);
			}
		}    
		
		XmlUtil.removeAllNodesBy(_doc, "unit/table");
	}
	
	private void htmlPhase() throws Exception {
		{
			NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "//html");
			int sz = list.getLength();
			for (int i=0; i < sz; i++){
				Node node = list.item(i);
				
				StringWriter sw = new StringWriter();
				writeChildrenXmlTo(node, sw);
         
				DocumentFragment raw = XmlUtil.textToFragment(
					node.getOwnerDocument(), sw.getBuffer().toString()
				);
				node.getParentNode().insertBefore(raw, node);  
				node.getParentNode().removeChild(node);
			}
		}
	}
	
	private void latexPhase() throws Exception {
		{
			NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "//draw");
			int sz = list.getLength();
			for (int i=0; i < sz; i++){
				Node node = list.item(i);
				draw(node, true);
			}
		}  
		
		{
			NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "//latex");
			int sz = list.getLength();
			for (int i=0; i < sz; i++){
				Node node = list.item(i);
				draw(node, false);
			}  
		}
	}	
	
	private static void writeChildrenXmlTo(Node node, Writer sw) throws Exception {
		NodeList list = node.getChildNodes();
		int sz = node.getChildNodes().getLength();
		for (int i=0; i < sz; i++){
			XmlUtil.writeXmlTo(list.item(i), sw);
		}
	}  
	
	private void surveyPhase() throws Exception {
		{
			StringWriter sw = new StringWriter();
			
			NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "unit/question");
			int sz = list.getLength();
			if (sz != 1){
				throw new RuntimeException("Expected one 'unit/question'.");
			}
			Node question = list.item(0);
			
			writeChildrenXmlTo(question, sw);
		  				
			_ctx.question = sw.getBuffer().toString();
			sw.getBuffer().setLength(0);
		}
		
		{
			NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "unit/choices");
			int sz = list.getLength();
			if (sz != 1){
				throw new RuntimeException("Expected one 'unit/choices'.");
			}
			Node choices = list.item(0);
			
			list = choices.getChildNodes();
			sz = list.getLength();
			int idx = 0;
			for(int i=0; i < sz; i++){
				Node choice = list.item(i);

				StringWriter sw = new StringWriter();
				writeChildrenXmlTo(choice, sw);
				 
				if ("DECOY".equals(choice.getNodeName().toUpperCase())){
					_ctx.choices.add(sw.getBuffer().toString());
					idx++;
					continue;
				}
				if ("ANSWER".equals(choice.getNodeName().toUpperCase())){
					_ctx.choices.add(sw.getBuffer().toString());
					_ctx.answerIndexes.add(new Integer(idx));
					idx++;
					continue;
				}
			}
		}  
		
		if (_ctx.answerIndexes.size() == 0){
			_ctx.addError("Expected <answer/>.");
		}
		if (_ctx.choices.size() < 1){
			_ctx.addError("Expected 1 or more choices."); 
		}		
  
		{
			replaceNodes("//question", "p", new String [] {"style"}, new String [] {"font-weight: bold;"});
			replaceNodes("//choices", "ol", new String [] {"type"}, new String [] {"A"});
			replaceNodes("//answer", "li", new String [] {"style"}, new String [] {"padding: 12px;"});
			replaceNodes("//decoy", "li", new String [] {"style"}, new String [] {"padding: 12px;"});
		}
	}
	
	private void replaceNodes(String selector, String newNodeName, String [] names, String [] values) throws Exception {
		NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, selector);
		int sz = list.getLength();
		for (int i=0; i < sz; i++){
			Node node = list.item(i);
			Element newNode = node.getOwnerDocument().createElement(newNodeName);
			for(int j=0; j<names.length; j++){
				newNode.setAttribute(names[j], values[j]);
			}  
			renameNode(node, newNode);  
		}
	}
	  
	private void renameNode(Node from, Node to){
		NodeList _list = from.getChildNodes();
		int _sz = _list.getLength();
		for (int j=0; j < _sz; j++){
			Node _node = _list.item(j);
			to.appendChild(_node.cloneNode(true));
		}
		
		from.getParentNode().insertBefore(to, from);
		from.getParentNode().removeChild(from);
	}

	private static boolean isInTheList(String item, List<String> list, int exceptIndex){
		for (int i=0; i < list.size(); i++){
			if (i == exceptIndex) continue;
			if (item.trim().equals(list.get(i).trim())){
				return true;
			}
		}
		
		return false;
	}
	      
	public static void render(UnitContext ctx, RenderContext rtx, Writer sw) throws IOException {
		sw.write("<div class='challenge'>");   
		sw.write("<p class='quest'>" + ctx.question +  "</p>");
		sw.write("<ol>");    
		  
		for (int i=0; i < ctx.choices.size(); i++){
			String style = "list-style-type: none;"; 
			if (rtx != null && rtx.showAnswer){
				if (ctx.answerIndexes.contains(i)){
					style = style + "border: 2px dotted #808080;";
				}    
			}
			  
			if (isInTheList(ctx.choices.get(i), ctx.choices, i)){
				ctx.addWarning("Duplicate values for choice '" + i + "'.");
				style = style + "background-color: FFA0A0;";  
			}  
			   
			sw.write(
				"<li style='" + style + "'>" + 
					"<b>" +  LETTERS[i] + "</b>.&nbsp;"
					+  ctx.choices.get(i) + 
				"</li>"
			);
		}
		sw.write("</ol>");
		sw.write("</div>");
	}    
	
	public void evaluate() throws Exception {
		envPhase();
		yacasPhase();
		latexPhase();
		htmlPhase();
		surveyPhase();		
	}  
	
	public void setLocale( String locale) throws Exception {
		_eval.setLocale(locale);
	}
	
	public void render(Writer sw) throws Exception {
		try {  
			evaluate();
		} catch (Exception e){ 
			_ctx.addError(e.getMessage());
		}	
		render(_ctx, _rtx, sw);  
	} 
  
}