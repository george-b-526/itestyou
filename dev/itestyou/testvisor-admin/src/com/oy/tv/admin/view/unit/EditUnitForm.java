package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;

public class EditUnitForm extends WebView {

	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.popView();
		}
	};
	  
	BaseAction m_Preview = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.preview.setFocus(unit, TranslationDAO.LANG_EN);
			pushView(parent.preview);
		}
	};
	
	BaseAction m_Variations = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.varlist.setFocus(unit);
			pushView(parent.varlist);
		}  
	}; 
	
	BaseAction m_Generate = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.genvar.setFocus(unit);
			pushView(parent.genvar);
		}
	}; 
	
	BaseAction m_DeleteVariations = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				VariationDAO.deleteAllVariations(getCtx().getDb(), unit);
			} catch (Exception e){  
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to delete object.");
			}
		}
	};
	
	BaseAction m_Delete = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				UnitDAO.deleteUnit(getCtx().getDb(), unit);
				getCtx().pushMessage("Deleted.");
				parent.pushView(parent.summary);
			} catch (Exception e){  
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to delete object.");
			}
		}
	};
	
	BaseAction m_Activate = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				UnitDAO.updateUnit(getCtx().getDb(), unit, EObjectState.ACTIVE);
				getCtx().pushMessage("Activated.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to activate object.");
			}
		}
	};
	
	BaseAction m_Deactivate = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				UnitDAO.updateUnit(getCtx().getDb(), unit, EObjectState.INACTIVE);
				getCtx().pushMessage("Deactivated.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to deactivate object.");
			}
		}
	};
	
	BaseAction m_Save = new BaseAction (){
		int inGrade;
		String inXml;
		String inNotes;
		String inTitle;
		String inDesc;
		public void execute(IPropertyProvider provider){
			try {
				inXml = getCtx().getParameter("inXml");
				
				if (inTitle == null || inTitle.trim().length() == 0){
					inTitle = "Math Worksheets Unit #" + unit.getId();
				}
				UnitDAO.updateUnit(getDb(), unit, 
						inXml, inNotes, inTitle, inDesc, inGrade, unit.getType(), unit.getState());
				       
				getCtx().pushMessage("Saved.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());  
				throw new RuntimeException("Failed to create new object.");
			}  
		}      
	};

	BaseAction m_Publish = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				final String live_str = "jdbc:mysql://208.109.119.195:3306/mysql?user=admin&password=admin&useUnicode=true&characterEncoding=utf8";
				AnyDB live = new AnyDB();
				live.open_mysql(live_str, AllDAO.NS_DEFAULT);
				try {
					live.trxBegin();
					
					UserBO owner = new UserBO();
					owner.setId(1);

					UnitDAO.deepCopy(getDb(), live, owner, unit);					

					live.trxEnd();
				} finally {
					live.close();
				}				
				
				getCtx().pushMessage("Published.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to publish object.");
			}
		}
	};
	
	private HomePageView parent;
	private UnitBO unit;
	
	public EditUnitForm(HomePageView parent){
		super(parent.getCtx());
		  
		this.parent = parent;
	} 
	  
	public void setFocus(UnitBO unit){
		this.unit = unit;
	}
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		int varCount;
		try {
			varCount = VariationDAO.countVariationsFor(getCtx().getDb(), unit);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
		
		out.write("<p>Edit Unit: Id=" + unit.getId() + ", OwnerId=" + unit.getOwnerId() +"</p><hr />");
		  
		out.write("<form action='view' method='POST' style='margin: 0px;'>"); 
		out.write("<input type='hidden' name='action_id' value='" + m_Save.hashCode() + "'>");

		WebParts.beginPanel(out, "100%");
	 			         
		out.write("<tr>");    
		out.write("<td align='center'>");
     		             
		WebParts.beginPanel(out, "100%");
		{  						
			String act1 = WebParts.button(dispatcher, m_Variations, "Browse");
			String act2 = WebParts.button(dispatcher, m_Generate, "Bulk Generate & Analyze");
			String act3 = WebParts.button(dispatcher, m_DeleteVariations, "Delete All Unit Variations");
			out.write("<tr><td valign='top' width='1%' nowrap align='right'>Variations</td>");
			out.write("<td valign='top'>" + varCount + act2 + act1 + "<span style='float: right; background-color: red; padding: 8px;'>" + act3 + "</span></tr>");    
			
			out.write("<tr><td width='1%' nowrap align='right'>Grade</td>");
			out.write("<td><input name='inGrade' value='" + HtmlUtil.escapeHTML("" + unit.getGrade()) + "'></td></tr>");

			out.write("<tr><td width='1%' nowrap align='right'>Title</td>");
			out.write("<td><input name='inTitle' style='background-color: #F0F0F0; width: 90%;' readonly value='" + HtmlUtil.escapeHTML(unit.getTitle()) + "'></td></tr>");

			out.write("<tr><td width='1%' nowrap align='right'>Desc</td>");
			out.write("<td><input name='inDesc' style='width: 90%;' value='" + HtmlUtil.escapeHTML(unit.getDesc()) + "'></td></tr>");
			
			out.write("<tr><td width='1%' nowrap align='right'>Notes</td>");
			out.write("<td><input style='width: 90%;' name='inNotes' value='" + HtmlUtil.escapeHTML(unit.getNotes()) + "'><br />For example: CA.MATH.8.47</td></tr>");
  			
			String xml = unit.getXml();  
			out.write("<tr><td width='1%' nowrap align='right'>XML</td>");			
			out.write("<td><textarea rows='20' cols='80' style='width: 90%;' name='inXml'>" + HtmlUtil.escapeHTML(xml) + "</textarea></td></tr>");
  	 		  
			out.write("<tr><td width='1%' nowrap align='right'>State</td>");
			out.write("<td>" + unit.getState().value() + "</tr>");  
			
			out.write("<tr><td width='1%' nowrap align='right'>Help</td>");
			out.write("<td align='left'>" +
				"<u>Calculations:</u> " + 
				"${c} - evaluate variable c; " + 
				"Hold((a*x+b)) - captures expression without evaluating or simplifyng it; " +
				"N(f) - converts fraction f into decimal number: N(1/2) == 0.5; " +
				"Eval(c) - evaluates the expression: Eval(Hold(2+3)) == 2+3, but Eval(2+3) == 5; " +
				"Round(X) - rounds to the nearws integer; " +
				"FormatD(X) - format to two decimal places: FormatD(2.23456) == 2.23; " +
				"FormatT(X) - formats english name for tens: FormatT(9) == 'ninety'; " +
				"FormatE(X) - formats english name for ones: FormatE(9) == 'nine'; " +
				"Simplify(X) - simplifies symbolic expression: Simplify(3x+2x) == 5x; " +
				"HtmlImg(url) - insert image pointed by url; " +
				"ConcatStrings(String(a), \"+\")) - converts variable a to string and adds '+'; " +
				"RepeatNTimes(text, n) - outputs text n times: RepeatNTimes(\"a\", 3) == aaa; "  +
				"RepeatNTimes(text1, n, text2) - outputs text1 n times and append text2: RepeatNTimes(\"a\", 3, \"b\") == aaab; "  +
				"Mod(x,y) - compute remainder from division y/x: Mod(8,5) == 3; " +
				"Floor(x) - round x down to integer: Floor(2.123) == 2; " + 
				"<u>Rendering:</u> " +
				"b, i and some other HTML tags are allowed; " + 
				"&lt;html&gt;...&lt;/html&lt; - do not HTML-escape anything between the tags; " + 
				"&lt;draw&gt;...&lt;/draw&lt; - Eval() text between the tags, convert resulting math expression to Latex and render; " +
				"&lt;latex&gt;...&lt;/latex&lt; - render text between the tags as Latex expression; latex tag can have draw tag in it; " +
			  "</td>");
		}  
		WebParts.endPanel(out);
		     
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);
		
		
		String act1 = WebParts.button(dispatcher, m_Preview, "Preview");
		String act2 = WebParts.button(dispatcher, m_Delete, "Delete This Unit");
		String act3 = WebParts.button(dispatcher, m_Done, "Done");
			       
		String act4 = "";
		if (unit.getState().equals(EObjectState.ACTIVE)){  
			act4 = WebParts.button(dispatcher, m_Deactivate, "Deactivate");
		}
		if (unit.getState().equals(EObjectState.INACTIVE)){  
			act4 = WebParts.button(dispatcher, m_Activate, "Activate");
		}  
		
		String act5 = WebParts.button(dispatcher, m_Publish, "Publish Live");
		  
		out.write("<hr /><p>");

		if (UserDAO.isAdmin(getModel().getPrincipal())){
			out.write("<span style='float: left; background-color: red; padding: 8px;'>" + act2 + "</span>");
		}
		
		if (UserDAO.isAdmin(getModel().getPrincipal())){
			out.write("<span style='float: left; background-color: red; padding: 8px;'>" + act5 + "</span>");
		}
		
		out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		out.write("<input type='submit' value='save' />");
		out.write(act1);

		if (UserDAO.isAdmin(getModel().getPrincipal())){
			out.write(act4);
		}
		
		out.write(act3);
		out.write("</p>");
		
		out.write("</form>");
	}
	
}
