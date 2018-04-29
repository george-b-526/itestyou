package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class StartTestCmd implements ICommand {

	int grade;
	int accountIdx;
	int unitId;
	
	public StartTestCmd(int grade, int unitId, int accountIdx) {
		this(grade, accountIdx);
		this.unitId = unitId;
	}
	
	public StartTestCmd(int grade, int accountIdx) {
	  super();
	  
	  this.grade = grade;
	  this.accountIdx = accountIdx;
  }
	
	public int getGrade() {
  	return grade;
  }

	public int getUnitId() {
  	return unitId;
  }
	
	public int getAccountIdx(){
		return accountIdx;
	}
	
}
