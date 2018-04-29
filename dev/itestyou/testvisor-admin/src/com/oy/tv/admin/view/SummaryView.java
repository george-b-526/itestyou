package com.oy.tv.admin.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.tv.admin.WebView;
import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.parts.WebParts;

public class SummaryView extends WebView {

	private HomePageView parent;
	
	public SummaryView(HomePageView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write(
				"<p align='center'>Summary View (" + 
				getModel().getPrincipal().getName() + ":" + getModel().getPrincipal().getId() + ":" + 
				getModel().getPrincipal().getProperties() + 
				")</p><hr />");

		String act;
		if (getModel().isAuth()){
			WebParts.beginPanel(out, "100%");
	         
			out.write("<tr>");    
			out.write("<td align='center'>");
	     		          
			WebParts.beginPanel(out, "50%");
			{  
				out.write("<tr><td align='left'>");
			     
				out.write("<br />");
				act = dispatcher.encodeActionDispatch(parent.m_ListUnits, "Browse Units");
				out.write(act);
				
				if (UserDAO.isEditor(getModel().getPrincipal())){
  				out.write("<br />");
  				act = dispatcher.encodeActionDispatch(parent.m_AddUnit, "Add Unit");
  				out.write(act);
				}     	
				
				if (UserDAO.isAdmin(getModel().getPrincipal())){
					out.write("<br />");
					out.write("<a target='_blank' href='http://www.itestyou.com/cms/invoke/ity_import_units'>Sync units from database to WordPress frontend</a>");					
				}
				
				out.write("<br /><br />");
				act = dispatcher.encodeActionDispatch(parent.parent.login.m_Logout, "Logout");
				out.write(act);
				  			    
				out.write("</td></tr>");
			}
			WebParts.endPanel(out);
		       
			out.write("</td>");
			out.write("</tr>");
			 
			WebParts.endPanel(out);
		}     
	}
	
}
