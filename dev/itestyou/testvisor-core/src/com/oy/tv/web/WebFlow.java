package com.oy.tv.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.xml.sax.SAXException;

import com.meterware.httpunit.AuthorizationRequiredException;
import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.DNSListener;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieProperties;

public class WebFlow {
	
	public String base_url;
	public File base_dir;   
	
	private String base_ip;
	
	private int idx;
	protected WebConversation wc;
	
	private String url;
	private WebResponse response;
	private String page;
	private String mime;
	
	public WebFlow(){
		this(null, null, null);
	}

	public WebFlow(String baseIP, String baseUrl, String baseDir){
		base_ip = baseIP;
		base_url = baseUrl;
		
		if (baseDir != null){
			base_dir = new File(baseDir);
		}
	}
	
	public void clearCache() {
		if (base_dir != null){
			if (base_dir.exists()){
				deleteDir(base_dir);
			}
		}
	}
  
	public void execute() {
		// init storage space on disk
		clearCache();

		if (base_dir != null){
			if (!base_dir.mkdirs()){
				throw new RuntimeException("Failed to created dirs");
			}
		}
		
		// configure
		HttpUnitOptions.setScriptingEnabled(false);
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);
		
		//
		// some phpbb authoratory cookies were rejected; this forces them to be accepted
		// 
		CookieProperties.setDomainMatchingStrict(false);
		
		// init new conversation
		wc = new WebConversation();
		
		//
		// HttpUnitOptions.setLoggingHttpHeaders(true);
		//
				
		// configure
		ClientProperties props = wc.getClientProperties();
		props.setAutoRedirect(true);
		props.setAcceptCookies(true);
		
