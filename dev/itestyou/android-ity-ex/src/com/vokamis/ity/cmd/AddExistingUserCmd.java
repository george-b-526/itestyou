package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class AddExistingUserCmd implements ICommand {
	
	int grade;
	int accoundIndex;
	
	public AddExistingUserCmd(int grade, int accoundIndex) {
		super();
		this.accoundIndex = accoundIndex;
		this.grade = grade;
	}

	public int getAccoundIdx() {
  	return accoundIndex;
  }

	public int getGrade() {
  	return grade;
  }

}
