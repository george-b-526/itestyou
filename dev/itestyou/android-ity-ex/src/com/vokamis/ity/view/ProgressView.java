package com.vokamis.ity.view;

import android.webkit.WebView;

import com.vokamis.ity.cmd.ShowProgressCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.state.ActorAccount;
import com.vokamis.ity.state.StateManager;
import com.vokamis.ity.util.AbstractWebView;

public class ProgressView extends AbstractWebView {

	private ShowProgressCmd cmd;
	
	public ProgressView(IViewManager parent, WebView root){
		super(parent, root);
	}
	
	@Override
	public String getTitle(){
		return "My Progress";
	}
	
	public void dispatch(ShowProgressCmd cmd){
		this.cmd = cmd;
		dispatch();
	}
	
	@Override
	public String getStartUrl(){

		class JavaScript {
	    public void showUnit(String unitId) {
	    	int _unitId = Integer.parseInt(unitId);
	    	
	    	parent.dispatch(new StartTestCmd(
	    			0, _unitId, parent.getStateManager().getSettings().getAccountIdx()));
	    }
		}
		
		root.addJavascriptInterface(new JavaScript(), "ity_ctx");
		
		ActorAccount account = parent.getStateManager().getSettings().
			getAccount(cmd.getAccountIdx());

		String token = null;
		if (account != null){
			token = account.getAuthToken();
		}
		
		return StateManager.makeProgressUrl(parent, token);
	}
	
}
