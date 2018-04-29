package com.oy.tv.app.view;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.IView;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.app.IExitStrategy;
import com.oy.tv.app.ViewCtx;
import com.oy.tv.app.WebView;

public class RootView extends WebView {
       
	//
	// Start persistent activity based on existing bundle and actor
	// http://localhost/test/bin/view?action_id=0&inExitUrl=http://www.cnn.com&inBundleId=1&inOwnerId=1
	//   
	BaseAction m_Init = new BaseAction (){
		int inBundleId;
		int inOwnerId;
		String inExitUrl;
		public void execute(IPropertyProvider provider){
			try {
				getModel().init(
					inBundleId, inOwnerId, 
					new IExitStrategy.UrlExitStrategy(getCtx(), inExitUrl)
				);
			} catch (Exception e){
				throw new RuntimeException(e);
			}			
			
			getModel().getOptions().showIntro = true;
			getModel().getOptions().showStepResult = false;
			getModel().getOptions().showSummary = true;
			
			begin(provider);
		}  
		  
		public int hashCode(){
			return 0;
		}
	};
	
	//
	// Starts transient anonymous activity on unit   
	// http://localhost/test/bin/view?action_id=1&inUnitId=1
	// 
	BaseAction m_InitEx = new BaseAction (){
		public int inUnitId;
		
		private IExitStrategy getExitStratey(){
			class ReEnterStrategy implements IExitStrategy{
				private int unitId;
				
				public ReEnterStrategy(int unitId){
					this.unitId = unitId;
				}
				
				public void exit(){
					inUnitId = unitId;
					execute(null);
				}  
			};
			
			return new ReEnterStrategy(inUnitId);
		}
 		
		public void execute(IPropertyProvider provider){
			try {    
				getModel().init(inUnitId, getExitStratey());
			} catch (Exception e){      
				throw new RuntimeException(e);
			}
			      
			getModel().getOptions().showIntro = false;
			getModel().getOptions().showStepResult = true;
			getModel().getOptions().showSummary = false;
		
			begin(provider);
		}  
		  
		public int hashCode(){
			return 1;
		}
	};
	
	EnterView eview;
	StepView sview;
	LeaveView lview;
	
	public RootView(ViewCtx ctx){
		super(ctx);
		
		eview = new EnterView(this);
		sview = new StepView(this);
		lview = new LeaveView(this);
	} 
	  
	private void begin(IPropertyProvider provider){
		uninstallAllViews();
		pushView(eview);
		if (!getModel().getOptions().showIntro){
			eview.m_Next.execute(provider);
		}    
	}
	      
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		IView top = getFirstVisibleView();
		if (top != null){
			top.render(dispatcher, out);	
		}
	}  
  	
}
