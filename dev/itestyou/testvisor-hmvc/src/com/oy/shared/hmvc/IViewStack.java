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

/**
 * Only the top view in the stack will receive the dispatch() and render().
 * This can emulate the modal windows in Windows. If you want to create a 
 * composite view containing other views, you have to write adapters to do
 * the composition.     
 *
 */

public interface IViewStack {

	public void push(IView view);
	public void pop(); 
	public IView peek();
	 
}
