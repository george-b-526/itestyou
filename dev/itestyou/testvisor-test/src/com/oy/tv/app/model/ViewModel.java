package com.oy.tv.app.model;

import java.io.Writer;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.tv.app.IExitStrategy;
import com.oy.tv.app.ViewCtx;
import com.oy.tv.dao.core.BundleDAO;
import com.oy.tv.dao.core.TestDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.UserDAO;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.db.PagedKeyList;
import com.oy.tv.model.unit.RenderContext;
import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.schema.core.BundleBO;
import com.oy.tv.schema.core.ETestState;
import com.oy.tv.schema.core.TestBO;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.VariationBO;
import com.oy.tv.util.XmlUtil;

public class ViewModel {
	  
	private TestContext tctx = new TestContext();
	  
	private ViewCtx ctx;
	private SecureRandom srnd = new SecureRandom();
  	
	public ViewModel(ViewCtx ctx){
		this.ctx = ctx;
	}
	
	public void init(int bundleId, int ownerId, IExitStrategy onExit) throws SQLException {
		tctx.onExit = onExit;
		tctx.owner = UserDAO.loadUser(ctx.getDb(), ownerId);
		tctx.bun = BundleDAO.loadBundle(ctx.getDb(), bundleId);
		tctx.persistent = true;
		
		init();
	}
		   
	public void init(int unitId, IExitStrategy onExit) throws SQLException {
		tctx.onExit = onExit;
		tctx.owner = null;
		tctx.bun = createAdHockBundle(unitId);
		tctx.persistent = false;
  		
		init();
	}
	
	private BundleBO createAdHockBundle(int unitId){
		BundleBO bun = new BundleBO();
		bun.getUnitIds().add(unitId);
		return bun;
	}
	
	public InteractiveOptions getOptions(){
		return tctx.options;  
	}
	
	public int getBundleId(){
		return tctx.bun.getId();
	}
	   
	public TestBO getTest(){
		return tctx.test;
	}
	
	public int getStepsCount(){
		return tctx.steps.size();
	}
	
	public int getCurrentStepIndex(){
		return tctx.currIndex;
	}
	
	public IExitStrategy getExitStrategy(){
		return tctx.onExit;
	}
	
	private void recordVote(Step curr, int voteIndex, ETestState state) throws SQLException {
		curr.complletedOn = new Date();
		
		tctx.test.getAnswerIndexes().add(voteIndex);
		
		int at = tctx.test.getAttempted();
		int co = tctx.test.getCompleted();
		int cr = tctx.test.getCorrect();
		int ic = tctx.test.getIncorrect();
		
		at++;
		co++;
		if (curr.new2old.containsKey(voteIndex)){
			tctx.test.getScoreSheet().add(true);
			cr++;
		} else {
			tctx.test.getScoreSheet().add(false);
			ic++;
		}
		
		if (tctx.persistent){
			TestDAO.updateTest(
				ctx.getDb(), tctx.test, 
				new Date(), 
				tctx.test.getAnswerIndexes(), tctx.test.getScoreSheet(),
				at, co, cr, ic,
				state
			);
		}
	}
	
