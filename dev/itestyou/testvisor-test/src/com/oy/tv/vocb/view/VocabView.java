package com.oy.tv.vocb.view;

import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.IPropertyProvider;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.BaseWebView;
import com.oy.tv.app.UserMessage;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.dao.local.UnitCache;
import com.oy.tv.dao.runtime.QueueProcessorThread;
import com.oy.tv.dao.runtime.ResponseDAO;
import com.oy.tv.dao.runtime.UserUnitResponseDAO;
import com.oy.tv.model.learn.ProgressCalculator;
import com.oy.tv.model.learn.ProgressCalculator.RangeContainer;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.util.MementoManager;
import com.oy.tv.util.UtilMD5;
import com.oy.tv.vocb.model.VocabViewModel;
import com.oy.tv.wdgt.model.UserIdentity;
import com.oy.tv.wdgt.view.UnitRenderer;

public class VocabView extends BaseWebView {

	public static int VOCB_ITEMS_PER_LEVEL = 10;
	private static int MAX_REFERER_LENGTH = 255;
	public static final int DEFAULT_WORD_SET_ID = 115;	// reserved ID
	
	static Random rand = new SecureRandom();
	
	class SessionStartAction extends BaseAction {
		String inMemento;       
		String inReferer;
		int inUnitId;
		int inMode;
		
		private void initModel(){
			// if memento is not available, init new model from request parameters, if provided
			String activityId = 
				UtilMD5.string2md5HMA(
					"key", new Date().getTime() + ":" + Math.abs(rand.nextInt())
				).toUpperCase(); 
			model = new VocabViewModel(activityId);				  

			// override model with parameters passed in
			model.referer = inReferer;
			if (model.getReferer() != null && model.getReferer().length() > MAX_REFERER_LENGTH){
				model.referer = model.getReferer().substring(0, MAX_REFERER_LENGTH);
			}  			
			model.unitId = inUnitId;
			model.mode = inMode;
			
			// init seed and default unit
			if (model.unitId == 0){
				model.unitId = DEFAULT_WORD_SET_ID;
			}
			model.seed = rand.nextInt();
		}
		
		public void execute(IPropertyProvider provider){
			if (inMemento != null && inMemento.length() != 0){
				model = (VocabViewModel) MementoManager.decode(inMemento);	
 			} else {
 				initModel();
 			}
			view = new ChallengeView(VocabView.this);
		}
	
		public int hashCode(){
			return 0;  
		}

	};  
	SessionStartAction m_Challenge = new SessionStartAction();
	
	class NextStepAction extends BaseAction {
		String inMemento;       
		String inVote;
		
		public void execute(IPropertyProvider provider){
			if (inMemento == null || inMemento.length() == 0){
				m_Challenge.execute(provider);
			} else {
  			model = (VocabViewModel) MementoManager.decode(inMemento);
  			Result result = recordResponse(model, inVote);
  			if (!hasIntrasticialView(result)) {
    			view = new ChallengeView(VocabView.this);				
  			}			
			}
		}

