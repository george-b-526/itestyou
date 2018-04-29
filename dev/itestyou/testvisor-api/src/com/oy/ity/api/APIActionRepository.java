package com.oy.ity.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oy.tv.api.ApiServiceStub;
import com.oy.tv.db.AnyDB;
import com.oy.tv.util.DynamicPropertyManager;
import com.oy.tv.util.HttpUtil;
import com.vokamis.ity.rpc.ApiService;
import com.vokamis.ity.rpc.ApiService.LookupEnvelope;
import com.vokamis.ity.rpc.ApiService.RecoverEnvelope;
import com.vokamis.ity.rpc.ApiService.RegisterEnvelope;
import com.vokamis.ity.rpc.ApiService.RepasswordEnvelope;
import com.vokamis.ity.rpc.PolicyException;

public class APIActionRepository {

  static List<Action> actions = new ArrayList<Action>(Arrays.asList(
  		new Register(), new Recover(), new Lookup(), new Repassword()));
  
}

class NameValue {
	final String name;
	final Object value;

	NameValue(String name, Object value){
		this.name = name;
		this.value = value;
	}
}

class Action {
	final String verb;
	
	Action(String verb){
		this.verb = verb;
	}
	
	public void reply(HttpServletResponse resp, int status, String reason, NameValue ... optional) 
		throws IOException {
		resp.setStatus(200);
		reply(resp.getWriter(), status, reason, optional);
	}
		
	public void reply(PrintWriter out, int status, String reason, NameValue ... optional) 
		throws IOException {
  		out.print("<ity-api-result verb='" + verb + "' ver='1.0'>");
  		out.print("<status>" + status + "</status>");
  		if (reason != null){
  			out.print("<reason>" + reason + "</reason>");
  		}
  		for (NameValue nv : optional){
  			if (nv.value != null){
  				out.print("<" + nv.name + ">" + nv.value.toString() + "</" + nv.name + ">");
  			}
  		}
  		out.print("</ity-api-result>");
	}
	
	void execute(HttpServletRequest req, HttpServletResponse resp, 
		DynamicPropertyManager dpm, AnyDB db) throws Exception {}
}	

class Lookup extends Action {
	Lookup() {
		super("lookup");
	}

	void execute(HttpServletRequest req, HttpServletResponse resp, DynamicPropertyManager dpm, AnyDB db)
  	throws Exception {
		
		LookupEnvelope result;
		
		String token = req.getParameter("token");
		if (token == null || token.length() == 0){
			result = new ApiService.LookupEnvelope(
					LookupEnvelope.Status.FAILED, "Invalid token.");
		} else {
  		String deviceId = req.getParameter("device-id");
  		String ipAddress = HttpUtil.getRemoteAddr(req);
  
  		String userAgent = req.getHeader("User-Agent");
  		if (userAgent == null){
  			userAgent = "<unknown>";
  		}
  
  		try {  
  			ApiServiceStub stub = new ApiServiceStub(db, dpm);
  			result = stub.lookup(token, deviceId, ipAddress, userAgent);
  		} catch (PolicyException pe){
  			result = new ApiService.LookupEnvelope(
  					LookupEnvelope.Status.FAILED, pe.getMessage());
  		}
		}
		
		reply(resp, result.getStatus().ordinal(), result.getReason(), 
				new NameValue("user-id", result.getUserId()),
				new NameValue("is-pro", result.isPro() ? "1" : "0"),
				new NameValue("name", result.getName()));
	}

}

class Recover extends Action {
	Recover() {
		super("recover");
	}

	void execute(HttpServletRequest req, HttpServletResponse resp, DynamicPropertyManager dpm, AnyDB db)
	    throws Exception {

		RecoverEnvelope result;
		String name = req.getParameter("name");
		String deviceId = req.getParameter("device-id");
		String ipAddress = HttpUtil.getRemoteAddr(req);

		String userAgent = req.getHeader("User-Agent");
		if (userAgent == null){
			userAgent = "<unknown>";
		}

		try {
			ApiServiceStub stub = new ApiServiceStub(db, dpm);
			result = stub.recover(name, deviceId, ipAddress, userAgent);
		} catch (PolicyException pe){
			result = new ApiService.RecoverEnvelope(
					RecoverEnvelope.Status.FAILED, pe.getMessage());
		}
		
		reply(resp, result.getStatus().ordinal(), result.getReason());
	}
}

class Register extends Action {
	Register() {
		super("register");
	}

	void execute(HttpServletRequest req, HttpServletResponse resp, DynamicPropertyManager dpm, AnyDB db) throws Exception {

		RegisterEnvelope result;
		{
			String name = req.getParameter("name");
  		String pwd = req.getParameter("pwd");
  		String existing = req.getParameter("existing");
  		String deviceId = req.getParameter("device-id");
  		String ipAddress = HttpUtil.getRemoteAddr(req);

  		boolean _existing = "true".equals(existing) || "1".equals(existing); 
  		
  		String userAgent = req.getHeader("User-Agent");
  		if (userAgent == null){
  			userAgent = "<unknown>";
  		}
  		 
  		try { 
  			ApiServiceStub stub = new ApiServiceStub(db, dpm);
  			result = stub.register(name, pwd, _existing, deviceId, ipAddress, userAgent);
  		} catch (PolicyException pe){
  			result = new ApiService.RegisterEnvelope(
  					RegisterEnvelope.Status.FAILED, null, pe.getMessage());
  		}
		}

		reply(resp, result.getStatus().ordinal(), result.getReason(), 
				new NameValue("token", result.getToken()));
	}
}

class Repassword extends Action {
	Repassword() {
		super("repassword");
	}

	void execute(HttpServletRequest req, HttpServletResponse resp, DynamicPropertyManager dpm, AnyDB db)
	    throws Exception {

		RepasswordEnvelope result;
		{
			String name = req.getParameter("name");
  		String oldPassword = req.getParameter("old-password");
  		String newPassword = req.getParameter("new-password");
  		String deviceId = req.getParameter("device-id");
  		String ipAddress = HttpUtil.getRemoteAddr(req);

  		String userAgent = req.getHeader("User-Agent");
  		if (userAgent == null){
  			userAgent = "<unknown>";
  		}
  		 
  		try { 
  			ApiServiceStub stub = new ApiServiceStub(db, dpm);
  			result = stub.repassword(name, oldPassword, newPassword, deviceId, ipAddress, userAgent);
  		} catch (PolicyException pe){
  			result = new ApiService.RepasswordEnvelope(
  					RepasswordEnvelope.Status.FAILED, pe.getMessage());
  		}
		}
 
		reply(resp, result.getStatus().ordinal(), result.getReason());
	}
}
