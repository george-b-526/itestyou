package com.oy.tv.wdgt.view;

import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.sf.yacas.YacasEvaluatorEx;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.lw.perf.IPerfMonitor;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.BaseWebView;
import com.oy.tv.app.UserMessage;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.core.UnitDAO;
import com.oy.tv.dao.core.VariationDAO;
import com.oy.tv.dao.local.ObjectCacheDAO;
import com.oy.tv.dao.local.UnitCache;
import com.oy.tv.dao.runtime.ResponseDAO;
import com.oy.tv.dao.runtime.UserUnitResponseDAO;
import com.oy.tv.db.AnyDB;
import com.oy.tv.model.unit.TranslationContext;
import com.oy.tv.model.unit.TranslationWeaver;
import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.schema.core.ObjectCacheBO;
import com.oy.tv.schema.core.TranslationBO;
import com.oy.tv.schema.core.UnitBO;
import com.oy.tv.schema.core.UserBO;
import com.oy.tv.schema.core.VariationBO;
import com.oy.tv.util.MementoManager;
import com.oy.tv.util.SmartCacheMonitor;
import com.oy.tv.util.UtilMD5;
import com.oy.tv.util.XmlUtil;
import com.oy.tv.wdgt.ActionDispatcher;
import com.oy.tv.wdgt.model.UserIdentity;
import com.oy.tv.wdgt.model.ViewModel;

public class MainView extends BaseWebView {
	
	private static SmartCacheMonitor monitor = new SmartCacheMonitor(
		MainView.class, "MEMENTOS", 
		"This monitor reports Mementos cache."
	); 
	
	public static final int MAX_HISTORY_SIZE = 100;
	private static int MAX_REFERER_LENGTH = 255;
	
	private static SecureRandom srnd = new SecureRandom();
		
	class ChallengeAction extends BaseAction {
		int inGradeId;
		int inUnitId;  
		String inReferer;
		String inLocale;
		String inMemento;       
		
		public void execute(IPropertyProvider provider){
			// try to restore memento
			if (inMemento != null && inMemento.length() != 0){
				model = (ViewModel) MementoManager.decode(inMemento);	
 			}    
						    
			// if memento is not available, init new model from request parameters, if provided
			if (model == null){   
				String activityId = 
					UtilMD5.string2md5HMA(
						"key", new Date().getTime() + ":" + Math.abs( srnd.nextInt())
					).toUpperCase(); 
				model = new ViewModel(activityId);				  

				// override model with parameters passed in
				model.referer = inReferer;
				if (model.getReferer() != null && model.getReferer().length() > MAX_REFERER_LENGTH){
					model.referer = model.getReferer().substring(0, MAX_REFERER_LENGTH);
				}  			
				model.gradeId = inGradeId;
				model.unitId = inUnitId;
				
				String locale = ctx.getCookieValue(ActionDispatcher.COOKIE_LANGUAGE_PREFERENCE);
				if (inLocale != null) {
					locale = inLocale;
				}
				model.locale = TranslationDAO.formatLocale(locale);
			} 

			// pick random unit for the grade
			boolean mustRepeatUnit = provider.hasPropertyValue("inRepeat"); 
			if (mustRepeatUnit){
				model.failCount = 0;
			}
			if (model.gradeId != 0 && !mustRepeatUnit){
				model.unitId = getNextUnitId(ctx.getDb(), model);
				model.failCount = 0;				
			}
						
			if (model.unitId == 0){
				int DEFAULT_UNIT_ID = 2;
				model.unitId = DEFAULT_UNIT_ID;
			}
			updateUnitHistory(model);
			
			// get unit, new seed & get random variation
			{
				model.seed = srnd.nextInt();
				UnitBO unit = UnitCache.getUnit(ctx.getDb(), model.getUnitId());
				unitDescription = unit.getDesc();
				model.variationId = getVariation(unit);
				uctx = expandAndShuffle(ctx.getDb(), model, pp);
			} 
			
			type = ViewType.CHALLENGE;
		}  
		
