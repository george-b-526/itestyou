package com.oy.tv.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface ILog {
	
	public void message(String message);
	public void error(Exception e);

	public static class SystemOutLogProvider implements ILog {
		public void error(Exception e){
			e.printStackTrace();
			message(e.getMessage());
		}
		
		public void message(String message){
			String id = "thread:" + Thread.currentThread().getId();
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		    String date = formatter.format(new Date()); 
	 
		    message = "log >> " + date + "\t" + id + "\t" +  message; 
	 
		    System.out.println(message);
		}
	}
	
}
