package com.oy.tv.app.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.app.WebView;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.parts.WebParts;

public class LeaveView extends WebView {
	
	BaseAction m_Next = new BaseAction (){
		public void execute(IPropertyProvider provider){  
			getModel().getExitStrategy().exit();
  		}  
	};   
    	
	public LeaveView(RootView parent){
		super(parent.getCtx());
	} 
	         
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {		
		out.write("<html><head>");		  
		out.write("<title>Testvisor Tester - You have completed the test!</title>");  
		out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />");
		out.write("<link type='text/css' href='" + ResourceLocator.getCSSHref() + "' rel='stylesheet' />");
		
		out.write("<style>\n");
		out.write("label {text-align: left; display: block; width: 150px; margin-top: 12px; }\n");
		out.write("input {width: 150px;}\n");
		out.write("</style>\n");  
		    
		out.write("</head><body>");
		
		out.write("<h3 align='center'>Welcome to the Testvisor Tester!</h3>");
		out.write("<hr />");  
		   
		WebParts.beginPanel(out, "100%");
		
		out.write("<tr style='height: 200px;'>");    
		out.write("<td align='center'>");
		
		out.write("<div align='center' style='width: 400px; text-align: center;'>");	
		
				
		out.write("<p>Congratulations! You have completed the test!</p>");
		out.write("You have answered <b>" + getModel().getTest().getCorrect() + "</b> questions correctly out of <b>" + getModel().getTest().getCompleted() + "</b> questions total.</p>");  	  
		out.write("</div>");
		    
		out.write("</td>");
		out.write("</tr>");
		    
		WebParts.endPanel(out);
		  
		out.write("<hr />");
		    
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Next.hashCode() + "'>");
		out.write("<p align='center'>");
		out.write("<input style='width: 64px;' type='submit' value='continue' />");  
		out.write("</p>");
		out.write("</form>");
		 
		out.write("</body></html>");
	}

}
