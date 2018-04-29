package com.oy.tv.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseView;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.db.AnyDB;

public class BaseViewCtx implements IDatabaseCtx {

	protected String connStr;
	protected String dbName;
	
	
	private ILog log = new ILog.SystemOutLogProvider();
	private AnyDB db;	
	private Exception lastError;
	private List<String> messages = new ArrayList<String>();
	private ServletContext ctx;
	private BaseView top;
	private boolean pageDirect;
	private IPropertyProvider pp;
	
	protected HttpServletRequest req;
	protected HttpServletResponse resp;
	
	public static String resolveLocalPath(ServletContext ctx, String fileName) throws IOException {
		String path = ctx.getRealPath("");
		File file = new File(path + fileName);
		return file.getAbsolutePath();
	}
	
	public static BaseViewCtx lookup(HttpSession session){
		if (session != null){
			return (BaseViewCtx) session.getAttribute(BaseViewCtx.class.getName());	
		} else {
			return null;
		}
	}

	public static BaseViewCtx bind(IViewCxtFactory factory, IPropertyProvider pp){
		BaseViewCtx ctx = factory.createInstance();
		ctx.pp = pp;
		return ctx;
	}

	public static BaseViewCtx bind(HttpSession session, IViewCxtFactory factory, IPropertyProvider pp){
		BaseViewCtx ctx = lookup(session);
		if (ctx == null){
			ctx = bind(factory, pp);
			if (session != null){
				session.setAttribute(BaseViewCtx.class.getName(), ctx);
			}
		}     
		return ctx;
	}
	
	public IPropertyProvider getConfigProvider(){
		return pp;
	}
	
	public void beginRequest(ServletContext ctx, HttpServletRequest req, HttpServletResponse resp){
		this.ctx = ctx;
		this.req = req;
		this.resp = resp;
		
		pageDirect = false;
	}
	
	public void endRequest(){
		resp = null;
		req = null;
		ctx = null;
	} 
	
	public void addHeader(String name, String value){
		resp.addHeader(name, value);
	}
	
	public void addDateHeader(String name, long date){
		resp.addDateHeader(name, date);
	}
	
	public String getUid(){
		return req.getSession().getId();
	}       
		
	public ILog log(){
		return log;
	}
	   
	public static void sendRedirect(HttpServletResponse resp, int statusCode, String url){
		resp.setStatus(statusCode);
		resp.setHeader( "Location", url);
	}
	
	public void sendRedirect(int statusCode, String url){
		pageDirect = true;		
		sendRedirect(resp, statusCode, url);
	}
	
	public Writer getOutputWriterDirect(String mimeType, String [] headerNames, String [] headerValues) throws IOException {
		pageDirect = true;
		    
		for (int i=0; i < headerNames.length; i++){
			resp.setHeader(headerNames[i], headerValues[i]);
		}
		
		resp.setContentType(mimeType);
		return resp.getWriter();
	}
	
	public String getLocalFileContent(String fileName) throws IOException {
		String path = ctx.getRealPath("");
		File file = new File(path + fileName);
		
		byte [] buf = new byte[(int) file.length()];
		
		FileInputStream fis = new FileInputStream(file);
		try {
			fis.read(buf);
		} finally {
			fis.close();
		}
		
		return new String(buf, "UTF-8");
	}
	
	protected void setTop(BaseView top){
		this.top = top;
	}
	
	public BaseView getTop(){
		return top;
	}
	
	private static boolean dispatchViewAction(BaseView top, int actionId, IPropertyProvider provider) {
		if  (top == null){
			return false;
		} 
		return top.dispatchAction(actionId, provider);
	}
	
	private static void prepareView(BaseView top, IActionDispatchEncoder dispatcher){
		if  (top != null){
			top.prepare(dispatcher);
		}  
	}

	private static void renderView(BaseView top, IActionDispatchEncoder dispatcher, Writer out){
		try {
			if  (top != null){
				top.render(dispatcher, out);
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	public String getCookieValue(String name){
		if (req.getCookies() != null){
			for (Cookie cookie : req.getCookies()){
				if (name.equals(cookie.getName())){
					return URLDecoder.decode(cookie.getValue());  
				}    
			}
		}
		return null;
	}
	
	public String getParameter(String name){
		return req.getParameter(name);
	}
	
	public boolean hasLastError(){
		return getLastError() != null;
	}
	
	public Exception getLastError(){
		return lastError;
	}
	
	public String formatLastError(){
		if (lastError != null){
			try {
				return formatException(lastError);
			} finally {
				lastError = null;
			}
		}    
		return "";
	}
	
	public boolean hasMessages(){
		return messages.size() != 0;
	}
	
	public void pushMessage(String msg){
		messages.add(msg);
	} 
	
	public String formatMessages(){
		try {
			if (messages.size() !=0 ){
				if (messages.size() == 1 ){
					return HtmlUtil.escapeHTML(messages.get(0));
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("<ul>");
					for (int i=0; i < messages.size(); i++){
						sb.append("<li>" + HtmlUtil.escapeHTML(messages.get(i)) + "</li>");
					}
					sb.append("</ul>");
				
					return sb.toString();
				}
			} else {
				return "";
			}
		} finally {
			messages.clear();
		}
	}
	
	public String formatException(Throwable e){
		System.err.println("Exception on " + new Date());
		e.printStackTrace(System.err);
		
		String name = e.getClass().getName();
		String message = e.getMessage();
		
		String cause = "";		
		Throwable eCause = e.getCause();
		if (eCause != null){
			cause = formatException(eCause);
		}
		
		if (message != null){
			message = message.replaceAll("[\n]", "<br />");
		}
		if (cause != null){
			cause = cause.replaceAll("[\n]", "<br />");
		}
		    
		return 
			"<p align='left'>" + name + 
			((message == null || message.equals("")) ? "" : ": " + message) + 
			((cause == null || cause.equals(""))? "" : " " + cause)  + "</p>";
	}	
	
	public void transformModel(BaseView top, IActionDispatchEncoder dispatcher, IPropertyProvider provider, int actionId, Writer out){
		try {
			boolean found = dispatchViewAction(top, actionId, provider);
			if (!found){
				
			}
			lastError = null;
		} catch(Exception e){ 
			lastError = e;
		}		
 		
		if (!pageDirect){
			prepareView(top, dispatcher);
			renderView(top, dispatcher, out);
		}
	}
	
	public void transformModel(IActionDispatchEncoder dispatcher, IPropertyProvider provider, int actionId, Writer out){
		transformModel(getTop(), dispatcher, provider, actionId, out);
	}
	
	public void init(AnyDB db) throws SQLException {
		
	}
	
	public void beginDb() {
		try {  
			db = new AnyDB();
			db.open_mysql(connStr, "mysql");
			  
			db.execUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + ";");
			db.execSelect("USE " + dbName + ";");
			
			init(db);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public AnyDB getDb(){
		return db;
	}
	
	public void endDb() {
		try {  
			if (db != null){
				db.close();
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		} finally {
			db = null;
		}
	}
	
}
