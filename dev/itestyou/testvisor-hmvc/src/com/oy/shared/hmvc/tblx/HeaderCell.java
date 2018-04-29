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

public class HeaderCell extends TableCell {

	boolean visible = true;
	boolean allowCustomSort = true;
	String caption;
	
	public HeaderCell(){
		super(null);
		
		style = new CssStyle();
	}
	
	public void setVisible(boolean value){
		visible = value;
	}
		
	public void setAllowCustomSort(boolean value){
		allowCustomSort = value;
	}
	
}
