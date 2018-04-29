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


package com.oy.shared.hmvc.servlet;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.impl.BaseView;
import com.oy.shared.hmvc.util.HtmlUtil;

public class ServletActionAdapter implements IPropertyProvider, IActionDispatchEncoder {

	private static final String ACTION_ID_TOKEN = "action_id"; 
	 
	private HttpServletRequest request;

	public int getActionId(){
		int id;
		
		try {
			id = Integer.valueOf(getPropertyValue(ACTION_ID_TOKEN)).intValue();
		} catch (Exception e){
			id = -1;
		}
		 
		return id;
	}
	
	public ServletActionAdapter(HttpServletRequest request){
		this.request = request;
	}

	public String getPropertyValueRaw(String name){
		return request.getParameter(name);
	}
	
	public boolean hasPropertyValue(String name){
		return request.getParameter(name) != null;
	}
	
	public String getPropertyValue(String name){
		return HtmlUtil.unescapeHTML(request.getParameter(name));
	}
			
	public String [] getPropertyValues(String name){
		String [] values = request.getParameterValues(name);
		if (values == null){
			values = new String [] {};
		}

		for (int i=0; i < values.length; i++){
			values[i] = HtmlUtil.unescapeHTML(values[i]);
		}
		
		return values;
	}
	
	public String encodeActionDispatch(BaseAction act, String caption, String [] names, String [] values){
		return encodeActionDispatch(act, caption, "", names, values);
	}

	public String encodeActionDispatch(BaseAction act, String caption, String name, String value){
		return encodeActionDispatch(act, caption, "", new String [] {name}, new String [] {value});
	}
	
	public String encodeActionDispatch(BaseAction act, String caption, String style, String [] names, String [] values){
		StringBuffer sb = new StringBuffer();
		sb.append(ACTION_ID_TOKEN + "=" + act.hashCode());
		   
		try {
			for (int i=0; i < names.length;i++){
				if (!names[i].startsWith(BaseView.IN_PARAM_PREFIX)){
					throw new RuntimeException("Bad parameter name");
				}
				
				sb.append("&" + URLEncoder.encode(names[i], "UTF-8") + "=" + URLEncoder.encode(values[i], "UTF-8"));
			}
		} catch  (Exception e){
			throw new RuntimeException(e);
		}
		 
		String url = "view?" + sb.toString();
	
		return "<a href=\"" + url + "\" style=\"" + style + "\">" + caption + "</a>";
	
	}	
	
	// here we encode action callback
	public String encodeActionDispatch(BaseAction act, String caption){
		return encodeActionDispatch(act, caption, "");
	}
	
	public String encodeActionDispatch(BaseAction act, String caption, String style){
		return encodeActionDispatch(act, caption, style, new String [] {}, new String [] {});		
	}
	
}
