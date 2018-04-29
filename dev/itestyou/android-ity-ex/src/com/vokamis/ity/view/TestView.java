package com.vokamis.ity.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.vokamis.ity.R;
import com.vokamis.ity.cmd.ShowHomeViewCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.state.ActorAccount;
import com.vokamis.ity.state.StateManager;

public class TestView implements IViewContainer {
	
	private final IViewManager parent;
	private final WebView root;
	private ProgressDialog wait;
	
	public TestView(IViewManager parent, WebView root){
		this.parent = parent;
		this.root = root;
  
		root.clearCache(true);
	}
	
	public String getTitle(){
		return "Testing";  
	}
	
	public View getContentView(){
		return root;
	}
	
	public void dispatch(final StartTestCmd cmd){
		
		class CustomWebClient extends WebViewClient {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf(EntityPolicy.HOME_DOMAIN) != -1){
					return false;
				} else {
					// force foreign links like ads to run in the normal browser window
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					root.getContext().startActivity(intent);
					return true;
				}
			}
			
			@Override
	    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return true;
	    }
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
	  		waitBegin();
				parent.updateStatus(TestView.this, parent.getResourceString(R.string.loading));
				parent.getStateManager().checkpointProgress(
						cmd.getAccountIdx(), cmd.getGrade(), url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				waitEnd();
				parent.updateStatus(TestView.this, "");
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
			    String description, String failingUrl) {
				waitEnd();
				parent.toast(parent.getResourceString(R.string.no_server) + " " + description);
				parent.dispatch(new ShowHomeViewCmd());
			}
		}  

		{
  		root.setWebViewClient(new CustomWebClient());
  
  		root.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
  		root.getSettings().setPluginsEnabled(false);
  		root.getSettings().setSupportMultipleWindows(false);
  		root.setVerticalScrollBarEnabled(false);
  		root.setHorizontalScrollBarEnabled(false);
  
  		root.getSettings().setJavaScriptEnabled(true);
  		root.getSettings().setSupportZoom(true);
  		root.setKeepScreenOn(true);
		}
		
		{
  		ActorAccount account = parent.getStateManager().getSettings().
  				getAccount(cmd.getAccountIdx());

  		waitEnd();
  		waitBegin();
			root.stopLoading();  			
			root.clearView();
			root.loadUrl(StateManager.makeTestUrl(parent,
					cmd.getGrade(), cmd.getUnitId(), account.getAuthToken()));
		}
		
	}

	private void waitBegin(){
		if (wait == null){
			wait = ProgressDialog.show(root.getContext(), "", 
					parent.getResourceString(R.string.loading), true, true);
		}
	}

	private void waitEnd(){
		if (wait != null){
			wait.dismiss();
			wait = null;
		}
	}
}
