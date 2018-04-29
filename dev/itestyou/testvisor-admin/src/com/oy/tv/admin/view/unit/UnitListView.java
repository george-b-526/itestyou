package com.oy.tv.admin.view.unit;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.admin.WebView;
import com.oy.tv.admin.view.HomePageView;
import com.oy.tv.admin.view.IAction;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.model.unit.TranslationContext;
import com.oy.tv.model.unit.TranslationWeaver;
import com.oy.tv.parts.WebParts;
import com.oy.tv.schema.core.EObjectState;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;

public class UnitListView extends WebView {
	
	BaseAction m_Locale = new BaseAction (){
		String inLocale;
		public void execute(IPropertyProvider provider){
			lang = inLocale;
		}
	};
	
	BaseAction m_Page = new BaseAction (){
		int inPage;
		public void execute(IPropertyProvider provider){
			page = inPage;
		}
	};
	
	BaseAction m_Tag = new BaseAction (){
		int inId;
		public void execute(IPropertyProvider provider){
			UnitBO unit = getUnitOrFail(getCtx().getDb(), inId);
			
			parent.pushAction(
				new IAction(){
					public void act(){
						parent.pushView(UnitListView.this);					
					}  
				}
			);  
			
			parent.tag.setFocus(unit);
			parent.pushView(parent.tag);
		}
	};  
	
	BaseAction m_Edit = new BaseAction (){
		int inId;
		public void execute(IPropertyProvider provider){
			UnitBO unit = getUnitOrFail(getCtx().getDb(), inId);

			parent.pushAction(
				new IAction(){
					public void act(){
						parent.pushView(UnitListView.this);					
					}  
				}
			);  
			
			parent.editunit.setFocus(unit);
			parent.pushView(parent.editunit);
		}    
	};

	BaseAction m_NonTranslatable = new BaseAction (){
		int inId;
		public void execute(IPropertyProvider provider){
			try {
				UnitBO unit = new UnitBO();
				unit.setId(inId);
				unit.setType(UnitDAO.UnitType.WDGT.ordinal());
				
  			TranslationDAO.markNonTranslatable(getCtx().getDb(), unit);
  			getCtx().pushMessage("Marked non-translatable.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to save translation.", e);
			}
		}
	};

	BaseAction m_Translatable = new BaseAction (){
		int inId;
		public void execute(IPropertyProvider provider){
			try {
				UnitBO unit = new UnitBO();
				unit.setId(inId);
				unit.setType(UnitDAO.UnitType.WDGT.ordinal());
				
  			TranslationDAO.markTranslatable(getCtx().getDb(), unit);
  			getCtx().pushMessage("Marked translatable.");
			} catch (Exception e){
				getCtx().log().message(e.getMessage());
				throw new RuntimeException("Failed to save translation.", e);
			}
		}
	};
	
	BaseAction m_Translate = new BaseAction (){
		int inId;
		String inLang;
		public void execute(IPropertyProvider provider){
			UnitBO unit = getUnitOrFail(getCtx().getDb(), inId);

			parent.pushAction(
				new IAction(){
					public void act(){
						parent.pushView(UnitListView.this);					
					}  
				}
			);  
			
			parent.translateunit.setFocus(unit, inLang);
			parent.pushView(parent.translateunit);
		}    
	};

	
	BaseAction m_Done = new BaseAction (){
		public void execute(IPropertyProvider provider){
			parent.pushView(parent.summary);
		}  
	};
	
	public final int PER_PAGE = 10;
	
	private int page = 0;
	private HomePageView parent;
	private String lang = TranslationDAO.LANG_EN;
	
	public UnitListView(HomePageView parent){
		super(parent.getCtx());
		
		this.parent = parent;
	} 
	
