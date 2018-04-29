package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class AuthUserCmd implements ICommand {
	
	int grade;
	int accountIdx;
	
	public AuthUserCmd(int grade, int accountIdx) {
		super();
		this.accountIdx = accountIdx;
		this.grade = grade;
	}

	public int getAccoundIdx() {
  	return accountIdx;
  }

	public int getGrade() {
  	return grade;
  }

}
