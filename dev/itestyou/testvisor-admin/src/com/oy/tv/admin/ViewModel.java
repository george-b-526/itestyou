package com.oy.tv.admin;

import java.util.Date;

import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.dao.identity.AllDAO;
import com.oy.tv.dao.identity.CustomerDAO;
import com.oy.tv.schema.core.CustomerBO;
import com.oy.tv.schema.core.UserBO;


public class ViewModel {
    
	public static String VERSION = "0.1.1";
	     
	private ViewCtx ctx;  
	private UserBO user;
	
	
	public ViewModel(ViewCtx ctx){
		this.ctx = ctx;
		    
		reset();
	}	
	     
	public void login(String name, String pwd) throws Exception {
		UserBO user = UserDAO.login(ctx.getDb(), name, pwd);
		if (user == null){
			CustomerBO customer;
			ctx.getDb().execSelect("USE " + AllDAO.NS_DEFAULT + ";");
			try {
				customer = CustomerDAO.login(ctx.getDb(), name, pwd);
			} finally {
				ctx.getDb().execSelect("USE " + com.oy.tv.dao.core.AllDAO.NS_DEFAULT + ";");
			}
			
  		if (customer == null){
  			throw new RuntimeException("Account not found or bad user name or password.");
			}
			
			user = new UserBO();  
			user.setId(customer.getId());
			user.setName(customer.getName());
			user.setPasswordHash(customer.getPasswordHash());
			user.setProperties(customer.getProperties());
			user.setState(customer.getState());
		}
		
		if (user.getProperties() == null){
			// admin
			if (user.getId() == 1000 || user.getId() == 1){
				user.setProperties("roles={admin}");
			}
			
			// wwalter
			if (user.getId() == 3){
				user.setProperties("roles={editor}");
			}			
			
			// orepetskiy
			if (user.getId() == 4){
				user.setProperties("roles={translator}\ntranslator_of={ru}");
			}			

		}
		
		this.user = user;
		
		ctx.log().message("Login " + user.getName() + " on " + new Date());
	}    
      
	public void logout(){
		if (user != null){
			ctx.log().message("Logout " + user.getName() + " on " + new Date());
			user = null;    
			ctx.reset();
		}    
	}
 	 
	public boolean isAuth(){
		return getPrincipal() != null;
	}
	
	public UserBO getPrincipal(){
		return user;
	}
	 
	public void reset(){
		user = null;
	}
		
}
