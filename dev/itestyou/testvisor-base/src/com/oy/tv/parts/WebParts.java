package com.oy.tv.parts; 

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;

public class WebParts {
		
	public final static String STYLE_SELECTED_ITEM_COLOR = "#99FF00";
	
	public final static String STYLE_INACTIVE_VIEW_TAB_COLOR = "#E0FFB3";	
	
	public final static String STYLE_USER_BUTTON_COLOR = "#F0F0F0";
	public final static String STYLE_NORMAL_BUTTON_COLOR = "#F0F0F0";
	 
	public final static String STYLE_MENU_BUTTON_COLOR = "#FFDDAA";
	
	public final static String STYLE_BAND_FOOTER_COLOR = "#99CCFF";
	public final static String STYLE_BAND_HEADER_COLOR = "#FFCC99";
	
	public final static String STYLE_DATA_HEADER_COLOR = "#CCCCCC";
	
	public static final String DEVICE_WIDTH  = "100%";
	public static final String DEVICE_HEIGHT = "400";
	public static final String DETAILS_HEIGHT = "350";

	
	public static void beginDataTable(Writer out, String size) throws IOException{
		beginDataTable(out, size, "");
	}
	 
	public static void beginDataTable(Writer out, String size, String ex) throws IOException{	 
		String style = "border-collapse: collapse; border-style: solid; border-color: #" + STYLE_DATA_HEADER_COLOR + "; font-family:Arial; font-size:8pt;";				 
		out.write("<table " + ex + " width='" + size + "' border='1' cellpadding='2' cellspacing='0' style='" + style + "'>\n\r");
	}
	
	public static void endDataTable(Writer out)throws IOException{
		out.write("</table>\n\r");
	} 	
	
	
	/** Band is a table with header.
	 * 
	 * @param out
	 * @param size
	 * @param caption
	 * @param options
	 */
	public static void beginBand(Writer out, String size, String caption, String options)throws IOException{
		if (options == null){
			options = "";
		}
		 
		String style = "border-collapse: collapse; border-style: solid; border-color: #99CCFF; font-family:Arial; font-size:8pt;";
		String styleInner = "border-collapse: collapse; border-style: solid; border-color: #888888; font-family:Arial; font-size:8pt;";
				 
		out.write("<table width='" + size + "' border='1' cellpadding='2' cellspacing='0' style='" + style + "' " + options + ">\n\r");
		if(caption != null && !caption.equals("")){
			out.write("<tr><td valign='top' height='1px' align='left' bgcolor='" + STYLE_BAND_HEADER_COLOR + "'><b>" + caption+ "</b></td></tr>\n\r");
		}	
		out.write("<tr><td valign='top'><table width='100%' border='0' cellpadding='4' style='" + styleInner + "'>");
	}  
	
	public static void beginBand(Writer out, String size, String caption)throws IOException{
		beginBand(out, size, caption, "");
	}
	
	public static void endBand(Writer out)throws IOException{
		endBand(out, null);
	}
	
	public static void endBand(Writer out, String footer)throws IOException{
		out.write("</table></td></tr>\n\r");
		if(footer != null && !footer.equals("")){
			out.write("<tr><td align='center' bgcolor='" + STYLE_BAND_HEADER_COLOR + "'>" + footer + "</td></tr>\n\r");
		}
		out.write("</table>\n\r");
	} 
	
	
	public static void beginBand2(Writer out, String size, String caption, String options)throws IOException{
		if (options == null){
			options = "";
		}
		out.write("<table width='" + size + "' border='1' cellpadding='2' cellspacing='0' style='font-family:Arial; font-size:8pt' bordercolor='#bbbbff' " + options + ">\n\r");
		if(caption != null && !caption.equals("")){
			out.write("<tr><td valign='top' height='1px' align='left' bgcolor='" + STYLE_BAND_HEADER_COLOR + "'><b>" + caption+ "</b></td></tr>\n\r");
		}	
	}  
	
	public static void beginBand2(Writer out, String size, String caption)throws IOException{
		beginBand(out, size, caption);
	}
	
	public static void endBand2(Writer out)throws IOException{
		endBand(out, null);
	}
	
	public static void endBand2(Writer out, String footer)throws IOException{
		if(footer != null && !footer.equals("")){
			out.write("<tr><td align='center' bgcolor='" + STYLE_BAND_HEADER_COLOR + "'>" + footer + "</td></tr>\n\r");
		}
		out.write("</table>\n\r");
	} 

