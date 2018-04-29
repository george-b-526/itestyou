package com.vokamis.ity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.vokamis.ity.cmd.AddExistingUserCmd;
import com.vokamis.ity.cmd.AuthUserCmd;
import com.vokamis.ity.cmd.ExitCmd;
import com.vokamis.ity.cmd.RegisterCmd;
import com.vokamis.ity.cmd.ShowHomeViewCmd;
import com.vokamis.ity.cmd.ShowLeaderboardCmd;
import com.vokamis.ity.cmd.ShowProgressCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.ICommand;
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.state.AppSettings;
import com.vokamis.ity.state.StateManager;
import com.vokamis.ity.view.AuthUserView;
import com.vokamis.ity.view.HomeView;
import com.vokamis.ity.view.LeaderboardView;
import com.vokamis.ity.view.ProgressView;
import com.vokamis.ity.view.RegisterView;
import com.vokamis.ity.view.TestView;

public abstract class RootActivity extends Activity implements IViewManager {
	  	
	private StateManager sm;
	private IViewContainer current;
	private HomeView hview;
	private TestView tview;
	private LeaderboardView lview;
	private ProgressView pview;
	private RegisterView rview;
	private AuthUserView aview;
	private RootActivity _this;  
	
	final public String getAppVersion(){
		try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return pkgInfo.versionName;
		} catch (Exception e){
			return "unknown";
		}
	}

	public abstract String getEditionName();

	public abstract String getAppName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		_this = this;
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.main);
    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
    		
		updateStatus("Launching", "");
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		sm = new StateManager(this, this, getResources().getString(R.string.api_endpoint));
					
		{
  		View main = getLayoutInflater().inflate(R.layout.main, null);
  		setContentView(main);
  		hview = new HomeView(this, main);
		}
		
		{
  		View main = getLayoutInflater().inflate(R.layout.register, null);
  		setContentView(main);
  		rview = new RegisterView(this, main);
		}

		{
  		View main = getLayoutInflater().inflate(R.layout.register, null);
  		setContentView(main);
  		aview = new AuthUserView(this, main);
		}
		
		tview = new TestView(this, new WebView(this));
		lview = new LeaderboardView(this, new WebView(this));
		pview = new ProgressView(this, new WebView(this));
		  
		dispatch(new ShowHomeViewCmd());		
	}
	
	@Override
	public StateManager getStateManager(){
		return sm;
	}
	
	public AppSettings getSettings(){
		return sm.getSettings();
	}
	
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bun = msg.getData();
			if (bun.containsKey(RootActivity.class.getName())){
				ICommand cmd = (ICommand) bun.getSerializable(RootActivity.class.getName());
				dispatchInternal(cmd);
			} else {
				super.handleMessage(msg);
			}
		}
	};
	
	public void dispatch(ICommand cmd){
   	Bundle bun = new Bundle();
  	bun.putSerializable(RootActivity.class.getName(), cmd);
  	Message msg = Message.obtain();
  	msg.setData(bun);
		handler.sendMessage(msg);
	}
	
	private void dispatchInternal(ICommand cmd){
		if (cmd instanceof ShowHomeViewCmd){
			setContentView(hview.getContentView());
			hview.dispatch((ShowHomeViewCmd) cmd);
			onActivate(hview);
			return;
		} 
		
		if (cmd instanceof StartTestCmd){
			StartTestCmd c = (StartTestCmd) cmd;
			if (getSettings().getAccount(c.getAccountIdx()).getAuthToken() == null){
				cmd = new AuthUserCmd(c.getGrade(), c.getAccountIdx());
				dispatch(cmd);
				return;
			} else {
				setContentView(tview.getContentView());
				tview.dispatch((StartTestCmd) cmd);
				onActivate(tview);
				return;
			}
		}
		
		if (cmd instanceof ShowLeaderboardCmd){
			ShowLeaderboardCmd c = (ShowLeaderboardCmd) cmd;
			if (getSettings().getAccount(c.getAccountIdx()).getAuthToken() == null){
				cmd = new AuthUserCmd(0, c.getAccountIdx());
				dispatch(cmd);
				return;
			} else {
				setContentView(lview.getContentView());
				lview.dispatch((ShowLeaderboardCmd) cmd);
				onActivate(lview);
				return;
			}
		}
		
		if (cmd instanceof ShowProgressCmd){
			ShowProgressCmd c = (ShowProgressCmd) cmd;
			if (getSettings().getAccount(c.getAccountIdx()).getAuthToken() == null){
				cmd = new AuthUserCmd(0, c.getAccountIdx());
				dispatch(cmd);
				return;
			} else {
				setContentView(pview.getContentView());
				pview.dispatch(c);
				onActivate(pview);
				return;
			}
		}
		
		if (cmd instanceof RegisterCmd){
			setContentView(rview.getContentView());
			rview.dispatch((RegisterCmd) cmd);
			onActivate(rview);
			return;
		}
		
		if (cmd instanceof AddExistingUserCmd){
			setContentView(aview.getContentView());
			aview.dispatch((AddExistingUserCmd) cmd);
			onActivate(aview);
			return;
		}

		if (cmd instanceof AuthUserCmd){
			setContentView(aview.getContentView());
			aview.dispatch((AuthUserCmd) cmd);
			onActivate(aview);
			return;
		}

		if (cmd instanceof ExitCmd){
			int pid = android.os.Process.myPid(); 
			android.os.Process.killProcess(pid); 			
			return;
		}
		
		throw new RuntimeException();
	}

	private void onActivate(IViewContainer view){
		current = view;
		updateStatus(view.getTitle(), "");
	}
	
	private void updateStatus(String title, String status){
		if (status != null && status.length() != 0){
			status = ": " + status;
		}
		TextView header = (TextView) findViewById(R.id.wnd_title);
    header.setText(getAppName() + " - " + title + status);
	}
	
	@Override
	public void updateStatus(IViewContainer view, String status){
		updateStatus(view.getTitle(), status);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
    if(keyCode == KeyEvent.KEYCODE_BACK) {
    	
    	// pop the view if any
    	if (current != null && current != hview){
    		dispatch(new ShowHomeViewCmd());
    		return true;
    	}  
    	
    	// simply exit if no more views
    	_this.dispatch(new ExitCmd());          	
    	return true;
    	
    } 

    return false;
	}
	
	@Override
	public void toast(String message){
		Display display = getWindowManager().getDefaultDisplay(); 
		int height = display.getHeight();
		
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, height / 5);
		toast.show();
	}
	
	@Override
	public String getResourceString(int resourceId){
		return getResources().getString(resourceId);	
	}
	
}