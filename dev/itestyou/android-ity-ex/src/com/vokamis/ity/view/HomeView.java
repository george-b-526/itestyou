package com.vokamis.ity.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.vokamis.ity.R;
import com.vokamis.ity.cmd.AddExistingUserCmd;
import com.vokamis.ity.cmd.RegisterCmd;
import com.vokamis.ity.cmd.ShowHomeViewCmd;
import com.vokamis.ity.cmd.ShowLeaderboardCmd;
import com.vokamis.ity.cmd.ShowProgressCmd;
import com.vokamis.ity.cmd.StartTestCmd;
import com.vokamis.ity.mvc.IViewContainer;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.EntityPolicy;
import com.vokamis.ity.state.ActorAccount;
import com.vokamis.ity.state.AppSettings;
import com.vokamis.ity.util.ViewHelper;

public class HomeView implements IViewContainer {
	
	private final IViewManager parent;
	private final View root;
	
	public HomeView(IViewManager parent, View root){
		this.parent = parent;
		this.root = root; 
	}

	public String getTitle(){
		return "Home Page";
	}
	
	public View getContentView(){
		return root;
	}

	public void dispatch(ShowHomeViewCmd cmd){
		Context context = root.getContext();  

		AppSettings current = parent.getStateManager().getSettings();		
		
		{
			TextView editionName = (TextView) root.findViewById(R.id.editionName);
			editionName.setText(parent.getEditionName());
		}
		
		final Spinner sAccount;
		{
			List<String> names = new ArrayList<String>();
			for (ActorAccount aa : current.getAccounts()){
				names.add(aa.getEmail());
			}
			names.add("< new account >");
			names.add("< existing account >");
			
  		sAccount = (Spinner) root.findViewById(R.id.account);
   		ViewHelper.fillSpinner(context, sAccount, names);
   		sAccount.setSelection(current.getAccountIdx());
		}
				 		
		class MyClickListener implements OnClickListener {
			public void onClick(View v) {
	    	if (EntityPolicy.mustUpgrade()) {
	    		parent.toast("You must upgrade this application to continue.");
	    		return;
	    	}
	    	
	    	int grade = 0; 
	    	int accountIdx = (int) sAccount.getSelectedItemId();

	    	boolean addNew = accountIdx == parent.getStateManager().getSettings().getAccounts().size();
	    	boolean addExisting = accountIdx == parent.getStateManager().getSettings().getAccounts().size() + 1;
	    	
	    	if (addNew){
	    		parent.dispatch(new RegisterCmd(grade));
	    	} else {
	    		if (addExisting){
	    			parent.dispatch(new AddExistingUserCmd(grade, -1));
	    		} else {
	    			parent.getStateManager().selectAccount(accountIdx);
	    			onExit(grade, accountIdx);
	    		}  	    		
	    	}
	    }
			
			public void onExit(int grade, int accountIdx){}
		}
		
		final Button bPlay;
		{
  		bPlay = (Button) root.findViewById(R.id.play);
  		bPlay.getBackground().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
  		bPlay.setOnClickListener( new MyClickListener() {
  			@Override
  			public void onExit(int grade, int accountIdx){
  				parent.dispatch(new StartTestCmd(grade, accountIdx));
  			}  			
  		});
		}

		final Button bLeader;
		{
			bLeader = (Button) root.findViewById(R.id.leaderboard);
			bLeader.setOnClickListener( new MyClickListener() {
  			@Override
  			public void onExit(int grade, int accountIdx){
  				parent.dispatch(new ShowLeaderboardCmd(accountIdx));
  			}  			
  		});
		}
		  
		final Button bProgress;
		{
			bProgress = (Button) root.findViewById(R.id.progress);
			bProgress.setOnClickListener( new MyClickListener() {
  			@Override
  			public void onExit(int grade, int accountIdx){
  				parent.dispatch(new ShowProgressCmd(accountIdx));
  			}  			
  		});
		}
  	
	}

	
}