	/** Area is a table without header or padding
	 * 
	 * @param out
	 * @param options
	 */
	public static void beginArea(Writer out, String options)throws IOException{		
		if (options == null){
			options = "";
		}	
		out.write("<table width='100%' border='1' cellpadding='4' cellspacing='0' style='border-collapse: collapse; font-family:Arial; font-size:8pt' " + options + ">\n\r");
	}
	
	public static void beginArea(Writer out)throws IOException{		
		beginArea(out, null);	
	}  
	
	public static void endArea(Writer out)throws IOException{
		out.write("</table>\n\r");
	}
	
	/** Panel is a table without header but with padding
	 * 
	 * @param out
	 * @param size
	 * @param cellpading
	 */
	
	public static void beginPanel(Writer out, String size, int cellpading)throws IOException{
		beginPanel(out, size, cellpading, "");
	}
	
	public static void beginPanel(Writer out, String size, int cellpading, String stylex)throws IOException{
		String _cellpadding = "cellpadding='" + cellpading + "'";		
		out.write("<table width='" + size + "' border='0' " + _cellpadding + " cellspacing='0' style='border-collapse: collapse; font-family:Arial; font-size:8pt; " + stylex + "'>\n\r");	
	} 
	
	public static void beginPanel(Writer out, String size, String stylex)throws IOException{
		beginPanel(out, size, 0, stylex);
	}
	
	public static void beginPanel(Writer out, String size)throws IOException{
		beginPanel(out, size, 0);
	}
	
	public static void endPanel(Writer out)throws IOException{
		out.write("</table>\n\r");
	}		
	
	public static String escapeHTMLKeepLineBreaks(String text){
		text = escapeHTML(text);		
		return text.replaceAll("\n", "<br>"); 
	}
	
	public static void beginScrollDiv(Writer out) throws IOException {
		out.write("<div style=\"width:100%;height:" + DETAILS_HEIGHT + ";overflow:auto;\">");
	}
	
	public static void endScrollDiv(Writer out) throws IOException {
		out.write("</div>");
	}
	
	public static void tabCell(IActionDispatchEncoder dispatcher, Writer out, BaseAction action, String caption, boolean selected) throws IOException{
		tabCell(dispatcher, out, action, caption, new String [] {}, new String [] {}, selected);	
	}
	
	public static void tabCell(IActionDispatchEncoder dispatcher, Writer out, BaseAction action, String caption, String [] names, String [] values, boolean selected) throws IOException {		
		if (selected){
			out.write("<td nowrap align='right' width='1px' bgcolor='" + STYLE_SELECTED_ITEM_COLOR + "'><b>&gt;</b>&nbsp;");
			out.write("<b>" + caption + "</b>");
		} else {
			out.write("<td nowrap align='right' width='1px' bgcolor=\"" + STYLE_INACTIVE_VIEW_TAB_COLOR + "\"><b>&gt;</b>&nbsp;");
			out.write(dispatcher.encodeActionDispatch(action, caption, "color: #000000; text-decoration: none;", names, values));
		}
		out.write("&nbsp;</td>");
	}	

	public static String button(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values, String color) throws IOException{
		return button(dispatcher, action, caption, names, values, color, "#A0A0A0");
	}
	
	private static String button(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values, String color, String bcolor) throws IOException{
		return 
			"&nbsp;" + 
			dispatcher.encodeActionDispatch(
				action, 
				"&nbsp;" + caption + "&nbsp;",
				"background-color:" + color + "; color: #000000; text-decoration: none; border: solid 1px " + bcolor + ";",
				names, values					
			) +  
			"&nbsp;"; 
	}
	
	public static String buttonTab(IActionDispatchEncoder dispatcher, BaseAction action, String caption, boolean active) throws IOException{
		if (active){
			return caption;
		} else {
			return dispatcher.encodeActionDispatch(
				action, caption 
			);	
		}
	}
	
	public static String buttonVerb(IActionDispatchEncoder dispatcher, BaseAction action, String caption) throws IOException{
		return buttonVerb(dispatcher, action, caption, new String []{}, new String [] {});
	}
	  
	public static String tabBegin() {
		return "<div align='center' style='margin: 8px; margin-bottom: 4px; border-bottom: 2px solid #404040;'>";
	}  
	
	public static String tabEnd() {
		return "</div>";
	}
  	  
	public static String tab(IActionDispatchEncoder dispatcher, BaseAction action, String caption, boolean active) throws IOException{
		String color = (active) ? "#D0D0D0" : "#F0F0F0"; 
		return button(dispatcher, action, "<b>&nbsp;&nbsp;" + caption + "&nbsp;&nbsp;</b>", new String []{}, new String [] {}, color, "#404040");
	} 
	