		if (base_ip != null){
			class MyDNSListener implements DNSListener {
				public String getIpAddress(String hostName){
					return base_ip;
				}
			}
			props.setDnsListener(new MyDNSListener());
		}		
	}
	 
	public void setAuthorization(String name, String pwd){
		wc.setAuthorization(name, pwd);
	}
	
	protected String user_agent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.1.4) Gecko/20091016 Firefox/3.5.4 (.NET CLR 3.5.30729)";

	public static class NameValue {
		String name;
		String value;
		
		public NameValue(String name, String value){
			this.name = name;
			this.value = value;
		}
	}
	
	public void post(String url, NameValue ... nvs) {
		// qualify URL
		this.url = url;
		if (base_url != null){
			this.url = base_url + url;
		}
		
		try {  
			PostMethodWebRequest req = new PostMethodWebRequest(this.url);
			for (NameValue nv : nvs){
				req.setParameter(nv.name, nv.value);
			}
			
			req.setHeaderField(
				"User-Agent", 
				user_agent
			);
			if (response != null){
				req.setHeaderField(
					"Referrer", 
					response.getURL().toString()
				);
			}
			response = wc.getResponse(req);
		} catch (AuthorizationRequiredException are){
			throw are;
		} catch (Exception e){
			throw new RuntimeException("Failed to load " + url, e);
		}
		
		if (response == null) { 
			throw new RuntimeException("Failure trying to retrieve : " + url);  
		}
		
		int responseCode = response.getResponseCode();        
		if (responseCode > 200) {
			throw new RuntimeException("Error getting page: Returned Status " 
					+ responseCode);
		}
		  
		save();
	}
	
	public void get(String url) {
		// qualify URL
		this.url = url;
		if (base_url != null){
			this.url = base_url + url;
		}
		
		try {  
			WebRequest req = new GetMethodWebRequest(this.url);
			req.setHeaderField(
				"User-Agent", 
				user_agent
			);
			if (response != null){
				req.setHeaderField(
					"Referrer", 
					response.getURL().toString()
				);
			}
			response = wc.getResponse(req);
		} catch (AuthorizationRequiredException are){
			throw are;
		} catch (Exception e){
			throw new RuntimeException("Failed to load " + url, e);
		}
		
		if (response == null) { 
			throw new RuntimeException("Failure trying to retrieve : " + url);  
		}
		
		int responseCode = response.getResponseCode();        
		if (responseCode > 200) {
			throw new RuntimeException("Error getting page: Returned Status " 
					+ responseCode);
		}
		  
		save();
	}
	
	public void rawBytesToFile(FileOutputStream file_output) throws Exception {
		// read fully
		page = response.getText();		
		
		// write out 
		ByteArrayInputStream bais = (ByteArrayInputStream) response.getInputStream();
		
		byte [] bytes = new byte [bais.available()];
		bais.read(bytes);
		
		file_output.write(bytes);
		file_output.close();
	}
	
	private void save(){
		// load fully
		try {
			page = response.getText();		
		} catch (IOException ioe){
			throw new RuntimeException(ioe);
		}
		
		// extract "html" from "text/html"
		mime = response.getContentType();
		String ext = response.getContentType().split("/")[1];

		// save to file
		if (base_dir != null){
			try {
				FileOutputStream fos = new FileOutputStream(new File(base_dir.getAbsolutePath() + "/page." + idx + "." + ext));
				fos.write(page.getBytes(response.getCharacterSet()));
				fos.close();
				    
				FileWriter fw = new FileWriter(new File(base_dir.getAbsolutePath() + "/head." + idx + ".txt"));
				fw.write(response.toString());
				fw.close();
			} catch  (Exception e){
				throw new RuntimeException(e);
			}
		}
		   
		idx++;
	}
	
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
    
    private WebForm getFormByName(WebResponse response, String name) throws SAXException {
        WebForm form = null;
        
        for (int i=0; i < response.getForms().length; i++){
        	if (
    			name == null || 
        		response.getForms()[i].getName().equals(name) || 
        		response.getForms()[i].getID().equals(name)
    		){
                form = response.getForms()[i];
                break;
            }
        }
        
        if (form == null) {
            throw new RuntimeException("No applicable form <" 
                    + name + "> found on page: " 
                    + response.getURL());
        }                  
        
        return form;
    }
   
    private static WebResponse actPOST(WebForm form, String buttonName, String [] names, String [] values) throws Exception {
    	
		// set form fields if provided
		if (values != null) {
			for(int i=0; i < values.length; i++){                
				
                //
                // Checking if the parameter exists in the form; otherwise
                // httpunit fails with an ArrayIndexOutOfBoundsException.
                //
                if (form.getParameter(names[i]) == null ){
                    throw new RuntimeException("No field " + names[i]);
                }
                
                // set value to the web client
                form.setParameter(names[i], values[i]);                
            }                           
		}		
    
		SubmitButton btn = getSubmitButton(form, buttonName);
		 
		return form.submit(btn);
    } 
    
    private static SubmitButton getSubmitButton(WebForm form, String name) {
        SubmitButton[] buttons = form.getSubmitButtons();
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getName().equals( name ) || buttons[i].getID().equals( name )) {
                return buttons[i];
            }
        }
        return null;
    }
    
    public void post(int formIndex, String buttonName, String [] names, String [] values){
    	try {
        	WebForm frm = response.getForms()[formIndex];
        	post(frm, buttonName, names, values);
    	} catch (Exception e){
    		throw new RuntimeException(e);
    	}
    }

    public void post(String form, String buttonName, String [] names, String [] values){
    	try {
        	WebForm frm = getFormByName(response, form);
        	post(frm, buttonName, names, values);
    	} catch (Exception e){
    		throw new RuntimeException(e);
    	}
    }
    
    private void post(WebForm frm, String buttonName, String [] names, String [] values) throws Exception {
    	response = actPOST(frm, buttonName, names, values);
    	save();
    	this.url = response.getURL().toString(); 
    }	
    
    protected void alink(String linkText){
    	try {  
    		WebLink link = response.getLinkWith(linkText);
    		get(link.getURLString());
    	} catch (Exception e){
    		throw new RuntimeException(e);
    	}
    }
	
    public String getLastPage(){
    	return page;
    }
    
    public String getLastMime(){
    	return mime;
    }
    
}
