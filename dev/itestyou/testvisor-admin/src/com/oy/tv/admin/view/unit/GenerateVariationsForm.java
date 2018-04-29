package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.model.core.NumberKind;
import com.oy.tv.model.core.VariationGenerator;
import com.oy.tv.model.core.VariationUniquesnessEvaluator;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.VariationBO;

public class GenerateVariationsForm extends WebView { 
  	
	BaseAction m_Load = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				StringBuffer sb = new StringBuffer();
				
				PagedKeyList pkl = VariationDAO.getAllPaged(getCtx().getDb(), unit, Integer.MAX_VALUE, 0);
				for (int i=0; i < pkl.ids.size(); i++){				
					int id = pkl.ids.get(i);
					VariationBO var = VariationDAO.loadVariation(getCtx().getDb(), id);
					if (i != 0){
						sb.append("\n");
					}
					sb.append(var.getValues());
				}    
				variations = sb.toString();
				
				getCtx().pushMessage("Loaded " + pkl.ids.size() + " items.");
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	};
	
	BaseAction m_Generate = new BaseAction (){
		String inInput;
		public void execute(IPropertyProvider provider){
			input = inInput;
			  
			VariationGenerator vg = new VariationGenerator();
			List<String> all = vg.generate(input);

			StringBuffer sb = new StringBuffer();
			
			for (int i=0; i < all.size(); i++){				
				if (i != 0){
					sb.append("\n");
				}
				sb.append(all.get(i));
			}    
			variations = sb.toString();
			
			getCtx().pushMessage("Generated " + all.size() + " variations.");
		}
	};	
	
	class AnalyzeAction extends BaseAction {
		String inVariations;
		
		boolean inMustComplex;
		boolean inMustFraction;
		boolean inMustMinus;
		boolean inMustSqrt;
		
		boolean inMustNotComplex;
		boolean inMustNotFraction;
		boolean inMustNotMinus;
		boolean inMustNotSqrt;
		  
		public void execute(IPropertyProvider provider){
			variations = inVariations;
			String [] all = inVariations.split("\n");
			
			VariationUniquesnessEvaluator vue = new VariationUniquesnessEvaluator();
			
			List<String> undesired = new ArrayList<String>();
			List<String> localDuplicates = new ArrayList<String>();
			List<String> globalDuplicates = new ArrayList<String>();
			List<String> unique = new ArrayList<String>();

			NumberKind must = new NumberKind();
			must.hasComplex = inMustComplex;
			must.hasMinus = inMustMinus;
			must.hasFraction = inMustFraction;
			must.hasSqrt = inMustSqrt;
			
			NumberKind mustnot = new NumberKind();
			mustnot.hasComplex = inMustNotComplex;
			mustnot.hasMinus = inMustNotMinus;
			mustnot.hasFraction = inMustNotFraction;
			mustnot.hasSqrt = inMustNotSqrt;
			
			vue.evaluate(
				unit, must, mustnot, 
				Arrays.asList(all), 
				undesired, localDuplicates, globalDuplicates, unique
			);
			
			uniques = VariationGenerator.toString(unique);
			ldups = VariationGenerator.toString(localDuplicates);
			gdups = VariationGenerator.toString(globalDuplicates);
			undesireds = VariationGenerator.toString(undesired);
			  
			getCtx().pushMessage(
				"Analyzed " + all.length + " items. Classified as " + 
				unique.size() + " unique, " +
				localDuplicates.size() + " local and " +
				globalDuplicates.size() + " global duplicates, " +
				undesired.size() + " undesired items."
			);  
		}
	};
	AnalyzeAction m_Analyze = new AnalyzeAction();
	
	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.pushView(parent.editunit);
		}   
	};
	  
	BaseAction m_Add = new BaseAction (){
		String inUniques;
		public void execute(IPropertyProvider provider){
			try  {
				String [] all = inUniques.split("\n");
				for (int i=0; i < all.length; i++){
					VariationDAO.addVariation(getCtx().getDb(), unit, all[i].trim());
				}  
				getCtx().pushMessage("Added " + all.length + " items.");
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	};   	
	
	private HomePageView parent;
	private UnitBO unit;
	
	private String input = "";        
	private String variations = "";
	private String uniques = "";
	private String ldups = "";
	private String gdups = ""; 
	private String undesireds = "";
	
	public GenerateVariationsForm(HomePageView parent){
		super(parent.getCtx());
		  
		this.parent = parent;
	} 
	  
	public void setFocus(UnitBO unit){
		this.unit = unit;
	}
	   
	public void renderBottom(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Add.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
	 			         
		out.write("<tr>");    
		out.write("<td align='center'>");
     		             
		WebParts.beginPanel(out, "100%");
		{  			
			out.write("<tr><td width='15%' align='right'>Unique</td>");			
			out.write("<td><textarea rows='5' cols='80' style='width: 90%;' name='inUniques'>" + HtmlUtil.escapeHTML(uniques) + "</textarea></td></tr>");
			
			out.write("<tr><td width='15%' align='right'>Local Duplicates (identical answer and decoy in one unit instance)</td>");			
			out.write("<td><textarea rows='5' cols='80' style='width: 90%;'>" + HtmlUtil.escapeHTML(ldups) + "</textarea></td></tr>");
			
			out.write("<tr><td width='15%' align='right'>Global Duplicates (different values that generate identical unit instances)</td>");			
			out.write("<td><textarea rows='5' cols='80' style='width: 90%;'>" + HtmlUtil.escapeHTML(gdups) + "</textarea></td></tr>");
			
			out.write("<tr><td width='15%' align='right'>Undesired (values that do not pass 'must have' or 'must not have' you requested)</td>");			  
			out.write("<td><textarea rows='5' cols='80' style='width: 90%;'>" + HtmlUtil.escapeHTML(undesireds) + "</textarea></td></tr>");			
		}   
		WebParts.endPanel(out);
		     
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);
		
		out.write("<hr /><p>");  
		out.write("<input type='submit' value='3) Add Unique Variations To Database' />");
		
		String act = WebParts.button(dispatcher, m_Done, "Done");
		out.write(act);		
		  
		out.write("</p>");
		
		out.write("</form>");
	}
	
	private String checked(boolean value){
		return "style='width: 32px;' value='true'" + (value ? " checked" : "");
	}
	
	public void renderMid(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Analyze.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
	 			         
		out.write("<tr>");    
		out.write("<td align='center'>");
     		               
		WebParts.beginPanel(out, "100%");
		{  			
			out.write("<tr><td width='1%' nowrap align='right'>Variations</td>");			
			out.write("<td colspan='2'><textarea rows='5' cols='80' style='width: 90%;' name='inVariations'>" + HtmlUtil.escapeHTML(variations) + "</textarea></td></tr>");
			  
			out.write("<tr><td width='1%' nowrap align='right'>&nbsp;</td>");
			out.write("<td align='right'>Must have</td>");
			out.write("<td align='left'>");
			out.write("<input type='checkbox' name='inMustMinus' " + checked(m_Analyze.inMustMinus) + "> Negative Numbers");
			out.write("<input type='checkbox' name='inMustFraction' " + checked(m_Analyze.inMustFraction) + "> Fractions ");
			out.write("<input type='checkbox' name='inMustComplex' " + checked(m_Analyze.inMustComplex) + "> Complex Numbers");
			out.write("<input type='checkbox' name='inMustSqrt' " + checked(m_Analyze.inMustSqrt) + "> Square Root");
			out.write("</td></tr>");
			      
			out.write("<tr><td width='1%' nowrap align='right'>&nbsp;</td>");
			out.write("<td align='right'>Must not have</td>");
			out.write("<td align='left'>");
			out.write("<input type='checkbox' name='inMustNotMinus' " + checked(m_Analyze.inMustNotMinus) + "> Negative Numbers");
			out.write("<input type='checkbox' name='inMustNotFraction' " + checked(m_Analyze.inMustNotFraction) + "> Fractions ");
			out.write("<input type='checkbox' name='inMustNotComplex' " + checked(m_Analyze.inMustNotComplex) + "> Complex Numbers");
			out.write("<input type='checkbox' name='inMustNotSqrt' " + checked(m_Analyze.inMustNotSqrt) + "> Square Root");
			out.write("</td></tr>");
		}    
		WebParts.endPanel(out);
		     
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);
		
		out.write("<p>");  
		out.write("<input type='submit' value='2) Analyze Variations And Classify Them Into Form Below' />");
		
		out.write("</p>");
		
		out.write("</form>");
	}
  	
	public void renderTop(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Generate.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
	 			         
		out.write("<tr>");    
		out.write("<td align='center'>");
     		             
		WebParts.beginPanel(out, "100%");
		{  			
  		out.write("<tr><td width='1%' nowrap align='right'>Input</td>");			
			out.write("<td><input style='width: 90%;' name='inInput' value='" + HtmlUtil.escapeHTML(input) + "' /><br />" + 
					"For example: {1..5,2..34:2}. For this input we will generate all value permutations " + 
					"where 1..5 means all values from 1 to 5 inclusive; 1..34:2 - means all value from 2 to 32 " + 
					"with step of 2, total of 5x16 = 80 combinations. The number of variable values must match " + 
					"the number of variable bindings defined in the XML.</td></tr>");			
		}     
		WebParts.endPanel(out);
		     
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);
		  
		out.write("<p>");  
		out.write("<input type='submit' value='1) Generate All Possible Variations Into Form Below' />");

		String act = WebParts.button(dispatcher, m_Load, "Load Current Variations From Database");
		out.write(act);

		
		out.write("</p>");
		
		out.write("</form>");
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>Generate Variations for Unit " + unit.getId() + "</p><hr />");		
	
		renderTop(dispatcher, out);
		out.write("<hr />");
		renderMid(dispatcher, out);
		out.write("<hr />");
		renderBottom(dispatcher, out);
 	}
}
