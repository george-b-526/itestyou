package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.ViewCtx;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.admin.view.IAction;
import com.oy.tv.model.unit.TranslationWeaver;
import com.oy.tv.model.unit.WeaveableTranslation;
import com.oy.tv.model.unit.RenderContext;
import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.TranslationContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.util.XmlUtil;

public class UnitPreView extends WebView {

	BaseAction m_Tag = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.pushAction(
				new IAction(){
					public void act(){
						parent.pushView(UnitPreView.this);
					}        
				}
			);  
   			  
			parent.tag.setFocus(unit);
			parent.pushView(parent.tag);
		}   
	};
	
	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.editunit.setFocus(unit);
			parent.pushView(parent.editunit);
		}  
	};  
	
	private HomePageView parent;
	private UnitBO unit;
	private String lang;
	
	public UnitPreView(HomePageView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	
	public void setFocus(UnitBO unit, String lang){
		this.unit = unit;
		this.lang = lang;
	}
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>Preview Unit " + unit.getId() + "</p><hr />");
		  
		out.write("<div align='left' style='width: 400px; text-align: left;'>");
		renderUnit(getCtx(), unit, null, out, parent.pp, true, lang);
		out.write("</div>");
		      
		out.write("<hr />");
		String act;
		
		act = WebParts.button(dispatcher, m_Tag, "Tag");
		out.write(act);    
		
		act = WebParts.button(dispatcher, m_Done, "Done");
		out.write(act);
	}
	
	private static void renderWarningError(List<String> items, Writer out) throws IOException {
		if (items != null && items.size() != 0){
			out.write("<ul>");
			for (int i=0; i < items.size(); i++){
				out.write("<li><font style='color: red;'>" + HtmlUtil.escapeHTML(items.get(i)) + "</font></li>");
			}
			out.write("</ul>");
		}
	}
	
	public static void renderUnit(ViewCtx ctx, UnitBO unit, String values, Writer out, 
			IPropertyProvider pp, String lang) {
		renderUnit(ctx, unit, values, out, pp, false, lang);
	}
	
	public static void renderUnit(ViewCtx ctx, UnitBO unit, String values, Writer out, 
			IPropertyProvider pp, boolean showMeta, String lang) {
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {
			StringWriter sw = new StringWriter();
			UnitProcessor up = new UnitProcessor();
		  	
			up._doc = XmlUtil.loadXmlFrom(unit.getXml());
			up._pp = pp;
			up._ctx = new UnitContext();
			up._rtx = new RenderContext();
			up._rtx.showAnswer = true;
			up._ctx.values = values;
			up._log = System.out;
			up._eval = eval;
			
			up.setLocale(lang);
 			up.render(sw);

 			out.write("<div style='padding: 8px; margin: 8px; border: solid 1px #A0A0A0;'>");
			out.write(sw.getBuffer().toString());  
			out.write("</div>");
			      
			renderWarningError(up._ctx.warnings, out);
			renderWarningError(up._ctx.errors, out);

			TranslationContext uc = TranslationWeaver.extractTranslatable(unit);
			renderWarningError(uc.parseErrors, out);

			if (showMeta){
   			out.write("<div style='padding: 8px; margin: 8px; border: solid 1px #A0A0A0;'>");
  			out.write("<b>" + HtmlUtil.escapeHTML(uc.getQuestion().getOriginalValue()) + "</b>");
  			out.write("<ul>");
  			for (WeaveableTranslation choice : uc.choices){
  				out.write("<li>" + HtmlUtil.escapeHTML(choice.getOriginalValue()) + "</li>");
  			}
  			out.write("</ul>");
			out.write("</div>");
			}
			
			YacasEvaluatorEx.leaseComplete(eval);
		} catch (Exception e){
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException("Failed to render unit.", e);
		}
	}
	
}
