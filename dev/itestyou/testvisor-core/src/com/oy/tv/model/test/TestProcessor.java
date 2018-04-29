package com.oy.tv.model.test;

import java.io.PrintStream;
import java.io.StringWriter;

import net.sf.yacas.YacasEvaluatorEx;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.util.XmlUtil;

public class TestProcessor {

	public Document _doc;
	public YacasEvaluatorEx _eval;
	public PrintStream _log;
	
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

		if(_log != null){
			_log.println("In> " + input);
			_log.println("Echo>" + echo);
			_log.println("Out>" + out);
		}
		
		return echo;
	}
	
	private void unit(Node node) throws Exception {
		eval("restart");
		
		String type = XmlUtil.getAttr(node, "type");

		StringWriter sw = new StringWriter();
		UnitProcessor up = new UnitProcessor();
		
		up._doc = XmlUtil.loadXml("library/main/" + type + ".unit.xml");
		up._ctx = new UnitContext();
		up._ctx.values = XmlUtil.getAttr(node, "values");
		up._log = _log;
		up._eval = _eval;
		  
		up.render(sw);
  		
		sw.flush();
		sw.getBuffer().toString();
		
		//Element div = up._doc.createElement("div");
	  	//XmlUtil.cloneNode(up._doc.getDocumentElement(), div);
		  	  
//	  	Node _div = _doc.importNode(div, true);
//	  	node.getParentNode().insertBefore(_div, node);
//	  	node.getParentNode().removeChild(node);	
	}
	
	public void transform() throws Exception {
		NodeList list = com.sun.org.apache.xpath.internal.XPathAPI.selectNodeList(_doc, "test/unit");
		int sz = list.getLength();
		for (int i=0; i < sz; i++){
			Node node = list.item(i);
			
			unit(node);
		}
	}
	
}
