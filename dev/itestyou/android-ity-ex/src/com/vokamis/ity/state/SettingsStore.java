package com.vokamis.ity.state;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.SharedPreferences;
import android.util.Base64;

class SettingsStore {

	private SharedPreferences settings;
	
	SettingsStore(SharedPreferences settings){
		this.settings = settings;
	}
	
	public boolean hasSettings(){
		return settings.getString("AppSettings", null) != null;
	}
	
	public void saveSettings(AppSettings current) {
  	SharedPreferences.Editor prefEditor = settings.edit();  
  	try {
  		saveSettings(prefEditor, current);
  	} finally {
  		prefEditor.commit();
  	}
	}

	private void saveSettings(SharedPreferences.Editor prefEditor, AppSettings current) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(current);
		} catch (IOException ioe){
			throw new RuntimeException(ioe);
		}
		prefEditor.putString("AppSettings", Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT));
	}

	public AppSettings loadSettings() {
		AppSettings current;
		String string = settings.getString("AppSettings", null);
		try {
			byte [] bytes = Base64.decode(string, Base64.DEFAULT);
  		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			current = (AppSettings) ois.readObject();
		} catch (Exception ioe){
			throw new RuntimeException(ioe);
		}		
		return current;
	}
	
}
