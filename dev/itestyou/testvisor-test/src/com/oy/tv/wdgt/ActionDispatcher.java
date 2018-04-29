package com.oy.tv.wdgt;
  
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseView;
import com.oy.shared.hmvc.servlet.ServletActionAdapter;
import com.oy.shared.lw.LinguinePerfWatch;
import com.oy.shared.lw.misc.ITrace;
import com.oy.shared.lw.perf.monitor.TaskExecutionMonitor;
import com.oy.shared.lw.perf.monitor.VirtualMachineMonitor;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.ILog;
import com.oy.tv.app.IViewCxtFactory;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.identity.AuthTokenDAO;
import com.oy.tv.dao.runtime.QueueProcessorThread;
import com.oy.tv.dao.runtime.ResponseDAO;
import com.oy.tv.dao.runtime.UserUnitResponseDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.tmpl.TemplateEngine;
import com.oy.tv.util.DynamicPropertyManager;
import com.oy.tv.util.HttpUtil;
import com.oy.tv.vocb.view.VocabView;
import com.oy.tv.wdgt.model.UserIdentity;
import com.oy.tv.wdgt.model.UserIdentity.Origin;
import com.oy.tv.wdgt.view.MainView;
import com.oy.tv.wdgt.view.UnitRenderer;

public class ActionDispatcher extends HttpServlet {
	static final long serialVersionUID =0;

	private static VirtualMachineMonitor vmMonitor = new VirtualMachineMonitor();
	private static TaskExecutionMonitor taskMonitor = new TaskExecutionMonitor(
			ActionDispatcher.class, "WEB_APP", 
			"This monitor reports HTTP endpoint."
		);
	
	private static TaskExecutionMonitor mathMonitor = new TaskExecutionMonitor(
			ActionDispatcher.class, "MATH", 
			"This monitor reports /math endpoint."
		);	

	private static TaskExecutionMonitor vocbMonitor = new TaskExecutionMonitor(
			ActionDispatcher.class, "VOCB", 
			"This monitor reports /math endpoint."
		);	
	
	private DynamicPropertyManager dpm = new DynamicPropertyManager();
	private static LinguinePerfWatch watch;
	private static Map<String, NamedContext> contexts;
		
	@Override
	public void init() throws ServletException { 
		try {  
			// templates runtime
			TemplateEngine.init();
					  	
			// properties runtime
			dpm.open(
				new ILog.SystemOutLogProvider(), 
				BaseViewCtx.resolveLocalPath(getServletContext(), "/WEB-INF/config.properties")
			);
			
			// lisp runtime
			YacasEvaluatorEx.init(
				BaseViewCtx.resolveLocalPath(
					getServletContext(), "/WEB-INF/lib/"
				) + "/" + ResourceLocator.getCommonAlgebraLangPackJarName()
			);
			
			// monitoring
			{
			// instantiate
				watch = new LinguinePerfWatch(new ITrace.MockTraceOutImpl(
						ActionDispatcher.class, ITrace.DEBUG));
				
				// configure
				watch.getPerfAgentContext().setCollectDelayMillis(5 * 1000);
				watch.getPerfAgentContext().setSnapshotFileName(dpm.getPropertyValue("lw-log"));		
				
				// start
				watch.start();

				// register monitors
				watch.getPerfAgent().addMonitor(vmMonitor);
				watch.getPerfAgent().addMonitor(taskMonitor);
				watch.getPerfAgent().addMonitor(mathMonitor);
				watch.getPerfAgent().addMonitor(vocbMonitor);
				watch.getPerfAgent().addMonitor(UnitDAO.getMonitor());
				watch.getPerfAgent().addMonitor(AuthTokenDAO.getMonitor());
				watch.getPerfAgent().addMonitor(MainView.getMonitor());
				watch.getPerfAgent().addMonitor(QueueProcessorThread.getMonitor());
				watch.getPerfAgent().addMonitor(ResponseDAO.getMonitor());
			}

			start();
		} catch(Exception e){
			e.printStackTrace();
			throw new ServletException(e);
		}    
	}