		private int getNextUnitId(AnyDB db, ViewModel model) {
			try {
				List<UnitBO> units = UnitDAO.loadUnitsOldestLast(db, model.gradeId, model.locale);
				List<Option> options = getNextUnitOptions(units, model.unitIdHistory);
				if (options.size() != 0){
					Collections.shuffle(options);
					return options.get(0).unitId;
				} else {
					return 0;
				}
			} catch (UserMessage um){
				throw um;
			} catch (Exception e){
				throw new RuntimeException(e);
			}
		}
		
		public int hashCode(){
			return 0;
		}
	};  
	ChallengeAction m_Challenge = new ChallengeAction();
	
	class ResponseAction extends BaseAction {
		String inMemento;
		boolean correct;
		
		public void execute(IPropertyProvider provider){ 
			
			// check memento is ok
			if (inMemento == null || inMemento.length() == 0){
				m_Challenge.execute(provider);
				return;
			}
			model = (ViewModel) MementoManager.decode(inMemento);
			
			// determine the index of vote
			String vote = null;
			int voteIndex = -1;				 
			for (int i = 0; i < UnitProcessor.LETTERS.length; i++){
				vote = ctx.getParameter("v" + i);
				if (vote != null){
					voteIndex = i;
					break;
				}
			}
			
			// check submission
			UnitContext uctx = expandAndShuffle(ctx.getDb(), model, pp);
			correct = uctx.answerIndexes.contains(voteIndex);

			// unit data
			final String unitData; {
  			StringBuffer sb = new StringBuffer();
  			sb.append("seed={" + model.seed + "}\n");
  			sb.append("vote={" + vote + "}\n"); 
  			sb.append("index={" + voteIndex + "}\n"); 
  			boolean has = false;
  			sb.append("answers={");
  			for (int index : uctx.answerIndexes){
  				if (has){
  					sb.append(index + ", ");									
  				}
  				has = true;
  				sb.append(index);				
  			}
  			sb.append("}\n");
  			sb.append("correct={" + ((correct) ? "1" : "0") + "}");
  			unitData = sb.toString();
			}
			
			// record result
			ResponseDAO.Score score;
			model.tryCount++;
			if (voteIndex == -1) {
				model.failCount = 0;
				model.passCount = 0;
				model.skipCount++;
				score = ResponseDAO.Score.SKIP;
			} else {
  			if (correct){
  				model.failCount = 0;
  				model.skipCount = 0;
  				model.passCount++;
  				score = ResponseDAO.Score.PASS;
  			} else {
  				model.passCount = 0;
  				model.skipCount = 0;
  				model.failCount++;
  				score = ResponseDAO.Score.FAIL;
  			}
			}
			recordResponse(score, unitData);  
			
			// choose view
			if (score == ResponseDAO.Score.SKIP) {
				m_Challenge.execute(provider);
			} else {
				type = ViewType.RESPONSE;
			}
		}  
   		  
		public int hashCode(){
			return 1;
		}
	};  
	ResponseAction m_Response = new ResponseAction();

	class RetryAction extends BaseAction {
		String inMemento;
		
		public void execute(IPropertyProvider provider){
			
			// check memento
			if (inMemento == null || inMemento.length() == 0){
				model = null;
				m_Challenge.execute(provider);
				return;
			} 
  			
			// decode
			model = (ViewModel) MementoManager.decode(inMemento);
			model.seed = srnd.nextInt();  
			
			// re-shuffle
			uctx = expandAndShuffle(ctx.getDb(), model, pp);	
			
			// choose view
			type = ViewType.CHALLENGE;
		}
 		
		public int hashCode(){
			return 2;
		}
	}
	RetryAction m_Retry = new RetryAction();
	
	enum ViewType {CHALLENGE, RESPONSE};
	
	private ViewType type = ViewType.CHALLENGE;
	ViewModel model;
	UnitContext uctx;
	UserIdentity ui;  
	String unitDescription;
	private IPropertyProvider pp;
	
	public MainView(BaseViewCtx ctx, UserIdentity ui, IPropertyProvider pp){
		super(ctx);
		
		this.pp = pp;
		this.ui = ui;
	}
	
