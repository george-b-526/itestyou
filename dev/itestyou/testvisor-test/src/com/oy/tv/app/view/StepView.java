package com.oy.tv.app.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.WebView;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.parts.WebParts;

public class StepView extends WebView {
	
	BaseAction m_Vote = new BaseAction (){
		int inCurrIndex;
		public void execute(IPropertyProvider provider){
			if (inCurrIndex == getModel().getCurrentStepIndex()){
				
				int index = -1;				 
				for (int i =0; i < UnitProcessor.LETTERS.length; i++){
					String vote = getCtx().getParameter("vote" + i);
					if (vote != null){
						index = i;
						break;
					}
				}
				                       
				if (index == -1){     
					getCtx().pushMessage("Unable to understand your response.");
				} else {
					boolean more = getModel().voteAndNext(index);    
					if (more){  
						getCtx().sendRedirect(303, ResourceLocator.getTestHref());
					} else {
						parent.uninstallAllViews();
						parent.pushView(parent.lview);  
						if (!getModel().getOptions().showSummary){
							parent.lview.m_Next.execute(provider);
						}
					}  
				}  
			} else {    
				getCtx().pushMessage("Please don't use the browser back button. ");
			}
		}
	};
  	
	private RootView parent;
	
	public StepView(RootView parent){
		super(parent.getCtx());
		  
		this.parent = parent;
	} 
	 
	public static void renderNoCacheHeaders(BaseViewCtx ctx){
		ctx.addHeader("Pragma", "no-cache");
		ctx.addDateHeader("Expires", 1);
		ctx.addHeader("Cache-Control", "no-cache");
		ctx.addHeader("Cache-Control", "no-store");
		ctx.addHeader("Cache-Control", "must-revalidate");		
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		if (UnitProcessor.LETTERS.length < getModel().getCurrentChoiceCount()){
			throw new RuntimeException("Too many options.");
		}
		
		renderNoCacheHeaders(getCtx());
		
		out.write("<html><head>");		  
		out.write("<title>Testvisor Tester - Test in Progress</title>");  
		out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />");
		out.write("<link type='text/css' href='" + ResourceLocator.getCSSHref() + "' rel='stylesheet' />");
		
		out.write("<style>\n");
		out.write("label {text-align: left; display: block; width: 150px; margin-top: 12px; }\n");
		out.write("input {width: 150px;}\n");
		out.write("</style>\n");  
		    
		out.write("</head><body>");		  		
		try { 			
			out.write("<h3 align='center'>Question");
			if (getModel().getStepsCount() != 1){
				out.write(" " + (getModel().getCurrentStepIndex() + 1) + " of " + getModel().getStepsCount());
			}  
			out.write("</h3>");
			out.write("<hr />");
			{
				WebParts.beginPanel(out, "100%");
		         
				if (getCtx().getLastError() != null){
					out.write("<tr style='background-color: #FFA0A0;'><td align='center'>");
					out.write("Error has occured on our server. We are investigating this case and will try to make a resolve it as soon as possible.");
					out.write("</td></tr>");  				  
				}
				   
				if (getCtx().hasMessages()){
					out.write("<tr style='background-color: #C0FFC0;'><td align='center'>");
					out.write(getCtx().formatMessages());
					out.write("</td></tr>");	 			
				}
				
				out.write("<tr>");    
				out.write("<td align='center'>");
				
				getModel().renderCurrent(getDb(), out);  
				    
				out.write("</td>");
				out.write("</tr>");
				   
				WebParts.endPanel(out);  
			}
			out.write("<hr />");
			{  
				out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
				out.write("<input type='hidden' name='inCurrIndex' value='" + getModel().getCurrentStepIndex() + "'>");
				out.write("<input type='hidden' name='action_id' value='" + m_Vote.hashCode() + "'>");
				{
					WebParts.beginPanel(out, "100%");
				 			         
					out.write("<tr>");    
					out.write("<td align='center'>");
											    
					for (int i=0; i < getModel().getCurrentChoiceCount(); i++){
						if (i != 0){
							out.write("&nbsp;");
						}
						out.write("<input style='width: 64px;' type='submit' name='vote" + i + "' value='" + UnitProcessor.LETTERS[i] + "' />");
					}
											    
					out.write("</td>");
					out.write("</tr>");
					   
					WebParts.endPanel(out);				
				}									
				out.write("</form>");
			}
		} catch(Exception e){
 			out.write(getCtx().formatException(e));
		}				 		
		out.write("</body></html>");
	}
	
} 
 