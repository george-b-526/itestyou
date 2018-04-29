package com.oy.tv.ml.chartml.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public abstract class GenericPlot {

	public PlotContext _ctx;
	
	public abstract double y(double x); 
  	
	final public void render(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		XYSeriesCollection dataset;
		{
			XYSeries series = new XYSeries("XYGraph");
	
			for (double i=_ctx.MIN_X; i < _ctx.MAX_X; i += _ctx.STEP){
				series.add(i, y(i));
			}
			
			dataset = new XYSeriesCollection();
			dataset.addSeries(series);
		}
		
		NumberAxis xAxis;
		{
			xAxis= new NumberAxis("X");
			xAxis.setRange(0.01 + _ctx.MIN_X, -0.01 + _ctx.MAX_X);
			xAxis.setPositiveArrowVisible(true);
			xAxis.setTickUnit(new NumberTickUnit(1.00));
		}
		
		NumberAxis yAxis;
		{
			yAxis = new NumberAxis("Y"); 
			yAxis.setRange(0.01 + _ctx.MIN_Y, -0.01 + _ctx.MAX_Y);
			yAxis.setPositiveArrowVisible(true);
			yAxis.setTickUnit(new NumberTickUnit(1.00));
		}
		
		XYPlot plot;
		{  
			XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
			renderer.setSeriesPaint(0, Color.BLACK);
			renderer.setSeriesStroke(0, new BasicStroke(1.0f));
			  
			plot = new XYPlot(dataset, xAxis, yAxis, renderer);
			
			plot.setBackgroundPaint(new Color(255, 255, 255));
			plot.setOrientation(PlotOrientation.VERTICAL);
			
			plot.setDomainCrosshairVisible(true);
			plot.setDomainCrosshairValue(0);
			plot.setDomainCrosshairPaint(Color.BLACK);
			   
			plot.setRangeCrosshairVisible(true);
			plot.setRangeCrosshairValue(0);  
			plot.setRangeCrosshairPaint(Color.BLACK);
		}
		
		JFreeChart chart;
		{
			chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
			chart.setBackgroundPaint(new Color(255, 255, 255));
		}
  		  
		{      
			resp.setContentType("image/png");  
			javax.imageio.ImageIO.setUseCache(false);
			ChartUtilities.writeChartAsPNG(resp.getOutputStream(), chart, 320, 300);
		}       
		
	}
	
}
