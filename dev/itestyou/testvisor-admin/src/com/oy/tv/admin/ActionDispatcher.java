package com.oy.tv.admin;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.servlet.ServletActionAdapter;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.ILog;
import com.oy.tv.app.IViewCxtFactory;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.util.DynamicPropertyManager;

public class ActionDispatcher extends HttpServlet {
 	
	static final long serialVersionUID =0;
	
	private DynamicPropertyManager dpm = new DynamicPropertyManager();
	
	@Override
	public void init() throws ServletException { 
		try {
			dpm.open(
				new ILog.SystemOutLogProvider(), 
				BaseViewCtx.resolveLocalPath(getServletContext(), "/WEB-INF/config.properties")
			);
			
			YacasEvaluatorEx.init(
				BaseViewCtx.resolveLocalPath(
					getServletContext(), "/WEB-INF/lib/"
				) + "/" + ResourceLocator.getCommonAlgebraLangPackJarName()
			);
		} catch(Exception e){
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() { 
		dpm.close();  
	}  
	
	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
 	
		IViewCxtFactory factory = new IViewCxtFactory () {
			public BaseViewCtx createInstance(){
				return new ViewCtx(dpm);
			}			
		};
		
		req.setCharacterEncoding("UTF-8");
		
		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		 
		HttpSession session = req.getSession();
		synchronized(session){
  		ViewCtx ctx = (ViewCtx) BaseViewCtx.bind(session, factory, dpm);
  		try {
  			ctx.beginRequest(getServletContext(), req, resp);
  			try{    
  				ctx.beginDb();
  				try {
  					ServletActionAdapter sac = new ServletActionAdapter(req);					
  					ctx.transformModel(sac, sac, sac.getActionId(), resp.getWriter());
  				} finally {
  					ctx.endDb();
  				}
  			} finally {
  				ctx.endRequest();
  			}
  		} catch (Throwable t){ 
  			resp.getWriter().write(ctx.formatException(t));
  		}
  		if (ctx.hasLastError()){
  			resp.getWriter().write(ctx.formatException(ctx.getLastError()));
  		}
		}
	} 
		
}