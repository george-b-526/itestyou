package com.oy.tv.app;

import java.sql.SQLException;

import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.tv.app.model.ViewModel;
import com.oy.tv.app.view.RootView;
import com.oy.tv.dao.core.AllDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.ns.ResourceLocator;

public class ViewCtx extends BaseViewCtx {
	
	private ViewModel model;
	
	public ViewCtx(IPropertyProvider ipp){
		dbName = ipp.getPropertyValue(ResourceLocator.DB_NAME_PROPERTY_NAME);
		connStr = ipp.getPropertyValue(ResourceLocator.CONN_STR_PROPERTY_NAME);
		
		setTop(new RootView(this));     
	}
	
	public ViewModel getModel(){
		return model;
	}
	    
	public void reset(){  
		req.getSession().invalidate();
	}    
	
	@Override
	public void init(AnyDB db) throws SQLException {
		new AllDAO().init(db);
		
		if (model == null){
			model = new ViewModel(this);
		}
	}

  	
}
