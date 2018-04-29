package com.oy.tv.app.view;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.app.WebView;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.parts.WebParts;

public class EnterView extends WebView {
	
	BaseAction m_Next = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.uninstallAllViews();
			parent.pushView(parent.sview);
			
			getCtx().sendRedirect(303, ResourceLocator.getTestHref());  
		}
	};
	 
	private RootView parent;  
	
	public EnterView(RootView parent){
		super(parent.getCtx());
  
		this.parent = parent;
	} 
	         
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		Map<String, Object> bind = new HashMap<String, Object>();
		  
		bind.put("out", out);    
		bind.put("m_Next", m_Next);    
		bind.put("model", getModel());
		bind.put("WebParts", WebParts.class);
		bind.put("ResourceLocator", ResourceLocator.class);
		
		renderTemplate("Default", out, bind);
	}

}
