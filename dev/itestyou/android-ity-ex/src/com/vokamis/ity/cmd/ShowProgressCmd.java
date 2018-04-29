package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class ShowProgressCmd implements ICommand {

	int accoundIndex;
	
	public ShowProgressCmd(int accoundIndex) {
		super();
		this.accoundIndex = accoundIndex;
	}
  
	public int getAccountIdx() {
  	return accoundIndex;
  }
	
}