	public boolean voteAndNext(int voteIndex){
		if (tctx.currIndex < tctx.steps.size()){
			Step curr = tctx.steps.get(tctx.currIndex);
			
			tctx.currIndex++;
			boolean more;
			ETestState state;
			if (tctx.currIndex < tctx.steps.size()){
				more = true;
				state = ETestState.IN_PROGRESS;
			} else {
				more = false;
				state = ETestState.COMPLETED;
			}
			
			try {
				recordVote(curr, voteIndex, state);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
			  
			if (more){
				return true;
			} else {
				return false;
			}			
		} else {
			return false;
		}
	}  
	
	public int getCurrentChoiceCount(){
		return tctx.steps.get(tctx.currIndex).getChoiceCount();
	}
	
	private UnitContext expand(AnyDB db, Step curr) {
		try {
			UnitContext ctx = new UnitContext();
			
			UnitBO unit = UnitDAO.loadUnit(db, curr.group.unitId);
			VariationBO var = VariationDAO.loadVariation(db, curr.variationId);
			
			YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
			try {
				UnitProcessor up = new UnitProcessor();
			  	
				up._doc = XmlUtil.loadXmlFrom(unit.getXml());
				up._ctx = ctx;
				up._ctx.values = var.getValues();
				up._log = System.out;
				up._eval = eval;
							    
				up.evaluate();  
				     
				YacasEvaluatorEx.leaseComplete(eval);
			} catch (Exception e){
				YacasEvaluatorEx.leaseFail(eval);
				throw new RuntimeException("Failed to render unit.", e);
			}	

			UnitContext.shuffle(ctx, curr.new2old, srnd);
			curr.choiceCount = ctx.choices.size();

			return ctx;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}
	  
	public void renderCurrent(AnyDB db, Writer out) throws Exception {
		Step curr = tctx.steps.get(tctx.currIndex);
		UnitContext uctx = expand(ctx.getDb(), curr);
		curr.startOn = new Date();
		UnitProcessor.render(uctx, new RenderContext(), out);
	}  
	  
	private static UnitGroup [] groupByUnitId(BundleBO bundle){
		Map<Integer, UnitGroup> all = new HashMap<Integer, UnitGroup>(); 
		for (int i=0; i < bundle.getUnitIds().size(); i++){
			int id = bundle.getUnitIds().get(i);
			
			UnitGroup group = all.get(id);
			if (group == null){
				group = new UnitGroup();
				group.unitId = id;
				group.count = 0;
				  
				all.put(id, group);
			}
			group.count++;
		}
		return all.values().toArray(new UnitGroup [] {});
	}
	
	private void createVariations(UnitGroup [] groups) {
		try {
			tctx.steps = new ArrayList<Step>();
			for (int i=0; i < groups.length; i++){
				UnitGroup group = groups[i];
				
				UnitBO unit = UnitDAO.loadUnit(ctx.getDb(), group.unitId);	
				PagedKeyList pkl = VariationDAO.getAllPaged(ctx.getDb(), unit, Integer.MAX_VALUE, 0);				  
				for (int j=0; j < group.count && pkl.ids.size()> 0; j++){
					int index = srnd.nextInt(pkl.ids.size());
					int key = pkl.ids.remove(index);
      
					Step step = new Step();
					step.group = group;
					step.variationId = key;
  				  	
					tctx.steps.add(step);
				}
			}
		} catch (Exception e){
			throw new RuntimeException(e);
		}  
	}
	
	private List<Integer> getVariationIds(){
		UnitGroup [] groups = groupByUnitId(tctx.bun);
		createVariations(groups);  	      
		List<Integer> variationIds = new ArrayList<Integer>();
		for (int i=0; i < tctx.steps.size(); i++){
			Step step = tctx.steps.get(i);
			variationIds.add(step.variationId);
		}
		return variationIds;
	}    
	
	private void initTest() throws SQLException {
		List<Integer> variations = getVariationIds();
		if (tctx.persistent){
			tctx.test = TestDAO.addTest(ctx.getDb(), tctx.bun, tctx.owner);  
			TestDAO.updateTest(
				ctx.getDb(), tctx.test, 
				new Date(), variations, ETestState.STARTED
			);
		} else {
			tctx.test = new TestBO();
			tctx.test.getVariationIds().clear();
			tctx.test.getVariationIds().addAll(variations);
		}
	}
	  
	private void initActivity(){
		tctx.currIndex = 0;
		if (tctx.steps.size() != 0){
			expand(ctx.getDb(), tctx.steps.get(tctx.currIndex));
		}
	}
  	    
	private void init() throws SQLException {
		initTest();
		initActivity();
	}
	
}