	private static String getHint(ViewModel model, UserIdentity ui){
		if (ui.userId == 1000){
			StringBuffer sb = new StringBuffer();

			sb.append("hash:" + model.hashCode() + "; ");
			sb.append("referer:" + model.referer + "; ");
			sb.append("createdOn:" + model.createdOn + "; ");
			sb.append("activityId:" + model.activityId + "; ");
			sb.append("userId:" + ui.userId + "; ");
			sb.append("\n");
			sb.append("failCount:" + model.failCount + "; ");
			sb.append("passCount:" + model.passCount + "; ");
			sb.append("\n");
			sb.append("gradeId:" + model.gradeId + "; ");
			sb.append("unitId:" + model.unitId + "; ");
			sb.append("variationId:" + model.variationId + "; ");
			sb.append("history:{");
			
			for (int unitId : model.getUnitIdHistory()){
				sb.append(unitId + "; ");
			}
			sb.append("}");
			return sb.toString();
		} 
		return "";
	}
	         
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		if (ctx.hasLastError()){
			if (ctx.getLastError() instanceof UserMessage){
				UserMessage msg = (UserMessage) ctx.getLastError();
				UnitRenderer.renderMessage(out, msg.getMessage());	
			} else {  
				throw new RuntimeException(ctx.getLastError());
			}
		} else {
			if (model == null){
				UnitRenderer.renderMessage(out, "Nothing to display.");
			} else {

				// referrer
				String referrer = null;
				if (model != null){
					referrer = model.referer;
				}
				
				// serialize
				model.createdOn = new Date().getTime();
				String memento = MementoManager.encode(model);
				  				
				// ctx
				String ctx = "{unit_id:" + model.getUnitId() + ";variation_id:" + model.getVariationId() + "}";
				
				// render
				boolean hasUnitVariety = model.getGradeId() != 0;
				UnitRenderer ur = new UnitRenderer(this);
				switch(type){
					case CHALLENGE:
						ur.renderChallenge(dispatcher, out, memento, unitDescription, getHint(model, ui), 
								referrer, ctx);
						break;
					case RESPONSE:
						ur.renderResponse(dispatcher, out, memento, getHint(model, ui), referrer, ctx,
								model.getUnitId(), m_Response.correct, model.getPassCount(), model.getFailCount(),
								hasUnitVariety, model.locale);
						break;
					default: throw new RuntimeException("Bad view type:" + type);
				}
			}
		}  		
	}
		
	BaseViewCtx ctx(){
		return ctx;
	}
 	    
	private void recordResponse(ResponseDAO.Score score, String unitData){  
		try {   
			UserBO usr = new UserBO();
			usr.setId(ui.userId);
			   
			VariationBO var = new VariationBO();
			var.setId(model.getVariationId());
			
			UnitBO uni = new UnitBO();
			uni.setId(model.getUnitId());
			        
			String sessionId = ui.sessionId;
			if (sessionId != null){
				sessionId = sessionId.toUpperCase();  
			}
			
			ResponseDAO.enqueue(  
				getDb(), usr.getId(), uni.getId(), var.getId(), new Date(model.getCreatedOn()), new Date(), score, 
				model.getReferer(), ui.clientAddress, ui.clientAgent, sessionId, model.getActivityId(),
				unitData, model.locale
			);    
			
			UserUnitResponseDAO.newWorkAvailable();
		} catch (Exception e){
			System.out.println("Failed to record response.");
			e.printStackTrace();
		}
	}	
	
	private int getVariation(UnitBO unit) {
		try {
			VariationBO var = VariationDAO.loadRndVariation(ctx.getDb(), unit);
			if (var == null){    
				throw new UserMessage("Item has no variations.");
			}
			return var.getId();
		} catch (UserMessage um){
			throw um;
		} catch (Exception e){
			throw new RuntimeException(e);
		}
	}  
	
	private static UnitContext expandNow(AnyDB db, ViewModel model, String xml, IPropertyProvider pp) {
		YacasEvaluatorEx eval = YacasEvaluatorEx.leaseBegin();
		UnitProcessor up = new UnitProcessor();
		
		UnitContext ctx = new UnitContext();
		try {
			VariationBO var = VariationDAO.loadVariation(db, model.variationId);
			
			up._doc = XmlUtil.loadXmlFrom(xml);
			up._pp = pp;
			up._ctx = ctx;
			up._ctx.values = var.getValues();
			up._log = System.out;
			up._eval = eval;
			
			up._eval.setLocale(model.locale);			
			up.evaluate();  
			       
			YacasEvaluatorEx.leaseComplete(eval);
		} catch (Exception e){
			e.printStackTrace();
			YacasEvaluatorEx.leaseFail(eval);
			throw new RuntimeException(
				"Failed to render unit: " + model.unitId + ", variation: " + model.variationId, e
			);
		}  
		return ctx;
	}
	
	private static UnitContext expandAndShuffle(AnyDB db, ViewModel model, IPropertyProvider pp) {
		UnitContext ctx;
		try {
			String key = MainView.class.getName() + ":" + model.locale + ":" + 
					model.unitId + ":" + model.variationId;
			
			ObjectCacheBO item = ObjectCacheDAO.get(db, key);
			if (item != null){  
				ctx = (UnitContext) MementoManager.decode(item.getValue());
			} else {
				UnitBO unit = UnitDAO.loadUnit(db, model.unitId);
				try {
  				TranslationBO tln = TranslationDAO.getTranslationFor(db, model.locale, 
  						model.getUnitId());
  				if (tln != null) {
  					TranslationContext tctx = TranslationWeaver.extractTranslatable(unit);
  	  			TranslationContext.updateFromXml(tctx, tln.getData());  			
  					unit = TranslationWeaver.weaveTranslatable(unit, tctx);
  				}
				} catch (Exception e) {
					System.out.println("Failed to translate unit '" + model.unitId + 
							"' into '" + model.locale + "' on" + new Date());
					e.printStackTrace();
				}
				ctx = expandNow(db, model, unit.getXml(), pp);
				ObjectCacheDAO.put(db, key, MementoManager.encode(ctx), new Date());
			}
		} catch (Exception e){ 
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		Map<Integer, Integer> new2old = new HashMap<Integer, Integer>();
		UnitContext.shuffleEx(ctx, new2old, new Random(model.seed));
  
		return ctx;
	}
	
	public static void updateUnitHistory(ViewModel model) {
		model.unitIdHistory = appendWithLimit(
				model.unitIdHistory, model.unitId, MAX_HISTORY_SIZE, new HashSet<Integer>());
	}
	
	public static int [] appendWithLimit(int [] array, int value, int max_size, Set<Integer> all) {
		List<Integer> input = new ArrayList<Integer>();
		if (array != null) {
  		for (int i : array){
  			input.add(i);
  		}
		}
		
		input.add(0, value);
		while (input.size() > max_size){
			input.remove(input.size() - 1);
		}
		
		int [] output = new int [input.size()];
		for (int i=0; i < output.length; i++){
			output[i] = input.get(i);
			all.add(input.get(i));
		}
		
		return output;
	}
	
	/**
	 * Select the best next unit from the possible options, which
	 * has not been seen by the client longest. 
	 */
	public static List<Option> getNextUnitOptions(List<UnitBO> units, int [] unitIdHistory) {
		if (units.size()== 0){    
			throw new UserMessage("Grade has no challenges.");
		}

		// init unit history
		if (unitIdHistory == null){
			unitIdHistory = new int [] {};
		}
				
		// map history (unitId to round)
		Map<Integer, Integer> historyMap = new HashMap<Integer, Integer>();
		for (int i = unitIdHistory.length - 1; i >= 0; i --){
			historyMap.put(unitIdHistory[i], i);
		}
				
		// new options
		List<Option> options = new ArrayList<Option>();
		for (UnitBO unit : units){
			Option o = new Option();
			o.unitId = unit.getId();
			if (historyMap.containsKey(unit.getId())){
				o.round = historyMap.get(unit.getId());					
			} else {
  			o.round = Integer.MAX_VALUE;
			}
			options.add(o);
		}
		
		// select winners
		List<Option> winners = new ArrayList<Option>();
		int maxRound = Integer.MIN_VALUE;
		for(Option o : options){
			if (maxRound < o.round){
				winners.clear();
				maxRound = o.round;
			}
			if (maxRound == o.round){
				winners.add(o);
			}
		}		
		
		return winners;
	}
	
	public static IPerfMonitor getMonitor(){
		return monitor;
	}
	
}

class Option {
	int unitId;
	int round;
	
	@Override
	public String toString(){
		return "" + unitId + "@" + round;
	}
}