	private void use(AnyDB db, String databaseName) throws SQLException {
		db.execUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName + ";");
		db.execSelect("USE " + databaseName + ";");
	}

	private void initDb(AnyDB db) throws SQLException {
		use(db, com.oy.tv.dao.core.AllDAO.NS_DEFAULT);
		new com.oy.tv.dao.core.AllDAO().init(db);
		
		use(db, com.oy.tv.dao.identity.AllDAO.NS_DEFAULT);
		new com.oy.tv.dao.identity.AllDAO().init(db);
		
		use(db, com.oy.tv.dao.runtime.AllDAO.NS_DEFAULT);
		new com.oy.tv.dao.runtime.AllDAO().init(db);
		
		use(db, com.oy.tv.dao.local.AllDAO.NS_DEFAULT);
		new com.oy.tv.dao.local.AllDAO().init(db);
	}
	
	private void start() throws SQLException {
		ViewCtx ctx = new ViewCtx(dpm); 
		ctx.beginDb();
		try {
			initDb(ctx.getDb());
		} finally {
			ctx.endDb();
		}  
			
		// enable data caching
		UnitDAO.CAN_CACHE = true;
		AuthTokenDAO.CAN_CACHE = true;
		
		// add all contexts
		final String ctxPath = "/test/";
		contexts = new HashMap<String, NamedContext>();
		
		MathWidgetDispatcher mwd = new MathWidgetDispatcher();
		contexts.put(ctxPath + mwd.getNs(), mwd);
		
		VocabularyWidgetDispatcher vwd = new VocabularyWidgetDispatcher();
		contexts.put(ctxPath + vwd.getNs(), vwd);

		UserUnitResponseDAO.start(ctx);		
	}

	@Override
	public void destroy() { 
		stop();
		
		try {
			// unregister monitors
			watch.getPerfAgent().removeMonitor(ResponseDAO.getMonitor());
			watch.getPerfAgent().removeMonitor(QueueProcessorThread.getMonitor());
			watch.getPerfAgent().removeMonitor(MainView.getMonitor());
			watch.getPerfAgent().removeMonitor(AuthTokenDAO.getMonitor());
			watch.getPerfAgent().removeMonitor(UnitDAO.getMonitor());
			watch.getPerfAgent().removeMonitor(vocbMonitor);
			watch.getPerfAgent().removeMonitor(mathMonitor);
			watch.getPerfAgent().removeMonitor(taskMonitor);
			watch.getPerfAgent().removeMonitor(vmMonitor);

			watch.stop();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		dpm.close();
	}  
	  
	private void stop(){ 
		UserUnitResponseDAO.stop();
	}

	class ViewCtx extends BaseViewCtx {
		public ViewCtx(IPropertyProvider ipp){
			this(ipp, null, null);
		}
		
		public ViewCtx(IPropertyProvider ipp, UserIdentity ui, NamedContext ctx){
			dbName = ipp.getPropertyValue(ResourceLocator.DB_NAME_PROPERTY_NAME);
			connStr = ipp.getPropertyValue(ResourceLocator.CONN_STR_PROPERTY_NAME);
			if (ctx != null){
				setTop(ctx.newRootView(this, ui, ipp));
			}
		}
	}
	
	private IViewCxtFactory createFactory(HttpServletRequest req, HttpServletResponse resp, 
			final NamedContext ctx) {
		
		final UserIdentity _ui;
		{
  		UserIdentity ui;  
  		try {
  			ui = getIdentity(req);
  		} catch (Throwable t){
  			t.printStackTrace();
  			ui = new UserIdentity();
  		}
  		ui.clientAddress = HttpUtil.getRemoteAddr(req); 		
  		ui.clientAgent = req.getHeader("User-Agent");
  		_ui = ui;
		}
		  
		class Factory implements IViewCxtFactory {
			public BaseViewCtx createInstance(){
				return new ViewCtx(dpm, _ui, ctx);
			}			
		};
		
		return new Factory();
	}
	
	abstract class NamedContext {
		public abstract String getNs();
		public abstract BaseView newRootView(BaseViewCtx ctx, UserIdentity ui, IPropertyProvider ipp);
		public abstract TaskExecutionMonitor getMonitor();
	}
	
	class MathWidgetDispatcher extends NamedContext {
		public String getNs(){
			return "wdgt";
		}
		public TaskExecutionMonitor getMonitor(){
			return mathMonitor;
		}
		public BaseView newRootView(BaseViewCtx ctx, UserIdentity ui, IPropertyProvider ipp) {
			return new MainView(ctx, ui, ipp);
		}
	}
	
	class VocabularyWidgetDispatcher extends NamedContext{
		public String getNs(){
			return "vocb";
		}
		public TaskExecutionMonitor getMonitor(){
			return vocbMonitor;
		}
		public BaseView newRootView(BaseViewCtx ctx, UserIdentity ui, IPropertyProvider ipp) {
			return new VocabView(ctx, ui, ipp);
		}
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		taskMonitor.incStarted();

		req.setCharacterEncoding("UTF-8");
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		
		String active = dpm.getPropertyValue("active");
		if (!"true".equals(active)){ 
			UnitRenderer.renderMessage(resp.getWriter(),
					"We are updating our system, please comeback later.");
		} else {
			NamedContext nc = contexts.get(req.getRequestURI());
			if (nc == null){
				resp.getWriter().write("Unknown context path.");
			} else {
  			ViewCtx ctx = (ViewCtx) BaseViewCtx.bind(createFactory(req, resp, nc), dpm);
  			try {
  				ctx.beginRequest(getServletContext(), req, resp);
  				nc.getMonitor().incStarted();
  				try{        
  					ctx.beginDb();
  					try {				
  						ServletActionAdapter sac = new ServletActionAdapter(req);
  						ctx.transformModel(
  							ctx.getTop(), sac, sac, sac.getActionId(), resp.getWriter()
  						);						 
  						nc.getMonitor().incCompleted();
  						taskMonitor.incCompleted();
  					} finally {
  						ctx.endDb();
  					}  
  				} finally {
  					ctx.endRequest();
  				}      
  			} catch (Throwable t){ 
  				nc.getMonitor().incFailed();
					taskMonitor.incFailed();

					System.err.println("Exception on " + new Date());
  				t.printStackTrace(System.err);  
  				UnitRenderer.renderMessage( 
  					resp.getWriter(), 
  					"There was an error of some kind on our server. Please give us a moment to fix it."
  				);    
  			}
			}
		}
	}
	
	public final static String COOKIE_LANGUAGE_PREFERENCE = "lang";
	public final static String SESSION_COOKIE_NAME_APP = "app_session";
	public final static String SESSION_COOKIE_NAME_WEB = "web_session";
	public final static String SESSION_COOKIE_NAME = "MEDIA_WIKI_session";
	public final static String SESSION_BASE_FOLDER = "/var/lib/php/session"; 
	public final static String SESSION_BASE_NAME = "sess_";

	private UserIdentity getIdentity(HttpServletRequest req) throws IOException, SQLException {	
		ViewCtx ctx = new ViewCtx(dpm); 
		ctx.beginDb();
		try {
			return getIdentity(req, ctx.getDb());
		} finally {
			ctx.endDb();
		}	
	}	
	
	private UserIdentity getIdentity(HttpServletRequest req, AnyDB db) 
			throws IOException, SQLException {
		
		// get session id from parameter or cookie
		Origin origin = Origin.WEB;
		String session = req.getParameter(SESSION_COOKIE_NAME_APP);
		if (session == null || session.length() == 0){
			if (req.getCookies() != null){
  			for (Cookie cookie : req.getCookies()){
  				if (SESSION_COOKIE_NAME_WEB.equals(cookie.getName())){
  					session = URLDecoder.decode(cookie.getValue());  
  					break;
  				}    
  			}
			}
		} else {  
			origin = Origin.APP;
		}

		// validate
		UserIdentity ui = null;
		if (session != null && session.length() != 0){
			ui = getByAuthToken(session, db, origin);  
		}

		// default
		if (ui == null){
			ui = new UserIdentity();
			ui.sessionId = session;
			ui.origin = origin;
			ui.sessionExpired = true;
		}

		return ui;
	}

	private UserIdentity getByAuthToken(String token, AnyDB db, Origin origin) throws SQLException {
		AuthTokenDAO.CustomerInfo ci = AuthTokenDAO.getCustomerIdFor(db, token);
		if (ci.getUserId() != null){
			UserIdentity ui = new UserIdentity();
			ui.userId = ci.getUserId();
			ui.sessionId = token;  
			ui.name = "App User";
			ui.isPro = ci.isPro();
			ui.origin = origin;		
			return ui;			
		}		
		return null;
	}   
		
	public static String loadFileIfExists(String fn) throws IOException {
		try {
  		File file = new File(fn);
  		if (file.exists()){
  			BufferedReader in = new BufferedReader(new FileReader(file));		
  	    StringBuffer sb = new StringBuffer();
  	    while(true){
  	    	String line = in.readLine();
  	    	if (line == null) break;
  	    	sb.append(line + "\n");
  	    }
  	    return sb.toString();
  		}
		} catch (Exception e){
			e.printStackTrace();
		}
		return "";
	}
	
	public static void renderMathAdMobile(Writer out) throws IOException {
		out.write(loadFileIfExists("/oy/testvisor/rtl/generic/public_html/ads/mob.math.txt"));
	}
	
	public static void renderVocbAdMobile(Writer out) throws IOException {
		out.write(loadFileIfExists("/oy/testvisor/rtl/generic/public_html/ads/mob.vocb.txt"));
	}

}