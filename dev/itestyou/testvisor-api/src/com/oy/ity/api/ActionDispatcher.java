package com.oy.ity.api;
  
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.ILog;
import com.oy.tv.dao.identity.AllDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.tmpl.TemplateEngine;
import com.oy.tv.util.DynamicPropertyManager;

public class ActionDispatcher extends HttpServlet {
 	
	static final long serialVersionUID =0;
	
	private DynamicPropertyManager dpm = new DynamicPropertyManager();

	@Override
	public void init() throws ServletException { 
		try {  
			TemplateEngine.init();
		  	
			dpm.open(
				new ILog.SystemOutLogProvider(), 
				BaseViewCtx.resolveLocalPath(getServletContext(), "/WEB-INF/config.properties")
			);
			
			AnyDB db = beginDb();
			try {
				new AllDAO().init(db);
			} finally {
				endDb(db);
			} 
		} catch(Exception e){
			throw new ServletException(e);
		}    
	}

	@Override
	public void destroy() { 
		dpm.close();
	}  
	  	
	private AnyDB beginDb() throws Exception {
		String dbName = dpm.getPropertyValue(ResourceLocator.DB_NAME_PROPERTY_NAME);
		String connStr = dpm.getPropertyValue(ResourceLocator.CONN_STR_PROPERTY_NAME);

		AnyDB db = new AnyDB();
		db.open_mysql(connStr, dbName);
	
		return db;
	}
	
	private void endDb(AnyDB db) throws Exception {
		db.close();
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		
		String verb = req.getParameter("verb");
		
		{
  		resp.setContentType("text/html");
  		req.setCharacterEncoding("UTF-8");
  		resp.setCharacterEncoding("UTF-8");
		}
		
		{
  		String active = dpm.getPropertyValue("active");
  		if (!"true".equals(active)){ 
  			renderSystemError(resp, 503, verb, "We are updating our system, please try later.");
  			return;
  		}
		}

		for (Action action : APIActionRepository.actions){
			if (action.verb.equals(verb)){
				try {
  				AnyDB db = beginDb();
  				try {
  					action.execute(req,resp, dpm, db);  
  				} finally {
  					endDb(db);
  				}
  				return;
				} catch (Throwable t){
					System.err.println("Exception on " + new Date());
					t.printStackTrace(System.err);
					renderSystemError(resp, 500, verb, "Internal server error.");
				}
			}	
		}
		
		{
  		renderSystemError(resp, 400, verb, "Unknown verb.");
		}
	}

	public static void renderSystemError(HttpServletResponse resp, int code, String verb, String reason) throws IOException {
		if (verb == null || verb.length() == 0){
			verb = "unknown";
		}
		
		resp.setStatus(code);
		
		PrintWriter out = resp.getWriter();
		out.print("<ity-result verb='" + URLEncoder.encode(verb, "UTF-8") + "' ver='1.0'>");
		out.print("<code>" + code + "</code>");
		out.print("<reason>" + reason + "</reason>");
		out.print("</ity-result>");
	}
	  	
}