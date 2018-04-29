package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class ShowLeaderboardCmd implements ICommand {

	int accoundIndex;
	
	public ShowLeaderboardCmd(int accoundIndex) {
		super();
		this.accoundIndex = accoundIndex;
	}
  
	public int getAccountIdx() {
  	return accoundIndex;
  }
	
}
