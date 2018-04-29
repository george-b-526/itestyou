package com.oy.tv.ml.chartml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oy.shared.hmvc.impl.BaseView;
import com.oy.shared.hmvc.servlet.ServletActionAdapter;
import com.oy.tv.ml.chartml.impl.GenericPlot;
import com.oy.tv.ml.chartml.impl.PlotContext;

public class QuadraticPlot extends HttpServlet {
 	
	static final long serialVersionUID =0;
	
	@Override
	public void init() throws ServletException { }

	@Override  
	public void destroy() { }  
	
	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			QuadraticPlotView view = new QuadraticPlotView();
			view._ctx = new PlotContext();
			BaseView.restoreObjectState(view, new ServletActionAdapter(req));
			view.render(req, resp);
		} catch (Throwable t){
			t.printStackTrace();
		}  
	}  
						
}

class QuadraticPlotView extends GenericPlot {
	
	double inA;  
	double inB;   
	double inC;
	
	public  double y(double x){
		return inA * x * x  + inB * x + inC;
	}

}