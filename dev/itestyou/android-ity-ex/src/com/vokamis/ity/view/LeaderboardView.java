package com.vokamis.ity.view;

import android.webkit.WebView;

import com.vokamis.ity.cmd.ShowLeaderboardCmd;
import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.state.ActorAccount;
import com.vokamis.ity.state.StateManager;
import com.vokamis.ity.util.AbstractWebView;

public class LeaderboardView extends AbstractWebView {

	private ShowLeaderboardCmd cmd;
	
	public LeaderboardView(IViewManager parent, WebView root){
		super(parent, root);
	}
	
	@Override
	public String getTitle(){
		return "Leaderboard";
	}
	
	public void dispatch(ShowLeaderboardCmd cmd){
		this.cmd = cmd;
		dispatch();
	}
	
	@Override
	public String getStartUrl(){
		ActorAccount account = parent.getStateManager().getSettings().
			getAccount(cmd.getAccountIdx());

		String token = null;
		if (account != null){
			token = account.getAuthToken();
		}
		
		return StateManager.makeLeaderboardUrl(parent, token);
	}
	
}
