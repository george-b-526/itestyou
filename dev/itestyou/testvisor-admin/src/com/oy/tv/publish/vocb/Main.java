package com.oy.tv.publish.vocb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oy.tv.dao.know.TermDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.schema.core.CorpusBO;
import com.oy.tv.schema.core.DiffItemBO;
import com.oy.tv.schema.core.ProjectionBO;
import com.oy.tv.schema.core.TermEditBO;
import com.oy.tv.schema.core.TermsBO;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/*

	http://localhost/publish?restartApplication&corpus=1&from=/lang/en&to=/lang/sr

*/

public class Main extends Application implements HttpServletRequestListener {

	final String test_str = "jdbc:mysql://localhost:10011/mysql?user=root&useUnicode=true&characterEncoding=utf8";
	
		private String cor_id;
		private String from;
		private String to;
    
  	public void onRequestStart(HttpServletRequest request,
  	    HttpServletResponse response) {
  		cor_id = request.getParameter("corpus");
  		from = request.getParameter("from");
  		to = request.getParameter("to");
  		if (from == null){
  			from = "en";
  		}
  		
//  		make this word for SAT where we dont have lang_
//  			- term
//  			- definition
    	//
    	// how to do auth
//    	show diff at save time; show that type and cat will be updated
  	}
  
  	public void onRequestEnd(HttpServletRequest request,
  	    HttpServletResponse response) {
  	}    
    
  	private void fill(Table table, ProjectionBO proj){
  		table.removeAllItems();
  		for (int i=0; i < proj.getTerms().size(); i++){
	  		TermsBO row = proj.getTerms().get(i);
	  		
	  		String valueFrom = "";
	  		if (row.getFrom() != null){
	  			valueFrom = row.getFrom().getValue();
	  		} else {
	  			// dont show rows where from is null as we dont support editing of toDimention
	  			// in this case
	  			continue;
	  		}
	  		
	  		
	  		String valueTo = "";
	  		if (row.getTo() != null){
	  			valueTo = row.getTo().getValue();
	  		}
	  		
	  		AbstractTextField ffrom;
	  		AbstractTextField fto;
	  		final int MAX_LINE_LEN = 128;
	  		if ((valueFrom != null && valueFrom.length() > MAX_LINE_LEN) 
	  				|| (valueTo != null && valueTo.length() > MAX_LINE_LEN)){
  	  		ffrom = makeTextArea(valueFrom);
  	  		fto = makeTextArea(valueTo);
	  		} else {
	  			ffrom = makeTextField(valueFrom);
	  			fto = makeTextField(valueTo);
	  		}
	  		ffrom.setReadOnly(true);
	  		
	  		table.addItem(new Object[] {
	  				i,
	  				ffrom,
	  				fto,
	  				row.getType(),
	  				row.getCategory()
  					}, i);
	  	}

	  	{
	  		// style table
  	  	table.setWidth("100%");
  	  	table.setHeight("350px");
	  		table.setPageLength(20);
  	  	
        // column width
        table.setColumnExpandRatio(proj.getDimentionFrom(), 0.5f);
        table.setColumnExpandRatio(proj.getDimentionTo(), 0.5f);

        // alignment
        table.setColumnAlignment("Id", Table.ALIGN_RIGHT);
        table.setColumnAlignment("Type", Table.ALIGN_CENTER);
        
	  		// turn off column reordering and collapsing
        table.setColumnReorderingAllowed(false);
        table.setColumnCollapsingAllowed(true);
        table.setColumnCollapsed("Category", true);
        
	  		// Allow selecting items from the table.
        table.setEditable(false);
        table.setSelectable(false);
  	  	table.setMultiSelect(false);
  	  	
  	  	// Send changes in selection immediately to server.
  	  	table.setImmediate(true);
	  	}  	  	
  	}
  	
    @Override
    public void init() {
	  	Panel main = new Panel();
	  	final Window mainWnd = new Window("ITestYou Publish", main); 
	  	setMainWindow(mainWnd);
	  	final Label statusBar = new Label();
	  	statusBar.setValue("Loading...");
	  	
	  	try {
    		CorpusBO corpus = new CorpusBO();
    		final ProjectionBO proj; {
      		try {
      			corpus.setId(Integer.parseInt(cor_id));
      		} catch (NumberFormatException nfe){
      			throw new UserException("Expected integer 'corpus'.");
      		}
      		if (from == null || to == null){
      			throw new UserException("Expected 'from' and 'to'.");
      		}      		
      		//corpus.setId(1);
      		corpus.setName("Corpus Name");
      		proj = load(corpus, from, to);
    		}

  	  	// table
  	  	final Table table = new Table(
  	  			"Item: " + corpus.getName() + ": " + proj.getCorpus().getId() + "|" + proj.getDimentionTo());
  	  	main.addComponent(table);
  	  	main.addComponent(statusBar);

  	  	table.addStyleName("components-inside");
  	  	
  	  	// columns
  	  	table.addContainerProperty("Id", Integer.class,  null);
  	  	table.addContainerProperty(proj.getDimentionFrom(), AbstractTextField.class,  null);
  	  	table.addContainerProperty(proj.getDimentionTo(), AbstractTextField.class, null);
  	  	table.addContainerProperty("Type", String.class, null);
  	  	table.addContainerProperty("Category", String.class, null);
  	  	
  	  	fill(table, proj);
  	  	
  	  	statusBar.setValue("Showing " + table.getItemIds().size() + " editable of " +  proj.getTerms().size() + " total items");
	  	
 	  	HorizontalLayout hl = new HorizontalLayout();
 	  	hl.setWidth("100%");
 	  	main.addComponent(hl);
  	  	
	  	HorizontalLayout actionPanel = new HorizontalLayout();
	  	hl.addComponent(actionPanel);
	  	hl.setComponentAlignment(actionPanel, Alignment.BOTTOM_CENTER);
	  	
	  	Button revert = new Button("Revert Changes");
	  	revert.addListener(new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
					fill(table, proj);
				}
			});
	  	