	public static String buttonVerb(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values) throws IOException{
		return button(dispatcher, action, caption, names, values, STYLE_USER_BUTTON_COLOR);
	}
	
	public static String button(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values, boolean enabled) throws IOException {
		if (enabled){
			return button(dispatcher, action, caption, names, values, STYLE_NORMAL_BUTTON_COLOR);
		} else {
			return "<span style=\"background-color:#FFE7C1; color: #888888;\"><b>&nbsp;" + caption + "&nbsp;</b></span>&nbsp;&nbsp;";
		}
	}

	public static String styleButton(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values, String buttonColor) throws IOException {
		return button(dispatcher, action, caption, names, values, buttonColor);
	}
	
	public static String menuBegin() throws IOException {
		return "<div align='left' style='width: 100% margin: 8px; margin-top: 2px; padding: 4px; border-bottom: 1px dotted #A0A0A0;'>";
	}
	
	public static String menuEnd() throws IOException {
		return "</div>";
	}
	
	public static String menu(IActionDispatchEncoder dispatcher, BaseAction action, String caption, String [] names, String [] values) throws IOException {
		return button(dispatcher, action, caption, names, values, STYLE_MENU_BUTTON_COLOR);
	}
	
	public static String button(IActionDispatchEncoder dispatcher, BaseAction action, String caption) throws IOException {
		return button(dispatcher, action, caption, new String [] {}, new String [] {}, true);
	} 

	public static String escapeHTML(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(text);
		}		 
	}
	 
	public static String unescapeHTML(String text){
		if (text == null) {
			return "";
		} else {
			return StringEscapeUtils.unescapeHtml(text);
		}
	}
	
	public static String [][] query2array(ResultSet rs) throws Exception {
		List<String []> rows = new ArrayList<String []>();
		int cols = rs.getMetaData().getColumnCount();
		String [] row;
		
		row = new String [cols]; 
		for (int i=0; i < row.length; i++){
			row[i] = rs.getMetaData().getColumnName(1 + i);
		}
		rows.add(row);
		
		while(rs.next()){
			row = new String [cols];
			for (int i=0; i < cols; i++){
				Object obj = rs.getObject(1 + i);
				if (obj == null){
					row[i] = "NULL";
				} else {
					row[i] = obj.toString();
				}
			}
			rows.add(row);
		}
		 
		return (String [][]) rows.toArray(new String [] []{});
	}
	  
	public static void renderDataTable(String [][] rows, int maxCols, Writer out) throws IOException {
		renderDataTable(rows, maxCols, new ICellTextCustomizer.NullCellTextCustomizer(), out);
	}
	
	public static void renderDataTableRows(String [][] rows, int maxCols, ICellTextCustomizer customizer, Writer out) throws IOException {
		String [] row;
		
		row = rows[0];
		out.write("<tr>");
		for (int i=0; i < maxCols; i++){
			out.write("<th>" + row[i] + "</th>");
		}
		out.write("</tr>");
		
		for (int i=1; i < rows.length; i++){
			row = rows[i];		
			customizer.customize(row);
			
			out.write("<tr>");
			for (int j=0; j < maxCols; j++){
				out.write("<td>" + row[j] + "</td>");
			} 
			out.write("</tr>");
		}
	}
	
	public static void renderDataTable(String [][] rows, int maxCols, ICellTextCustomizer customizer, Writer out) throws IOException {
		WebParts.beginDataTable(out, "100%");
		renderDataTableRows(rows, maxCols, customizer, out);
		WebParts.endDataTable(out);
	}
	
	public static void renderHint(String text, Writer out) throws IOException {
		renderHint(text, 350, out);
	}
	
	public static void renderHint(String text, int px, Writer out) throws IOException {
		out.write("<div style='width:" + px + "px; background-color: #C0FFC0; margin: 4px; padding: 8px; float:right; text-align: left;'>");
		out.write("<font color='#008000'><b>TIP:</b></font>&nbsp;&nbsp;&nbsp;");
		out.write(text);
		out.write("</div><div style='clear:both;'></div>");
	}
	
	public static String option(String name,  String value, boolean selected){
		String ex = " ";
		if (selected){
			ex = " selected ";
		}
		return "<option" + ex + "value='" + HtmlUtil.escapeHTML(value) + "'>" + HtmlUtil.escapeHTML(name) + "</option>";
	}

}
