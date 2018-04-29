package com.oy.tv.admin.view;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.unit.EditUnitForm;
import com.oy.tv.admin.view.unit.GenerateVariationsForm;
import com.oy.tv.admin.view.unit.TranslateUnitForm;
import com.oy.tv.admin.view.unit.UnitListView;
import com.oy.tv.admin.view.unit.UnitPreView;
import com.oy.tv.admin.view.unit.UnitTagView;
import com.oy.tv.admin.view.unit.VariationListView;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.schema.core.UnitBO;

public class HomePageView extends WebView {
	
	BaseAction m_ListUnits = new BaseAction (){
		public void execute(IPropertyProvider provider){
			pushView(unitlist);
		}
	};
	
	BaseAction m_AddUnit = new BaseAction (){
		public void execute(IPropertyProvider provider){
			try {
				UnitBO unit = UnitDAO.addUnit(getDb(), getModel().getPrincipal());
				
				getCtx().pushMessage("Unit Added");
				editunit.setFocus(unit);
				pushView(editunit);    
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to create new  Unit.");
			}  
		}      
	};
	
	public IPropertyProvider pp;
	public GenerateVariationsForm genvar;  
	public VariationListView varlist;
	public UnitListView unitlist;
	public EditUnitForm editunit;
	public TranslateUnitForm translateunit;
	public UnitPreView preview;
	public UnitTagView tag;
	public SummaryView summary;
	RootView parent;
	 
	private List<IAction> stack = new ArrayList<IAction>();
	
	public HomePageView(RootView parent, IPropertyProvider pp){
		super(parent.getCtx());
		
		this.pp = pp;
		this.parent = parent;
		
		genvar = new GenerateVariationsForm(this);
		varlist = new VariationListView(this); 
		unitlist = new UnitListView(this);
		preview = new UnitPreView(this);
		tag = new UnitTagView(this);
		editunit = new EditUnitForm(this);
		translateunit = new TranslateUnitForm(this);
		summary = new SummaryView(this);
		
		installView(genvar);
		installView(varlist);
		installView(unitlist);
		installView(preview);
		installView(tag);
		installView(editunit);
		installView(translateunit);
		installView(summary);
		  
		pushView(summary);
	} 
	   
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		getFirstVisibleView().render(dispatcher, out);
	}
	
	public void pushAction(IAction action){
		stack.add(stack.size(), action);
	}
	  
	public void popView(){
		if (stack.size() <= 0){
			throw new RuntimeException("Empty action stack");
		}
		IAction top = stack.remove(stack.size() - 1);
		top.act();
	}  
			
} 
 