package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.dao.core.TagsDAO;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.UnitBO;

public class UnitTagView extends WebView {

	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.popView();
		}  
	};
	
	BaseAction m_Save = new BaseAction (){
		int [] inTagId;
		public void execute(IPropertyProvider provider){
			TagsDAO.tagUnit(getDb(), unit, inTagId);
		}  
	};
  	
	private HomePageView parent;
	private UnitBO unit;
	
	public UnitTagView(HomePageView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	
	public void setFocus(UnitBO unit){
		this.unit = unit;
	}
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>Tag Unit " + unit.getId() + "</p><hr />");
		  
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Save.hashCode() + "'>");		
		      
		out.write("<div align='left' style='text-align: left;'>");
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		try {	
			List<TagsDAO.TagMap> all = TagsDAO.getAllTagsFor(getDb(), unit);
			out.write("Total tags: " + all.size());
			out.write("<table border='1' style='border-collapse: collapse;' bordercolor='#808080'>");
			out.write("<tr>");  
			for (int i=0; i < all.size(); i++){
				if (i != 0 && i % 5 == 0){
					out.write("</tr>");
					out.write("<tr>");
				}
				  
				String style = "";
				String checked = "";
				if (all.get(i).getTagged()){
					checked = " checked";
					style += " style='background-color: #F0FFF0;'";
				}   
				           
				out.write("<td valign='top'" + style + ">");
				out.write("<input style='width: 12px;' type='checkbox'" + checked + 
						" name='inTagId' value='" + all.get(i).getTag().getId() + "'>");
				out.write("<b>" + HtmlUtil.escapeHTML(all.get(i).getTag().getName()) + "</b>");
				if (all.get(i).getTag().getBody() != null){
					out.write("<p style='padding: 2px; margin: 2px; font-size: 10px;'>" + 
							all.get(i).getTag().getBody() + "</p>");
				}  
				out.write("</td>");
			}  
			out.write("<tr>");
			out.write("</table>");
     			
			out.write("<hr />");
			String act = WebParts.button(dispatcher, m_Done, "Done");
			out.write("<input type='submit' value='save' />" + act);
			
			
			YacasEvaluatorEx.leaseComplete(eval);
		} catch (Exception e){
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException("Failed to render unit.", e);
		}
		out.write("</div>");
		
		out.write("</form>");
	}
	
}
