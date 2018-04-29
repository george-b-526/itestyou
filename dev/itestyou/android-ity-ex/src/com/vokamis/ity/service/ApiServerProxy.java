package com.vokamis.ity.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.vokamis.ity.mvc.IViewManager;
import com.vokamis.ity.rpc.ApiService.RecoverEnvelope;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;
import com.vokamis.ity.rpc.PolicyException;


public class ApiServerProxy {

	private Rpc rpc = new Rpc();
	
	private final String userAgent;
	private final String apiEndpoint;
	
	public ApiServerProxy(IViewManager parent, String apiEndpoint){
		this.apiEndpoint = apiEndpoint;
  
		userAgent = 
			"app: " + parent.getAppVersionId() + ";" +
			"android:" + android.os.Build.VERSION.CODENAME + ";" + 
			"release:" + android.os.Build.VERSION.RELEASE + ";" +
			"id:" + android.os.Build.ID + ";" +
			"hw:" + android.os.Build.HARDWARE + ";" +
			"device:" + android.os.Build.DEVICE + ";" +
			"brand:" + android.os.Build.BRAND  + ";";
	}

	public RecoverEnvelope recover(String email, String deviceId) throws PolicyException {
		HttpPost verb = new HttpPost(apiEndpoint);  
		{
  		List<NameValuePair> nvp = new ArrayList<NameValuePair>();  
  		
  		nvp.add(new BasicNameValuePair("verb", "recover"));
  		nvp.add(new BasicNameValuePair("name", email));
  		nvp.add(new BasicNameValuePair("device-id", deviceId));
  		
  		try {
  			verb.setEntity(new UrlEncodedFormEntity(nvp));
  		} catch (Exception e){
  			throw new PolicyException("Error preparing message for the server.");
  		}
  		
			verb.addHeader("User-Agent", userAgent);
		}
		return RecoverEnvelope.valueOf(rpc.execute(verb));
	}
	
	public RegisterEnvelope register(String email, String pwd, boolean existing, String deviceId)
		throws PolicyException {
		HttpPost verb = new HttpPost(apiEndpoint);  
		{
  		List<NameValuePair> nvp = new ArrayList<NameValuePair>();  
  		
  		nvp.add(new BasicNameValuePair("verb", "register"));
  		nvp.add(new BasicNameValuePair("name", email));
  		nvp.add(new BasicNameValuePair("pwd", pwd));
  		nvp.add(new BasicNameValuePair("existing", (existing ? "true" : "false")));
  		nvp.add(new BasicNameValuePair("device-id", deviceId));
  		
  		try {
  			verb.setEntity(new UrlEncodedFormEntity(nvp));
  		} catch (Exception e){
  			throw new PolicyException("Error preparing message for the server.");
  		}
  		
			verb.addHeader("User-Agent", userAgent);
		}
		return RegisterEnvelope.valueOf(rpc.execute(verb));
	}

}