	  	Button save = new Button("Save Changes");
	  	
	  	actionPanel.addComponent(save);
	  	actionPanel.addComponent(revert);
	  	
	  	save.addListener(new Button.ClickListener() {
				public void buttonClick(ClickEvent event) {
					final Window wnd = new Window("Review & Save Changes");
					wnd.setModal(true);

					wnd.setHeight("600px");
					wnd.setWidth("800px");
					 
					wnd.setPositionX(50); 
					wnd.setPositionY(50);
					wnd.setResizable(false);
					
					final TextArea ta = new TextArea();
					ta.setWidth("100%");
					ta.setHeight("400px");
					wnd.addComponent(ta);
					
					List<TermEditBO> edits = new ArrayList<TermEditBO>();
					
					Iterator<?> iter = table.getItemIds().iterator();
					while(iter.hasNext()){
						int id = (Integer) iter.next();
						TermsBO terms = proj.getTerms().get(id);
					
						
						Item item = table.getItem(id);
						Property prop = item.getItemProperty(proj.getDimentionTo());
						
						String original = null;
						AbstractTextField field = (AbstractTextField) prop.getValue(); 
						String updated = (String) field.getValue();						
						
						if (updated != null && updated.trim().length() == 0){
							updated = null;
						}
						
						if (terms.getTo() != null){
							original = terms.getTo().getValue();
						}
						
						if (original == updated || (original != null && original.equals(updated))){
						} else {
							TermEditBO edit = new TermEditBO();
							edit.setId(id);
							edit.setValue(updated);
							edits.add(edit);							
						}
					}
					
					final List<DiffItemBO> items;
					try {
  					AnyDB db = new AnyDB();
  					db.open_mysql(test_str, "ITY_IKNOW");
  					try {
  						items = TermDAO.diff(db, proj, edits);
  						
  						StringBuffer sb = new StringBuffer();
  						for (DiffItemBO item : items){
  							sb.append(item.getDescription() + "\n");
  						}
  						ta.setValue(sb.toString());
  					} finally {
  						db.close();
  					}
					} catch (Exception e){
						throw new RuntimeException(e);
					}
					
										
					getMainWindow().addWindow(wnd);

		 	  	HorizontalLayout hl = new HorizontalLayout();
		 	  	hl.setWidth("100%");
		 	  	wnd.addComponent(hl);
					
					HorizontalLayout actionPanel = new HorizontalLayout();
			  	hl.addComponent(actionPanel);
			  	hl.setComponentAlignment(actionPanel, Alignment.BOTTOM_CENTER);
			  	
			  	Button save = new Button("Save");
			  	save.addListener(new Button.ClickListener() {
						public void buttonClick(ClickEvent event) {
							
							List<DiffItemBO> result;
							try {
		  					AnyDB db = new AnyDB();
		  					db.open_mysql(test_str, "ITY_IKNOW");
		  					try {
		  						result = TermDAO.apply(db, proj, items);
		  						
		  						StringBuffer sb = new StringBuffer();
		  						for (DiffItemBO item : result){
		  							sb.append(item.getDescription() + "\n");
		  						}
		  						ta.setValue(sb.toString());
		  					} finally {
		  						db.close();
		  					}
							} catch (Exception e){
								throw new RuntimeException(e);
							}
							
							if (result.size() == 0){
								wnd.getParent().removeWindow(wnd);
							}
						}
					});

		  		save.setEnabled(items.size() != 0);
			  	
			  	Button cancel = new Button("Cancel");
			  	cancel.addListener(new Button.ClickListener() {
						public void buttonClick(ClickEvent event) {
							wnd.getParent().removeWindow(wnd);
						}
					});
			  	
			  	actionPanel.addComponent(save);
			  	actionPanel.addComponent(cancel);
				}
	  	});
	  		  
	  	} catch (UserException e){
    		statusBar.setValue("Opps... Something went wrong. Here is an error: '" + e.getMessage() + "'.");
	  	} catch (Exception e){
    		statusBar.setValue("Opps... Something went wrong in our server. Give us a minute to fix it.");
    	}
	  }

    private AbstractTextField makeTextField(String text){
			TextField tf = new TextField();
			tf.setValue(text);
  		tf.setWidth("100%");
  		return tf;
    }
    
    private AbstractTextField makeTextArea(String text){
	  		TextArea ta = new TextArea();
	  		ta.setValue(text);
	  		ta.setWidth("100%");
	  		ta.setWordwrap(true);
  			ta.setRows(5);
  			return ta;
    }

    private ProjectionBO load(CorpusBO corpus, String dimFrom, String dimTo) throws Exception {
  		AnyDB db = new AnyDB();
			db.open_mysql(test_str, "ITY_IKNOW");
			try {
				return TermDAO.project(db, corpus, dimFrom, dimTo);
			} finally {
				db.close();
			}
    }
   
		class UserException extends Exception {
			public UserException(String message){
				super(message);
			}
		}

}
