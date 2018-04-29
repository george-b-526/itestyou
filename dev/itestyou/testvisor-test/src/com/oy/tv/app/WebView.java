package com.oy.tv.app;

import com.oy.tv.app.model.ViewModel;

public abstract class WebView extends BaseWebView {

	public WebView(ViewCtx ctx){
		super(ctx);
	}
  	  	
	public ViewCtx getCtx(){  
		return (ViewCtx) ctx;
	}
	
	public ViewModel getModel(){
		return ((ViewCtx) ctx).getModel();
	}
		
}
