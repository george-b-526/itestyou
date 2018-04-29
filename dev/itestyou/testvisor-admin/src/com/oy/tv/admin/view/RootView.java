package com.oy.tv.admin.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.tv.admin.ViewCtx;
import com.oy.tv.admin.WebView;
import com.oy.tv.parts.WebParts;

public class RootView extends WebView {
	
	LoginForm login;
	HomePageView home;
	
	public RootView(ViewCtx ctx, IPropertyProvider pp){
		super(ctx);
		
		home = new HomePageView(this, pp);

		login = new LoginForm(this);
		login.afterLogin = home;
		login.afterLogout = login;
		  
		installView(login);  
		
		pushView(login);
	} 
	  
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<html><head>");		
		out.write("<title>Testvisor Admin</title>");
		out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />");
		out.write("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"http://www.itestyou.com/css/test.normal.css\" />");
		
		out.write("<style>\n");
		out.write("label {text-align: left; display: block; width: 150px; margin-top: 12px; }\n");
		out.write("</style>\n");  
		
		out.write("</head><body>");		  
		
		WebParts.beginPanel(out, "100%", 2);
		
		out.write("</td></tr>"); 
		try {
			if (getCtx().getLastError() != null){
				out.write("<tr style='background-color: #FFA0A0;'><td align='center'>");
				out.write(getCtx().formatLastError());
				out.write("</td></tr>");				
			}
			   
			if (getCtx().hasMessages()){
				out.write("<tr style='background-color: #C0FFC0;'><td align='center'>");
				out.write(getCtx().formatMessages());
				out.write("</td></tr>");	 			
			}
			
			out.write("<tr height='200px' align='center'><td valign='top'>");

			getFirstVisibleView().render(dispatcher, out);
 			
			out.write("</td></tr>");
		} catch(Exception e){
 			out.write(getCtx().formatException(e));
		}				 
		
		WebParts.endPanel(out);
		
		out.write("</body></html>");
	}
	
} 
 