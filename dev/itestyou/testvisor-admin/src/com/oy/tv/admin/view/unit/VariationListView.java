package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.VariationBO;

public class VariationListView extends WebView {
	
	BaseAction m_Page = new BaseAction (){
		int inPage;
		public void execute(IPropertyProvider provider){
			page = inPage;
		}
	};
	    
	BaseAction m_Add = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				VariationDAO.addVariation(getCtx().getDb(), unit, "");
				page = Integer.MAX_VALUE;
				getCtx().pushMessage("Added.");
			} catch (Exception e){   
				throw new RuntimeException("Failed to load object.");
			}  
		}
	};
	
	BaseAction m_Delete  = new BaseAction (){
		String inId;
		public void execute(IPropertyProvider provider){
			try {
				VariationBO var = VariationDAO.loadVariation(getCtx().getDb(), inId);
				VariationDAO.deleteVariation(getCtx().getDb(), var);
				getCtx().pushMessage("Deleted.");
			} catch (Exception e){ 
				throw new RuntimeException("Failed to load object.");
			}  
		}
	};
	
	BaseAction m_Save = new BaseAction (){
		String [] inIds;
		String [] inValues;
		public void execute(IPropertyProvider provider){
			try {
				for (int i=0; i < inIds.length; i++){
					VariationBO var = VariationDAO.loadVariation(getCtx().getDb(), inIds[i]);
					VariationDAO.updateVariation(getCtx().getDb(), var, inValues[i], var.getState());
				}
				getCtx().pushMessage("Updated.");
			} catch (Exception e){ 
				throw new RuntimeException("Failed to load object.");
			} 
		}    
	};
  	
	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.pushView(parent.editunit);
		} 
	};
	
	public final int PER_PAGE = 10;
	
	private int page = 0;
	private UnitBO unit;
	private HomePageView parent;
	
	public VariationListView(HomePageView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	
	public void setFocus(UnitBO unit){
		this.unit = unit;
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>List Variations for Unit " + unit.getId() + "</p><hr />");
      		    
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Save.hashCode() + "'>");
		
		PagedKeyList pkl;
		try {
			pkl = VariationDAO.getAllPaged(getCtx().getDb(), unit, PER_PAGE, page);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		page = pkl.page;
  		  
		out.write("<div>");  
		out.write("Total " + pkl.count+ " items, go to page: ");	
		for (int i=0; i < pkl.pages; i++){
			if (page == i){
				out.write(" <b>" + (i + 1) + "</b> ");	
			} else {
				String act = WebParts.button(dispatcher, m_Page, "" + (i + 1), new String [] {"inPage"}, new String [] {"" + i}, true);
				out.write(act);
				out.write(" ");
			}
		}
		out.write("</div><hr />");
		
		WebParts.beginPanel(out, "100%");
         
		out.write("<tr>");    
		out.write("<td align='center'>");
		out.write("<div align='left' style='width: 400px; text-align: left;'>");
		        
		// fetch & render
		for (int i=0; i < pkl.ids.size(); i++){				
			int id = pkl.ids.get(i);
			try {  
				out.write("<hr />");    
  
				VariationBO var = VariationDAO.loadVariation(getCtx().getDb(), id);
				  
				out.write("<input type='hidden' name='inIds' value='" + var.getId() + "'>");
				out.write("Id = " + var.getId());  
				out.write(" | Values = <input name='inValues' value='" + 
						HtmlUtil.escapeHTML(var.getValues()) + "' />");
				
				String act = WebParts.button(dispatcher, m_Delete, 
						"Delete", new String [] {"inId"}, new String [] {"" + var.getId()}, true);
				out.write("<span style='background-color: red; padding: 8px;'>" + act + "</span>");
 				
				out.write("<br />");   
  				
				UnitPreView.renderUnit(getCtx(), unit, var.getValues(), out, parent.pp, 
						TranslationDAO.LANG_EN);
			} catch (Exception e){
				out.write("Failed to render unit id " + id);
			}  
		}  
	
		out.write("</div>");
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);

		out.write("<hr />");
		out.write("<input type='submit' value='save' />");

		String act;
		 
		act = WebParts.button(dispatcher, m_Add, "Add Variation");
		out.write(act);
		
		act = WebParts.button(dispatcher, m_Done, "Done");
		out.write(act);
  
		out.write("</form>");
	}
	
}
