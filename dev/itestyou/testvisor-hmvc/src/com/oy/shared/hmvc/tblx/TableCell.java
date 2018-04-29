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

public class TableCell {

	private CssStyle parent;

	Object value;
	CssStyle style;
	boolean escaped;
	
	public TableCell(CssStyle parent){
		this.parent = parent;
	}
	
	public String getValue(){
		if (value != null){
			return value.toString();
		}
		return null;
	}
	
	public void setValue(Object value){
		setValue(value, false);
	}
	
	public void setValueEscaped(Object value){
		setValue(value, true);
	}
	
	private void setValue(Object value, boolean escaped){
		this.value = value;
		this.escaped = escaped;
	}
	
	public void setEscaped(boolean value){
		escaped = value;
	}
	 
	public CssStyle getStyle(){
		if (style == null){
			style = CssStyle.cloneFrom(parent);
		} 
		return style;
	}
	
}