		boolean hasIntrasticialView(Result result) {
			// if result did not pass, we could have not reached new level
			if (result.score != ResponseDAO.Score.PASS) {
				return false;
			}
			
			try {
  			WordSet ws = UnitCache.getWordSet(ctx.getDb(), model.getUnitId());
  			
  			// use different logic for logged and anonymous users
  			boolean has;
  			if (ui.isLoggedIn()) {
  				has = hasIntrasticialViewForLoggedId(result, ws);
  			} else {
  				has = hasIntrasticialViewForAnonymous(result, ws);
  			}
  			
  			if (has) {
  				model.intrstclIndex++;
  			}
  			
  			return has;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		boolean hasIntrasticialViewForAnonymous(Result result, WordSet ws) throws IOException {
			// check if we hit new level based on number of right answers
			int level = model.correct / VOCB_ITEMS_PER_LEVEL;
			int remainder = model.correct % VOCB_ITEMS_PER_LEVEL;
			if (remainder == 0) {
				view = chooseIntrasticial(model.correct, level, ws, " ");				
				return true;
			}
			return false;
		}
				
		boolean hasIntrasticialViewForLoggedId(Result result, WordSet ws) 
				throws SQLException, IOException {
			// new results queued; estimate if we will hit new level with this result 
			// get all distinct values for current unit and check if this one is new
			RangeContainer rc = QueueProcessorThread.getRangeContainerFor(
					ctx.getDb(), ui.userId, model.getUnitId());
			if (rc != null) { 
  			boolean isNewAchievent = !rc.contains(model.getVariationId());
  			if (isNewAchievent) {
    			// check if we will hit new level when this score is recorded
  				int total_distinct = rc.size() + 1;
    			int level = total_distinct / VOCB_ITEMS_PER_LEVEL;
    			int remainder = total_distinct % VOCB_ITEMS_PER_LEVEL;  			
    			if (remainder == 0) {
    				view = chooseIntrasticial(total_distinct, level, ws, "");				
    				return true;
    			}  				
  			}
			}
			return false;
		}
		
		public int hashCode(){
			return 1;
		}
	};  
	NextStepAction m_NextStep = new NextStepAction();
		
	View view;
	String hint = "";
	VocabViewModel model;
	UserIdentity ui;  
	VocabViewContext vctx;
	IPropertyProvider pp;
	
	public void newChallenge() {
		model.lastVariationId = model.getVariationId();
		IChallengeSelector selector = IChallengeSelector.Factory.newChallengeSelector(
				rand, ctx.getDb(), ui.userId);
		vctx = selector.newChallenge(model.unitId, model.getMode(), model.lastVariationId);
		model.wordIds = vctx.decoyIdxs;
		model.answerIdx = vctx.answerIdx;
		model.inv = vctx.inv;
	}
	
	public static Result getResult(VocabViewModel model, String inVote){
		// parse responses
		int [] answers;
		int [] wordIds;
		{			
			if (inVote == null){
				inVote = "";
			}
			String [] votes = inVote.split(";");
			answers = new int [votes.length];
			wordIds = new int [votes.length];
			for (int i=0; i < votes.length; i++){
				answers[i] = Integer.parseInt(votes[i]);
				wordIds[i] = model.wordIds[answers[i]];
			}
		}
		
		// classify	
		boolean pass = answers.length == 1 && answers[answers.length - 1] == model.answerIdx;
		Result result = new Result();
		if(pass){
			result.score = ResponseDAO.Score.PASS;  				
			result.fail = new int [] {};
		} else {
			result.score = ResponseDAO.Score.FAIL;
			result.fail = wordIds;
		}
					
		return result;
	}
	
	public VocabView(BaseViewCtx ctx, UserIdentity ui, IPropertyProvider pp){
		super(ctx);
		this.pp = pp;
		this.ui = ui;
	}
	
	private Result recordResponse(VocabViewModel model, String inVote){
		// parse response
		Result result;
		try {
			result = getResult(model, inVote);
		} catch (NumberFormatException nfe) {
			System.err.println("Failed to parse vocb vote: " + inVote);
			
			result = new Result();
			result.score = ResponseDAO.Score.FAIL;  				
			result.fail = new int [] {};
		}

		// update model
		if (result.score == ResponseDAO.Score.PASS){
			model.correct++;
		} else {
			model.incorrect += result.fail.length;				
		}
		
		// record
		try {   			
			String sessionId = ui.sessionId;
			if (sessionId != null){
				sessionId = sessionId.toUpperCase();  
			}
			
			ResponseDAO.enqueue(  
				getDb(), ui.userId, model.getUnitId(), model.getVariationId(), new Date(model.getCreatedOn()), new Date(), result.score, 
				model.getReferer(), ui.clientAddress, ui.clientAgent, sessionId, model.getActivityId(),
				createUnitData(model.getVariationId(), model.isInverted(), result), TranslationDAO.LANG_EN
			);    

			UserUnitResponseDAO.newWorkAvailable();
		} catch (Exception e){
			System.out.println("Failed to record response.");
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 *		ep		// did convert from expression to meaning
	 *		ef		// failed to convert from expression to meaning
	 *		mp		// did convert from meaning to expression
	 *		mf		// failed to convert from meaning to expression
	 *
	 */
	public static String createUnitData(int variationId, boolean inverted, Result result){
		StringBuffer sb = new StringBuffer();

		if (result.score == ResponseDAO.Score.PASS){
			sb.append("ep=");
			sb.append(join(new int []{variationId}));
			sb.append("\n");

			sb.append("mp=");
			sb.append(join(new int []{variationId}));
		} else {
  		if (inverted){
  			if (result.score == ResponseDAO.Score.FAIL){
  				sb.append("mf=");
  				sb.append(join(new int []{variationId}));
  				sb.append("\n");
  
  				sb.append("ef=");
  				sb.append(join(result.fail));
  			}
  		} else {
  			if (result.score == ResponseDAO.Score.FAIL){
  				sb.append("ef=");
  				sb.append(join(new int []{variationId}));
  				sb.append("\n");
  
  				sb.append("mf=");
  				sb.append(join(result.fail));
  			}			
  		}
		}
		return sb.toString();
	}
	
	private static String join(int [] items){
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i=0; i < items.length; i++){
			if (i != 0){
				sb.append(",");
			}
			sb.append(items[i]);
		}
		sb.append("}");
		return sb.toString();
	}
		
	public static List<Integer> getWordsToLearn(String unitData){
		List<Integer> ids = new ArrayList<Integer>();
		Map<Integer, Integer> toLearn = ProgressCalculator.parseUnitData(unitData);
		for (int id : toLearn.keySet()){
			int count = toLearn.get(id);
			if (count <= 0){
				ids.add(id);
			}
		}  			
		Collections.sort(ids);
		return ids;
	}
	
	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
		if (view == null) {
			UnitRenderer.renderMessage(out, "Nothing to display.");
		} else { 
			view.render(dispatcher, out);
		}
	}

	interface View {
  	public void render(IActionDispatchEncoder dispatcher, Writer out)
    	throws IOException;
	}
	
	class IntrasticialView implements View {
		VocabView parent;
		String content;
		WordSet ws;
		String links;
		
		IntrasticialView(VocabView parent, String content, WordSet ws, String links) {
			this.parent = parent;
			this.content = content;
			this.ws = ws;
			this.links= links;
		}
		
  	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
  		if (ctx.hasLastError()){
  			renderError(out);
  		} else {
  			if (model == null){
  				UnitRenderer.renderMessage(out, "Nothing to display.");
  			} else {
  				VocabRenderer renderer = new VocabRenderer(parent);
  				renderer.renderIntrasticial(
  						dispatcher, out, model.unitId, getMemento(), getHint(model, ui, hint), 
  						getReferrer(), getFeedbackCtx(), model.correct, model.incorrect, content, 
  						ws, links);
  			}
  		}
  	}
	}
	
