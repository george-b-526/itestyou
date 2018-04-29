package com.vokamis.ity.util;

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
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.EntityPolicy;

public abstract class AbstractWebView implements IViewContainer {
	
	protected final IViewManager parent;
	protected final WebView root;
	private ProgressDialog wait;

	public AbstractWebView(IViewManager parent, WebView root){
		this.parent = parent;
		this.root = root;
  
		root.clearCache(true);
	}
	
	public View getContentView(){
		return root;
	}

	public abstract String getStartUrl();

	public void onStarted(String url) {}

	public void onFinished(String url) {}
	
	protected void dispatch(){
		
		final AbstractWebView _this = this;
		
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
				parent.updateStatus(_this, parent.getResourceString(R.string.loading));
				_this.onStarted(url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				waitEnd();
				parent.updateStatus(_this, "");
				_this.onFinished(url);
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
		
		String url = getStartUrl();
		if (url != null){
			waitEnd();
			waitBegin();
			root.stopLoading();  			
			root.clearView();
			root.loadUrl(url);			
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
