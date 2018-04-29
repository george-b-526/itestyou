package com.oy.tv.model.unit;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.oy.tv.util.XmlUtil;

public class TranslationContext {
	static final long serialVersionUID = 0;
	
	WeaveableTranslation question;
	public List<WeaveableTranslation> choices = new ArrayList<WeaveableTranslation>();
		
	public List<String> parseErrors = new ArrayList<String>();
	public List<String> weaveErrors = new ArrayList<String>();
		
	public WeaveableTranslation getQuestion(){
		return question;
	}
	
	public void resetToOriginal(){
		question.resetToOriginal();
		for (WeaveableTranslation choice : choices){
			choice.resetToOriginal();
		}
	}
	
	public static String saveToXml(TranslationContext ctx) throws Exception {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    
    Element root = doc.createElement("math-tln");
    doc.appendChild(root);
    
    Element question = doc.createElement("question");
    question.setTextContent(ctx.getQuestion().getNewValue());    
    root.appendChild(question);

    Element choices = doc.createElement("choices");
    root.appendChild(choices);
    for (WeaveableTranslation item : ctx.choices){
      Element choice = doc.createElement("choice");
      choices.appendChild(choice);
      choice.setTextContent(item.getNewValue());
    }
    
    return XmlUtil.xml2string(doc);
	}

	public static void updateFromXml(TranslationContext ctx, String xml) throws Exception {
		Document doc = XmlUtil.parse(new InputSource(new StringReader(xml)));
		{
      NodeList questions = doc.getElementsByTagName("question");
      if (1 != questions.getLength()){
      	throw new RuntimeException("Expected one element 'question'.");
      }
  
    	Node q = questions.item(0);
    	ctx.getQuestion().setNewValue(q.getTextContent());
		}
		
		{
      NodeList choices = doc.getElementsByTagName("choices");
      if (1 != choices.getLength()){
      	throw new RuntimeException("Expected one element 'choices'.");
      }

      NodeList items = choices.item(0).getChildNodes();
      int sz = items.getLength();
      
      int idx = 0;
      for (int i=0; i < sz; i++){
      	Node node = items.item(i);
      	if ("choice".equals(node.getNodeName())){
        	ctx.choices.get(idx).setNewValue(node.getTextContent());
      		
      		idx++;
      		if (idx > ctx.choices.size()){
      			throw new RuntimeException("Too many elements.");
      		}
      	}
      }
      
      if (ctx.choices.size() != idx){
      	throw new RuntimeException("Mismatch in the number of elements.");
      }
		}	
	}
	
}
