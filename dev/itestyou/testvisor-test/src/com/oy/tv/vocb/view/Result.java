package com.oy.tv.vocb.view;

import com.oy.tv.dao.runtime.ResponseDAO;

public class Result {
	int [] fail;
	ResponseDAO.Score score;		

	public ResponseDAO.Score getScore(){
		return score;
	}
}
