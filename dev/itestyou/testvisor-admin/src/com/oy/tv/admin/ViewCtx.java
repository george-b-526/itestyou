package com.oy.tv.admin;

import java.sql.SQLException;

import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.tv.admin.view.RootView;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.ns.ResourceLocator;

public class ViewCtx extends BaseViewCtx {
	
	private ViewModel model;
	private IPropertyProvider ipp;
	
	public ViewCtx(IPropertyProvider ipp){ 
		dbName = ipp.getPropertyValue(ResourceLocator.DB_NAME_PROPERTY_NAME);
		connStr = ipp.getPropertyValue(ResourceLocator.CONN_STR_PROPERTY_NAME);
		model = new ViewModel(this);
		
		this.ipp = ipp;
		
		reset();
	}
	
	public ViewModel getModel(){
		return model;
	}
	  
	public void reset(){  
		setTop(new RootView(this, ipp));
	}  
	
	@Override
	public void init(AnyDB db) throws SQLException {
		new AllDAO().init(db);		
	}
  	
}
