package com.vokamis.ity.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.Handler;

import com.vokamis.ity.rpc.PolicyException;

public class Rpc {

	static final int timeoutConnection = 15 * 1000;
	static final int timeoutSocket = 15 * 1000;

	
	public String execute(HttpUriRequest request) throws PolicyException {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

		DefaultHttpClient client = new DefaultHttpClient(httpParameters);

		String response = null;
		try {			
			HttpResponse httpResponse = client.execute(request);
			try {
  			int code = httpResponse.getStatusLine().getStatusCode();
  			if (code != 200){
  				throw new PolicyException("Server returned error.");
  			}
  			HttpEntity entity = httpResponse.getEntity();
  			if (entity != null) {
  				InputStream instream = entity.getContent();
  				response = readFully(instream);
  				instream.close();
  			}
			} catch (Exception e){
				e.printStackTrace(System.err);
				throw new PolicyException("Error while receiving server request.");
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new PolicyException("Unable to connect to the server.");
		} finally {
			client.getConnectionManager().shutdown();
		}
 
		return response;		
	}

	private static String readFully(InputStream is) throws Exception  {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	class Client {

		public void sendRequest(final HttpUriRequest request,
		    ResponseListener callback) {
			(new AsynchronousSender(request, new Handler(), new CallbackWrapper(
			    callback))).start();
		}

	}

	public interface ResponseListener {

		public void onResponseReceived(HttpResponse response);

	}

	public class AsynchronousSender extends Thread {

		private final DefaultHttpClient httpClient = new DefaultHttpClient();

		private HttpUriRequest request;
		private Handler handler;
		private CallbackWrapper wrapper;

		protected AsynchronousSender(HttpUriRequest request, Handler handler,
		    CallbackWrapper wrapper) {
			this.request = request;
			this.handler = handler;
			this.wrapper = wrapper;
		}

		public void run() {
			try {
				final HttpResponse response;
				synchronized (httpClient) {
					response = getClient().execute(request);
				}
				// process response
				wrapper.setResponse(response);
				handler.post(wrapper);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private HttpClient getClient() {
			return httpClient;
		}

	}

	public class CallbackWrapper implements Runnable {

		private ResponseListener callbackActivity;
		private HttpResponse response;

		public CallbackWrapper(ResponseListener callbackActivity) {
			this.callbackActivity = callbackActivity;
		}

		public void run() {
			callbackActivity.onResponseReceived(response);
		}

		public void setResponse(HttpResponse response) {
			this.response = response;
		}

	}

	
}
