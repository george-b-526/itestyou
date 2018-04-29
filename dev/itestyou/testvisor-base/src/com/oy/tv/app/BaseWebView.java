package com.oy.tv.app;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.oy.shared.hmvc.impl.BaseView;
import com.oy.tv.db.AnyDB;
import com.oy.tv.tmpl.TemplateEngine;

public abstract class BaseWebView extends BaseView {

	protected BaseViewCtx ctx;
	private BaseWebView parent;
	private boolean visible;
	
	public BaseWebView(BaseViewCtx ctx){
		this.ctx = ctx;
	}
	
	public void renderTemplate(String name, Writer out, Map<String, Object> bindings) throws IOException {
		TemplateEngine.renderTemplate(this, name, out, bindings);
	}
	
	public void pushView(BaseWebView view){
		boolean found = false;
		for (int i=0; i < countViews(); i++){
			if (view == getView(i)){
				found = true;
				break;
			}
		}
		 
		if (found){
			focusChild(view);
		} else { 
			super.installView(view);
			view.parent = this;
			focusChild(view);
		}
	}
	
	public AnyDB getDb(){
		return ctx.getDb();
	}  
	 
	public void setVisible(boolean value){
		visible = value;
	}  
	  
	public boolean getVisible(){
		return visible;
	}
	  
	public BaseWebView getFirstVisibleView(){
		BaseWebView first = null;
		  
		if (countViews() != 0){ 
			for (int i=0; i < countViews(); i++){
				if (getView(i) instanceof BaseWebView){
					BaseWebView curr = (BaseWebView) getView(i); 
					if (first == null){
						first = curr;
					}
					if (curr.getVisible()){
						first = curr;
						break; 
					}      
				}
			} 
		} 
		 
		return first;
	}
	
	public void focus(){
		if (parent != null){
			parent.focusChild(this);
		}
	}  
	    
	public void focusChild(BaseWebView child){
		boolean found = false;
		for (int i=0; i < countViews(); i++){
			if (child == getView(i)){
				found = true;
				break;
			}
		}
		
		if (!found){
			throw new RuntimeException("Bad view");
		}
		
		for (int i=0; i < countViews(); i++){
			((BaseWebView) getView(i)).setVisible(false);
		}
		child.setVisible(true);
		 
		if (parent != null){ 
			parent.focusChild(this);
		}
	}
}
