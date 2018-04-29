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

public class CssStyle {

	String cssclass;
	String style;
	
	public static CssStyle cloneFrom(CssStyle prototype){
		CssStyle target = new CssStyle();
		target.style = prototype.style;
		return target;
	}
	
	public void addStyle(String value){
		if (style == null){
			style = "";
		} 
		style += value;
	}
	 
	public void setClass(String value){
		cssclass = value;
	}
	
	public void setStyle(String value){
		style = value;
	}
	
	public void getCssClass(String value){
		cssclass = value;
	}
	
}
