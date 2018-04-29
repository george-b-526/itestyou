package com.oy.tv.admin.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.admin.WebView;
import com.oy.tv.parts.WebParts;

public class LoginForm extends WebView {
	
	BaseAction m_Login = new BaseAction (){
		String inName;
		String inPwd;
		public void execute(IPropertyProvider provider){
			try { 
				getModel().login(inName, inPwd);
			} catch (Exception e){
				throw new RuntimeException(e); 
			}			 
			parent.pushView(afterLogin);
		}  
	};
	
	BaseAction m_Logout = new BaseAction (){
		public void execute(IPropertyProvider provider){
			getModel().reset();
			getModel().logout();
			 
			parent.pushView(afterLogout);
		}    
	};
	
	private WebView parent;
	
	WebView afterLogin;
	WebView afterLogout;
	
	public LoginForm(WebView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>Please login</p>");
		
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Login.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
	 			         
		out.write("<tr>");    
		out.write("<td align='center'>");
     		            
		WebParts.beginPanel(out, "50%");
		{  
			out.write("<tr><td width='1%' nowrap align='right'>Name</td>");
			out.write("<td><input style='width: 100%; id='file' name='inName'></td></tr>");
	 		
			out.write("<tr><td width='1%' nowrap align='right'>Password</td>");
			out.write("<td><input type='password' style='width: 100%; id='kind' name='inPwd'></td></tr>");
			
			out.write("<tr><td colspan='2' align='center'><br /><input type='submit' value='login'></td></tr>");
		} 
		WebParts.endPanel(out);
		     
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);

		out.write("</form>");
	
	}
			
} 
 