	public static UnitBO getUnitOrFail(AnyDB db, int id){
		UnitBO unit = null;
		try {
			unit = UnitDAO.loadUnit(db, id);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		if (unit == null){
			throw new RuntimeException("Failed to load unit id " + id + ".");
		}   
		return unit;
	} 
	
	private void renderTranslateAction (IActionDispatchEncoder dispatcher, Writer out, 
			Set<String> key2alang, Set<String> ilang, String lang, 
			int id) throws Exception {
		String styleActive = "#00FF00";
		String styleInActive = "#0000FF";
		String styleNormal = WebParts.STYLE_NORMAL_BUTTON_COLOR;
		
		String style = styleNormal;
		if (key2alang.contains(lang)){
			style = styleActive;
		}
		if (ilang.contains(lang)){
			style = styleInActive;
		}		
		String act = WebParts.styleButton(
			dispatcher, m_Translate, lang.toUpperCase(), new String [] {"inId", "inLang"}, new String [] {"" + id, lang}, style
		);
		out.write(act);		
	}
	
	private boolean isL18N(Set<String> alang) {
		return !alang.contains("*");
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		out.write("<p>List Units | ");
  		  
		PagedKeyList pkl;
		try {
			UserBO actor = getCtx().getModel().getPrincipal();
			
			if (UserDAO.isAdmin(getModel().getPrincipal()) || 
					UserDAO.isTranslator(getModel().getPrincipal())
			){
				actor = null;
			}
			pkl = UnitDAO.getAllPaged(getCtx().getDb(), actor, PER_PAGE, page);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		page = pkl.page;
		  					
		out.write("Total " + pkl.count+ " items, go to page: ");	
		for (int i=0; i < pkl.pages; i++){
			if (page == i){
				out.write(" <b>" + (i + 1) + "</b> ");	
			} else {
				String act = WebParts.button(dispatcher, m_Page, "" + (i + 1), new String [] {"inPage"}, new String [] {"" + i}, true);
				out.write(act);
				out.write(" ");
			}
		}

		out.write(" | Locale: ");
		for (String lang: TranslationDAO.getSupportedlocales()) {
			if (TranslationDAO.LANG_ANY.equals(lang)) continue;
			out.write(WebParts.button(dispatcher, m_Locale, lang,
					new String [] {"inLocale"}, new String [] {lang}, !this.lang.equals(lang)));
		}		
		
		out.write("</p><hr/>");
		
		WebParts.beginPanel(out, "100%");
         
		out.write("<tr>");    
		out.write("<td align='center'>");
		out.write("<div align='left' style='width: 100%; text-align: left;'>");
		  
		// get translations
		TranslationBO [] tlns;
		try {
			tlns = TranslationDAO.getByNamespace(ctx.getDb(), 
					"/unit/math/", pkl.ids);
		} catch (Exception e){
			throw new RuntimeException(e);
		}
		
		// fetch & render
		for (int i=0; i < pkl.ids.size(); i++){
			String ex = "float: left; clear: both;";
			if (i % 2 == 1){
				ex = "float: right;";
			}
			out.write("<div style='padding: 8px; width: 45%; " + ex + "'>");
			
			int id = pkl.ids.get(i);
			
			Set<String> alang = new HashSet<String>();
			Set<String> ilang = new HashSet<String>();
			final String key = "/unit/math/" + id;
			for (TranslationBO tln : tlns){
				if (key.equals(tln.getKey())) {
					if (tln.getState().equals(EObjectState.ACTIVE)){
						alang.add(tln.getLang());
					}
					if (tln.getState().equals(EObjectState.INACTIVE)){
						ilang.add(tln.getLang());
					}
				}
			}
			
			try {
				UnitBO unit = UnitDAO.loadUnit(getCtx().getDb(), id);
				String act;	

				{
  				out.write("Id = " + unit.getId());
  				
  				if (unit.getState().equals(EObjectState.ACTIVE)){     
  					out.write(", State = <font style='background-color: #00FF00;'><b>&nbsp;ACTIVE&nbsp;</b></font>");
  				}      
  				
  				if (unit.getNotes() != null){
  					out.write(", Notes = <i>" + HtmlUtil.escapeHTML(unit.getNotes()) + "</i>");
  				}

  				out.write("<br />");
				}

				out.write("<div style='padding: 4px;'>");
				
				if (UserDAO.isEditor(getModel().getPrincipal())){
  				act = WebParts.button(  
  					dispatcher, m_Edit, "Edit", new String [] {"inId"}, new String [] {"" + id}, true
  				);
  				out.write(act);   				
				}
				
				if (UserDAO.isAdmin(getModel().getPrincipal())){
  				act = WebParts.button(  
  					dispatcher, m_Tag, "Tag", new String [] {"inId"}, new String [] {"" + id}, true 
  				);
  				out.write(act);
  				
  				if (!isL18N(alang)) {
    				act = WebParts.button(  
    					dispatcher, m_Translatable, "L18N", new String [] {"inId"}, new String [] {"" + id}, true 
    				);
    				out.write(act);
  				} else {
  					act = WebParts.button(  
    					dispatcher, m_NonTranslatable, "NON L18N", new String [] {"inId"}, new String [] {"" + id}, true 
    				);
    				out.write(act);
  				}
				}
				
				if (isL18N(alang)) {
					for (String lang : TranslationDAO.getSupportedlocales()) {
						boolean translatable =
							!TranslationDAO.LANG_ANY.equals(lang) &&
							!TranslationDAO.LANG_EN.equals(lang);
						if (translatable && UserDAO.canTranslate(getModel().getPrincipal(), lang)){
	  					renderTranslateAction(dispatcher, out, alang, ilang, lang, id);
						}						
					}
				}
				
				out.write("</div>");
				
				UnitBO aunit = unit;				
				try {
					TranslationBO tln = TranslationDAO.get(getCtx().getDb(), lang,
							"/unit/math/" + unit.getId());
		  		if (tln != null){
						TranslationContext ctx = TranslationWeaver.extractTranslatable(unit);
						ctx.resetToOriginal();

		  			TranslationContext.updateFromXml(ctx, tln.getData());  			
		  			aunit = TranslationWeaver.weaveTranslatable(unit, ctx);
		  		}
				} catch (Exception e){
					getCtx().pushMessage("Failed to load translation. " + e.getMessage());
				}
				UnitPreView.renderUnit(getCtx(), aunit, null, out, parent.pp, lang);
			} catch (Exception e){
				e.printStackTrace();
				out.write("Failed to render unit id " + id);
			}  
			
			out.write("</div>");
		}  
	
		out.write("</div>");
		out.write("</td>");
		out.write("</tr>");
		 
		WebParts.endPanel(out);

		out.write("<hr />");
  		
		String act = WebParts.button(dispatcher, m_Done, "Done");
		out.write(act);
	}
	
}
