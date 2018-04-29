/*
	Hierarchical Model View Controller (OY-HMVC)
	Copyright (C) 2005-2008 Pavel Simakov
	http://www.softwaresecretweapons.com

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/


package com.oy.shared.hmvc.tblx;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.impl.BaseView;
import com.oy.shared.hmvc.tblx.IRowComparator.RowComparator;
import com.oy.shared.hmvc.util.HtmlUtil;

public class TableView extends BaseView {

	BaseAction m_SortByCol = new BaseAction (){
		int inColIdx;
		public void execute(IPropertyProvider provider){
			ColumnFlag target;
			if (options.group.getEnabled() && options.group.getColIdx() == inColIdx){
				target = options.group;
			} else {
				target = options.sort;
			}
			   			
			if (target.getColIdx() == inColIdx){
				target.setFlag(!target.getFlag());
			}
			target.setColIdx(inColIdx);
			target.setEnabled(true);
		}		
	};   
	
	BaseAction m_GroupByCol = new BaseAction (){
		int inColIdx;
		public void execute(IPropertyProvider provider){
			if (options.group.getColIdx() == inColIdx){
				options.group.setEnabled(!options.group.getEnabled());
			} else {
				options.group.setEnabled(true);	
			}
			options.group.setColIdx(inColIdx);
		}		
	};
  	
	private int cols;
	private int border;
	
	private CssStyle tableStyle = new CssStyle();
	private CssStyle headerStyle = new CssStyle();
	private CssStyle footerStyle = new CssStyle();
	private CssStyle [] columnStyles;
	private CssStyle [] rowStyles;
	
	private ViewOptions options;
	
	private HeaderCell [] headers;
	 
	private Class [] colTypes;
	
	private TableCell [][] cells;
	
	private HeaderCell titleCell;
	private HeaderCell topMenuCell;
	private HeaderCell bottomMenuCell;
	
	public TableView(int cols, CssStyle cellStyle){
		this.cols = cols;
		
		if (cols <= 0){ 
			throw new RuntimeException("At least one column required");
		}
		  
		topMenuCell = new HeaderCell();
		bottomMenuCell = new HeaderCell();
		options = new ViewOptions();
		colTypes = new Class[cols];
		columnStyles = new CssStyle[cols];
		headers = new HeaderCell[cols];
		for (int i=0; i < cols; i++){
			headers[i] = new HeaderCell(); 
			headers[i].style = CssStyle.cloneFrom(cellStyle);
			columnStyles[i] = CssStyle.cloneFrom(cellStyle);
			colTypes[i] = String.class;
		} 
		 
		titleCell = new HeaderCell();
	
		formatDefaultStyles();
	}
	  
	public ViewOptions getViewOptions(){
		return options;
	}
	
	private void sort(){
		if (!(options.sort.getColIdx() >= 0 &&  options.sort.getColIdx() < cols)){
			options.sort.setColIdx(0);
		} 
		if (!(options.group.getColIdx() >= 0 &&  options.group.getColIdx() < cols)){
			options.group.setColIdx(0);
		}
		
		RowComparator main = new RowComparator(options.sort.getColIdx(), colTypes[options.sort.getColIdx()], options.sort.getFlag(), null);
		if (options.group.getEnabled()){  
			main = new RowComparator(options.group.getColIdx(), colTypes[options.group.getColIdx()], options.group.getFlag(), main);
		}
		
		Arrays.sort(cells, main);
	}
	
	private void formatDefaultStyles(){
		border = 1; 
		tableStyle.style = "border-collapse: collapse; border-style: none;";
		headerStyle.style = "background-color: #FAFAFA; font-style: italic; text-align:center;";
		footerStyle.style = "background-color: #FAFAFA; font-style: italic; text-align:center;";
		titleCell.style.style = "background-color: #E0E0E0; text-align: center; font-weight: bold;";
	} 
	
	public void setBorder(int value){
		border = value;
	}
	
	public CssStyle getStyle(){
		return tableStyle;
	}
	
	public CssStyle getHeaderRowStyle(){
		return headerStyle;
	}
	
	public int getCols(){
		return cols;
	}
	
	public int getRows(){
		return cells.length;
	}
	
	public TableCell getTitleCell(){
		return titleCell;
	}
	
	public HeaderCell getToMenuCell(){
		return topMenuCell;
	}
	
	public HeaderCell getBottomMenuCell(){
		return bottomMenuCell;
	}
	
	public CssStyle getColumnStyle(int index){
		return columnStyles[index];
	}
	
	public CssStyle getRowStyle(int index){
		return rowStyles[index];
	}
	
	public HeaderCell getHeaderCell(int index){
		return headers[index];
	}
	
	public TableCell getCell(int col, int row){
		return cells[row][col];
	}
	
	public void setColumnTypes(Class [] types){
		colTypes = new Class[cols];
		for (int i=0; i < cols; i++){
			if (i < types.length){
				colTypes[i] = types[i];
			} else {
				colTypes[i] = String.class;
			} 
		}  
	}
	
	public void setHeaderValues(String [] values){
		for (int i=0; i < cols; i++){
			if (i < values.length){
				headers[i].value = values[i];
				headers[i].setVisible(true);
			} else {
				headers[i].setVisible(false);
			}
		}
	}
		
	public void materialize(String [][] values) {
		this.cells = new TableCell[values.length][];
		 
		for (int i=0; i < values.length; i++){
			this.cells[i] = new TableCell[cols];
			for (int j=0; j < cols; j++){ 
				this.cells[i][j] = new TableCell(columnStyles[j]);
				this.cells[i][j].value = values[i][j];
			}
		}
		
		initRowStylesAndSort();
	}
	
	public void materialize(ResultSet rs) throws Exception {
		if (cols != rs.getMetaData().getColumnCount()){
			throw new RuntimeException("Bad column count");
		}
		
		for (int i=0; i < cols; i++){
			rs.getMetaData().getColumnType(i + 1);
		}
		
		List<TableCell []> rows = new ArrayList<TableCell []>();
		while(rs.next()){
			TableCell [] row = new TableCell[cols];
			for (int i=0; i < cols; i++){ 
				row[i] = new TableCell(columnStyles[i]);
				row[i].value = rs.getObject(i + 1);
			}
			rows.add(row);
		}  
		
		cells = (TableCell [][]) rows.toArray(new TableCell [][]{});
	
		initRowStylesAndSort();
	}
	
	private void initRowStylesAndSort(){
		rowStyles = new CssStyle[cells.length];
		for (int i=0; i < rowStyles.length; i++){
			rowStyles[i] = new CssStyle();	
		}
		
		if (options.getAllowCustomSort() && getHeaderCell(options.group.getColIdx()).allowCustomSort){
			sort(); 
		}
	}
	
	private static String _style(CssStyle style){
		if (style.style != null){
			return " style='" + HtmlUtil.escapeHTML(style.style) + "'";
		}
		return "";
	}
	
	private static String _class(CssStyle style){
		if (style.cssclass != null){
			return " class='" + HtmlUtil.escapeHTML(style.cssclass) + "'";
		}
		return "";
	}
	
	private static String openTag(String tag, CssStyle style){
		return openTag(tag, style, "");
	} 
	
	private static String openTag(String tag, CssStyle style, String ex){
		return "<" + tag + _class(style) + _style(style) + " " + ex + ">";
	}
	
	private static void rowCell(Writer out, TableCell cell, int vcols) throws IOException {
		if (cell.value != null){
			out.write(openTag("tr", cell.style));
			out.write("<td colspan='" + vcols + "'>");
			if (cell.escaped){
				out.write(cell.value.toString());
			} else {
				out.write(HtmlUtil.escapeHTML(cell.value.toString()));
			}
			out.write("</td>");
			out.write("</tr>\n");
		}
	}
	
	private String getActions(IActionDispatchEncoder dispatcher, int colIdx){
		String act = ""; 
		if (options.getAllowCustomSort() && getHeaderCell(colIdx).allowCustomSort){
			
			ColumnFlag target = null;
			if (options.sort.getEnabled() && options.sort.getColIdx() == colIdx){
				target = options.sort;
			}
			if (options.group.getEnabled() && options.group.getColIdx() == colIdx){
				target = options.group;
			}
			
			String sortSymbol = "[srt]";
			if (target != null){
				if (target.getFlag()){
					sortSymbol = "[a-z]";
				} else {
					sortSymbol = "[z-a]";
				} 
			} 
			  
			String sort = dispatcher.encodeActionDispatch(
				m_SortByCol, sortSymbol, "text-decoration: none; font-weight: bold; color: #000000;",
				new String [] {"inColIdx"}, 
				new String [] {"" + colIdx}
			);
			
			String groupSymbol = "[grp]"; 
			if (options.group.getEnabled() && options.group.getColIdx() == colIdx){
				groupSymbol = "[ungrp]"; 
			}  
			
			String group = dispatcher.encodeActionDispatch(
				m_GroupByCol, groupSymbol, "text-decoration: none; font-weight: bold; color: #000000;",
				new String [] {"inColIdx"}, 
				new String [] {"" + colIdx}
			); 
			  
			act = " " + sort + " " + group;
		}   
		
		return act;
	}
	
	private void rowHeader(Writer out, IActionDispatchEncoder dispatcher, int vcols) throws IOException {
		out.write(openTag("tr", headerStyle));
		for (int i=0; i < cols; i++){
			if (!headers[i].visible) {
				continue;
			} 
	
			Object value = headers[i].value;
			if (value == null){
				value = "<NULL>";
			}
 			
			out.write(openTag("td", headers[i].style));		 
			if (headers[i].escaped){
				out.write(value.toString());
			} else {
				out.write(HtmlUtil.escapeHTML(value.toString()));
			}
			out.write(getActions(dispatcher, i));
			out.write("</td>");
		}
		out.write("</tr>\n");
	}
	
	private void rowCells(Writer out, IActionDispatchEncoder dispatcher, int vcols) throws IOException {
		
		class Span {
			int from;
			int to;
			int len;
		}
	 	
		Span span = null;
		for (int i=0; i < cells.length; i++){
			
			//end old span
			if (span!= null){
				if (!(i < span.to)){
					span = null;
				}
			} 
 
			// begin new span
			if (span == null){
				span = new Span();
				span.from = i;
				span.to = i;
			    
				// measure span extent 
				if (options.group.getEnabled()){
					Object base = cells[i][options.group.getColIdx()].getValue(); 
					for (int j=i; j < cells.length; j++){
						Object comp = cells[j][options.group.getColIdx()].getValue();
						if (base == comp || (base != null && base.equals(comp))){
							span.to++;	
						} else {
							break;
						}
					}
				}
			} 
			span.len = span.to - span.from;
						
			// render row
			out.write(openTag("tr", rowStyles[i]));
			for (int j=0; j < cols; j++){
				
				// do not render hidden columns
				if (!headers[j].visible) {
					continue;
				}
				 				
				// handle span 
				String rowspan = "";
				if (j == options.group.getColIdx()){
					if (i == span.from){
						rowspan = "valign='top' rowspan='" + span.len + "'";
					} else {
						continue;
					}
				}
				
				TableCell cell = cells[i][j];
				
				Object value = cell.value;
				if (value == null){
					value = "<NULL>";
				}	
				 
				CssStyle style = cell.style;
				if (style == null){
					style = columnStyles[j];
				} 
				 				 
				out.write(openTag("td", style, rowspan));
				if (cell.escaped){
					out.write(value.toString());
				} else {
					out.write(HtmlUtil.escapeHTML(value.toString()));
				}
				out.write("</td>");	
			}
			out.write("</tr>\n");
		}
	}
	
	private void rowFooter(Writer out, IActionDispatchEncoder dispatcher, int vcols) throws IOException {
		out.write(openTag("tr", footerStyle));
		out.write("<td align='right' colspan='" + vcols + "'>");
		out.write(cells.length + " rows");
		out.write("</td>");
		out.write("</tr>\n");
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		// count visible columns
		int vcols = 0;
		for (int i=0; i < cols; i++){
			if (headers[i].visible){
				vcols++;
			}
		}
   
		// render
		out.write(openTag("table", tableStyle, " border='" + border + "'"));
			rowCell(out, topMenuCell, vcols);
			rowCell(out, titleCell, vcols);		
			rowHeader(out, dispatcher, vcols);
			if (cells.length != 0){
				rowCells(out, dispatcher, vcols);
			}
			rowFooter(out, dispatcher, vcols);
			rowCell(out, bottomMenuCell, vcols);
		out.write("</table>\n");
	}
	
}
