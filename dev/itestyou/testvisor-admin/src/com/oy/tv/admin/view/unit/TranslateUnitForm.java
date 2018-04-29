package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.model.unit.TranslationContext;
import com.oy.tv.model.unit.TranslationWeaver;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;

public class TranslateUnitForm extends WebView {

	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.popView();
		}
	};

	BaseAction m_Reset = new BaseAction (){
		public void execute(IPropertyProvider provider){
			ctx.resetToOriginal();
			newUnit = TranslationWeaver.weaveTranslatable(unit, ctx);
			getCtx().pushMessage("Reset OK.");
		}
	};

	BaseAction m_Activate = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				tln = TranslationDAO.updateState(getCtx().getDb(), 
						lang, "/unit/math/" + unit.getId(), EObjectState.ACTIVE);
				getCtx().pushMessage("Activated OK.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to activate translation.", e);
			}			
		}
	};

	BaseAction m_Deactivate = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				tln = TranslationDAO.updateState(getCtx().getDb(), 
						lang, "/unit/math/" + unit.getId(), EObjectState.INACTIVE);
				getCtx().pushMessage("Deactivated OK.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to deactivate translation.", e);
			}			
		}
	};

	BaseAction m_Delete = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
  			TranslationDAO.delete(getCtx().getDb(), lang, unit);
  			getCtx().pushMessage("Deleted.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to save translation.", e);
			}
		}
	};
	
	BaseAction m_Save = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				if (tln != null && !tln.getState().equals(EObjectState.INACTIVE)){
					throw new RuntimeException("Must be INACTIVE to save.");
				}
				
				ctx.getQuestion().setNewValue(provider.getPropertyValueRaw("inQuestion"));
				for (int i=0; i < ctx.choices.size(); i++){
					ctx.choices.get(i).setNewValue(provider.getPropertyValueRaw("inChoice" + i));
				}
				newUnit = TranslationWeaver.weaveTranslatable(unit, ctx);
				
				String xml = TranslationContext.saveToXml(ctx);
				tln = TranslationDAO.put(getCtx().getDb(), lang, "/unit/math/" + unit.getId(), xml);
				
				getCtx().pushMessage("Saved.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to save translation.", e);
			}			
		}      
	};
	
	private HomePageView parent;
	private UnitBO unit;
	private UnitBO newUnit;
	private String lang;
	private TranslationContext ctx;
	private TranslationBO tln;
	
	public TranslateUnitForm(HomePageView parent){
		super(parent.getCtx());
		  
		this.parent = parent;
	} 
	  
	public void setFocus(UnitBO unit, String lang){
		this.unit = unit;
		this.newUnit = this.unit;
		this.lang = lang;
		
		ctx = TranslationWeaver.extractTranslatable(unit);
		ctx.resetToOriginal();
		
		try {
  		tln = TranslationDAO.get(getCtx().getDb(), lang, "/unit/math/" + unit.getId());
  		if (tln != null){
  			TranslationContext.updateFromXml(ctx, tln.getData());  			
  			newUnit = TranslationWeaver.weaveTranslatable(unit, ctx);
  		}
		} catch (Exception e){
			getCtx().pushMessage("Failed to load translation. " + e.getMessage());
		}
	}
	   
	private void renderOriginal(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<b>Original (EN)</b>");
		out.write("<div style='width: 450px;'>");
		UnitPreView.renderUnit(getCtx(), unit, null, out, parent.pp, false, 
				TranslationDAO.LANG_EN);
		renderWarningError(ctx.parseErrors, out);
		out.write("</div>");
	}

	private void renderTranslation(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<b>Translation (" + lang.toUpperCase() + ")</b>");
		
		if (tln != null && tln.getState().equals(EObjectState.ACTIVE)){
			out.write(" <span style='background-color: #00FF00;'><b>&nbsp;ACTIVE&nbsp;</b></span>");
		}
		
		out.write("<div style='width: 450px;'>");
		UnitPreView.renderUnit(getCtx(), newUnit, null, out, parent.pp, false, lang);
		renderWarningError(ctx.weaveErrors, out);
		out.write("</div>");
	}
	
	private void renderForm(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Save.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
		{  								
			out.write("<tr>");
			out.write("<td nowrap align='right' valign='top'>Question</td>");
			out.write("<td><textarea style='background-color: #F0F0F0;' rows='5' cols='60' readonly>" + HtmlUtil.escapeHTML(ctx.getQuestion().getOriginalValue()) + "</textarea></td>");
			out.write("<td><textarea rows='5' cols='60' name='inQuestion'>" + 
					HtmlUtil.escapeHTML(ctx.getQuestion().getNewValue()) + "</textarea></td>");
			out.write("</tr>");

			for (int i=0; i < ctx.choices.size(); i++){
				out.write("<tr>");
				out.write("<td nowrap align='right' valign='top'>Choice " + i + "</td>");
				out.write("<td><textarea style='background-color: #F0F0F0;' rows='1' cols='60' readonly >" + HtmlUtil.escapeHTML(ctx.choices.get(i).getOriginalValue()) + "</textarea></td>");
				out.write("<td><textarea rows='1' cols='60' name='inChoice" + i + "'>" + 
						HtmlUtil.escapeHTML(ctx.choices.get(i).getNewValue()) + "</textarea></td>");
				out.write("</tr>");				
			}
		
		}  
		WebParts.endPanel(out);
		     
		out.write("<hr /><span style='font: 8px;'><small>");
		out.write(HtmlUtil.escapeHTML("<#0/>, <#1/>, ... - <draw>, <latex>, <img> tags; do NOT modify;") + "<br />");
		out.write(HtmlUtil.escapeHTML("<$0/>, <$1/>, ... - ${a} and other inline math expressions; do NOT modify;"));
		out.write("</small></span>");
		
		String act2 = WebParts.button(dispatcher, m_Reset, "Reset");

		String act3 = null;
		if (tln != null) {
  		if (tln.getState().equals(EObjectState.INACTIVE)){  
  			act3 = WebParts.button(dispatcher, m_Activate, "Activate");
  		}  
  		if (tln.getState().equals(EObjectState.ACTIVE)){  
  			act3 = WebParts.button(dispatcher, m_Deactivate, "Deactivate");
  		}
		}

		String act4 = WebParts.button(dispatcher, m_Delete, "Delete");

		
		out.write("<hr /><p>");
		out.write("<span style='float: left; background-color: red; padding: 8px;'>" + act2 + "</span>");
		
		if (act3 != null && UserDAO.isAdmin(getModel().getPrincipal())){
			out.write(
					"<span style='float: left; background-color: red; padding: 8px;'>" + act4 + "</span>");
			out.write(
					"<span style='float: left; background-color: red; padding: 8px;'>" + act3 + "</span>");		
		}
	
		out.write("<input type='submit' value='Save' />");
		out.write(WebParts.button(dispatcher, m_Done, "Done"));
		out.write("</p>");
		
		out.write("</form>");		
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
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>Translate Unit: Id=" + unit.getId() + ", OwnerId=" + unit.getOwnerId() +"</p><hr />");
	  
		out.write("<table>");
		
		out.write("<tr>");
		out.write("<td valign='top' align='center'>");
		renderOriginal(dispatcher, out);
		out.write("</td>");
		out.write("<td valign='top' align='center'>");
		renderTranslation(dispatcher, out);
		out.write("<td>");
		out.write("</tr>");

		out.write("<tr>");
		out.write("<td colspan='2' valign='top' align='center'>");
		renderForm(dispatcher, out);
		out.write("</td>");
		out.write("</tr>");
		
		out.write("</table>");
	}

}
