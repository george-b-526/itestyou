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


package com.oy.shared.hmvc.impl;

import java.util.LinkedList;
import java.util.List;

import com.oy.shared.hmvc.IView;
import com.oy.shared.hmvc.IViewStack;

public class ViewStack implements IViewStack {

	private List stack = new LinkedList();
	
	public void push(IView view){
		stack.add(view);
	}
	
	public void pop(){
		if (stack.size() != 0){
			stack.remove(stack.size() - 1);
		}
	}
	
	public IView peek(){
		if (stack.size() != 0){
			return (IView) stack.get(stack.size() - 1);
		} 
		
		return null;
	}	
	
}
