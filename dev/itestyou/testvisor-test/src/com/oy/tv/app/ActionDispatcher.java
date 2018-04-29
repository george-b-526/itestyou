package com.oy.tv.app;
  
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.servlet.ServletActionAdapter;
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
		
		resp.setContentType("text/html");
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		
		String active = dpm.getPropertyValue("active");
		if (!"true".equals(active)){   
			BaseViewCtx.sendRedirect(resp, 301, ResourceLocator.getInactiveHref());
		} else {
			ServletActionAdapter sac = new ServletActionAdapter(req);
			
			ViewCtx ctx = (ViewCtx) BaseViewCtx.bind(factory, dpm);
			try {
				ctx.beginRequest(getServletContext(), req, resp);
				try{
					ctx.beginDb();
					try {						
						ctx.transformModel(ctx.getTop(), sac, sac, sac.getActionId(), resp.getWriter());
					} finally {
						ctx.endDb();
					}  
				} finally {
					ctx.endRequest();
				}        
			} catch (Throwable t){ 
				t.printStackTrace();  
				resp.getWriter().write(
					"There was an error of some kind on our server. Please give us a moment to fix it."
				);
			}
		}
	}
						
}