package com.oy.tv.util;

import java.io.FileInputStream;
import java.util.Properties;

import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.tv.app.ILog;

public class DynamicPropertyManager implements IPropertyProvider {

	public final static String NS_NAME = "DPM_NS";
	public static int RELOAD_TIME = 5 * 1000;
	
	class CustomThread extends Thread {
		boolean terminated = false;
		public void run(){
			while(!terminated){
				try {
					Thread.sleep(RELOAD_TIME);
					reload();
				} catch (Exception e){
					log.error(e);
				}
			}
		}		
	}
	
	private Object lock = new Object();
	private String fileName;
	private ILog log;
	private Properties props;
	private CustomThread thread = new CustomThread();
	
	public void open(ILog log, String fileName) throws Exception {
		this.log = log;
		this.fileName = fileName;
		  
		System.out.println(DynamicPropertyManager.class.getName() + " open with " + NS_NAME + "=" + System.getenv(NS_NAME));
		reload();
		
		thread = new CustomThread();
		thread.start();
	}
	
	private void reload() throws Exception {
		FileInputStream fos = new FileInputStream(fileName);
		Properties newProps;
		try {
			newProps = new Properties();
			newProps.load(fos);
		} finally {
			fos.close();
		}
		      
		synchronized(lock){
			props = newProps;
		}
	}
	  
	public String getPropertyValue(String name){
		synchronized(lock){    
			String ns = System.getenv(NS_NAME);
			String hname = name + "@" + ns;
			if (ns != null && props.containsKey(hname)){
				return props.getProperty(hname);
			} else {
				return props.getProperty(name);
			}
		}  
	}

	public String getPropertyValueRaw(String name){
		return getPropertyValue(name);
	}
	
	public boolean hasPropertyValue(String name){
		synchronized(lock){
			return getPropertyValue(name) != null;
		}
	}
	
	public String [] getPropertyValues(String name){
		synchronized(lock){
			return new String [] {getPropertyValue(name)};
		}
	}
	
	public void close() {
		try {
			thread.terminated = true;
			thread.interrupt();
			thread.join();
		} catch (Exception e){
			log.error(e);
		}
	}
	
}
