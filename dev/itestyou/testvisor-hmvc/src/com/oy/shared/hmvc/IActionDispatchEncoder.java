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


package com.oy.shared.hmvc;

import com.oy.shared.hmvc.impl.BaseAction;

public interface IActionDispatchEncoder {

	public String encodeActionDispatch(BaseAction act, String caption);
	public String encodeActionDispatch(BaseAction act, String caption, String style);
	
	public String encodeActionDispatch(BaseAction act, String caption, String name, String value);
	public String encodeActionDispatch(BaseAction act, String caption, String [] names, String [] values);
	public String encodeActionDispatch(BaseAction act, String caption, String style, String [] names, String [] values);	
	
}