	class ChallengeView implements View {
		VocabView parent;
		
		ChallengeView(VocabView parent) {
			this.parent = parent;
			newChallenge();
		}
		
  	public void render(IActionDispatchEncoder dispatcher, Writer out) throws IOException {
  		if (ctx.hasLastError()){
  			renderError(out);
  		} else {
  			if (model == null){
  				UnitRenderer.renderMessage(out, "Nothing to display.");
  			} else {
  				VocabRenderer renderer = new VocabRenderer(parent);
  				renderer.renderChallenge(
  						dispatcher, out, model.unitId, getMemento(), getHint(model, ui, hint), 
  						getReferrer(), getFeedbackCtx(), model.isInverted(), 
  						model.correct, model.incorrect);
  			}
  		}
    }
	}
	
	private void renderError(Writer out) throws IOException {
		if (ctx.getLastError() instanceof UserMessage){
			UserMessage msg = (UserMessage) ctx.getLastError();
			UnitRenderer.renderMessage(out, msg.getMessage());	
		} else {  
			throw new RuntimeException(ctx.getLastError());
		}
	}

	private String getFeedbackCtx() {
		return 
			"{unit_id:" + model.getUnitId() + ";" + 
			"variation_id:" + model.getVariationId() + "}";
	}
	
	private String getMemento() {
		model.createdOn = new Date().getTime();
		return MementoManager.encode(model);
	}
	
	private String getReferrer() {
		String referrer = null;
		if (model != null){
			referrer = model.referer;
		}
		return referrer;
	}
  	
	private static String getHint(VocabViewModel model, UserIdentity ui, String hint){
		if (ui.userId == 1000){
			StringBuffer sb = new StringBuffer();

			sb.append("hash:" + model.hashCode() + "; ");
			sb.append("referer:" + model.referer + "; ");
			sb.append("createdOn:" + model.createdOn + "; ");
			sb.append("activityId:" + model.activityId + "; ");
			sb.append("userId:" + ui.userId + "; ");
			sb.append("unitId:" + model.unitId + "; ");
			sb.append("variationId:" + model.getVariationId() + "; ");
			sb.append("lastVariationId:" + model.lastVariationId + "; ");
			
			sb.append("answerIdx:" + model.answerIdx + "; ");
			sb.append("mode:" + model.mode + "; ");
			sb.append("inv:" + (model.inv ? "true" : "false") + ";\n");
			
			sb.append("wordIds: {");
			for (int unitId : model.wordIds){
				sb.append(unitId + "; ");
			}
			sb.append("}\n");
			
			if (hint != null){
				sb.append(hint);
			}
			
			return sb.toString();
		} 
		return "";
	}
	
	View makeIntrasticial(String text1, String text2, WordSet ws) throws IOException {
		return new IntrasticialView(VocabView.this, 
				"<span id='vocb_intra_1'>" + text1 + "</span>" + 
				"<span id='vocb_intra_2'>" + text2 + "</span>", 
				ws, UnitRenderer.getShareToolbarLinks());			
	}
	
	View makeIntrasticial(int level, String text, WordSet ws) throws IOException {
		return makeIntrasticial(
				"You have reached level <b>" + level + "</b>!", 
				text, ws);			
	}
	
	View chooseIntrasticial(int total_distinct, int level, WordSet ws, String text) throws IOException {
		int OPTION_COUNT = 4;
		
		if (model.intrstclIndex % OPTION_COUNT == 0) {
			return makeIntrasticial(level, 
					"Next level is just 10 <i>new</i> solved problems away." + text, ws);
		}
		
		if (model.intrstclIndex % OPTION_COUNT == 1) {
			return makeIntrasticial(level, 
					"You know " + total_distinct + " of " + ws.getWords().size()+ 
					" words in this vocabulary." + text, ws);
		}

		if (model.intrstclIndex % OPTION_COUNT == 2) {
			return makeIntrasticial(level, 
					"You have solved " + (model.correct + model.incorrect) + 
					" problems and got " + model.correct + " right!" + text, ws); 
		}

		if (model.intrstclIndex % OPTION_COUNT == 3) {
			NumberFormat format = new DecimalFormat("##0.0");
			double done = (100 * total_distinct) / ((double) ws.getWords().size());
			return makeIntrasticial(level, 
					"You are now done with " + format.format(done) + 
					"% of words you plan to learn." + text, ws); 
		}
			
		return null;
	}
}