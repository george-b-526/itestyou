package com.vokamis.ity.cmd;

import com.vokamis.ity.mvc.ICommand;

public class RegisterCmd implements ICommand {

	int grade;

	public RegisterCmd(int grade) {
	  super();
	  this.grade = grade;
  }
	
	public int getGrade() {
  	return grade;
  }

}
