package com.oy.tv.vocb.view;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.model.vocb.WordSet;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.wdgt.ActionDispatcher;
import com.oy.tv.wdgt.model.UserIdentity;
import com.oy.tv.wdgt.model.UserIdentity.Origin;
import com.oy.tv.wdgt.view.UnitRenderer;

public class VocabRenderer {

	private static final Random srnd = new SecureRandom();
	
	private VocabView view;
	
	public VocabRenderer(VocabView view){
		this.view = view;
	}
	
	UserIdentity ui(){ 
		return view.ui; 
	}
	
	public void renderChallenge(IActionDispatchEncoder dispatcher, Writer out, int unitId, 
			String memento, String hint, String referrer, String feedbackCtx, boolean isInverted,
			int correct, int incorrect) throws IOException {
		
		final int uid = srnd.nextInt(10 * 1000);
		final String varUnit = "test" + uid;
		
		final String onload; {
  		String varClock = "clock" + uid;
  		String startClock = 
  			"onload='" + 
  			"var " + varClock + "=new ItyStopwatch(\"tbrClock\"); " +
  			varClock + ".start(); " + 
  			varUnit + ".beforeSubmit = function() { " + varClock + ".stop(); };" + 
  			"'";
  		boolean hasTimer = ui() != null && ui().origin == Origin.APP;  
  		onload = (hasTimer ? startClock : "");
		}		
		
		UnitRenderer.renderBeginEx(
				out, ResourceLocator.getVocbCSSHref(), 
				view.vctx.ws.getName() + " Vocabulary", null, hint, onload,
		    "<script type='text/javascript' src='//www.itestyou.com/js/md5.js' ></script>",
		    "<script type='text/javascript' src='//www.itestyou.com/js/vocb.normal.2.js' ></script>");

		if (ui() != null){
			if (ui().origin != Origin.APP){
				UnitRenderer.renderToolbar(ui().isLoggedIn(), out, referrer);
			} else {
				UnitRenderer.renderActiveMobileToolbar(
						out, unitId, ui().sessionId, feedbackCtx, referrer, correct, incorrect);
			}
		}	
				
		out.write("<div class='container'>\n");
		out.write("<div class='vocb'>\n");

		{
  		out.write("<form id='main' action='vocb' method='POST'>\n"); 
  		out.write("<input type='hidden' name='action_id' value='1' />\n");
  		if (ui() != null && ui().origin == Origin.APP){
  			out.write("<input type='hidden' name='app_session' value='" + ui().sessionId + "'>\n");
  		}
  		out.write("<input type='hidden' name='inMemento' value='" + memento + "' />\n");
  		out.write("<input type='hidden' name='inVote' value='' />\n");
  		
  		if (isInverted){
  			out.write("<div class='word'>" + HtmlUtil.escapeHTML(view.vctx.word.getDefinition()) + "</div>\n");				
  		} else {
  			out.write("<div class='word'>" + HtmlUtil.escapeHTML(view.vctx.word.getWord()) + "</div>\n");
  		}
  		
    	out.write("<script type='text/javascript'>\n");
    	out.write("var " + varUnit + " = new ItyVocabularyTest('" + md5("" + view.vctx.answerIdx) + "');\n");
    	out.write("</script>\n");
  		
  		out.write("<ul>\n");  
  		for (int i=0; i < view.vctx.decoys.length; i++){
  			String caption;
  			if (isInverted){
  				caption = view.vctx.decoys[i].getWord();
  			} else {
  				caption = view.vctx.decoys[i].getDefinition();					
  			}
  			
  			out.write("<li style='vote'>");
  			out.write(  
  					"<input " + 
  					"id='vote" + i + "' " +   
  					"class='vote' " + 
  					"type='submit' " + 
  					"name='v" + i + "' " + 
  					"value='" + encodeDefinition(caption) + "' " + 					
  					"onClick='return " + varUnit + ".castVote(" + i + "); " + 
  					"'/>\n");
  			out.write("</li>\n");  
  		}  
  		out.write("</ul>\n");
  		
  		out.write("</form>\n");  	
		}

		out.write("</div>\n");
		out.write("</div>\n");

		// ads
		if (ui() != null && ui().origin == Origin.APP && !ui().isPro){
			ActionDispatcher.renderVocbAdMobile(out);
		}
		
		UnitRenderer.renderEnd(out);
	}
	
	public void renderIntrasticial(IActionDispatchEncoder dispatcher, Writer out, int unitId, 
			String memento, String hint, String referrer, String feedbackCtx,
			int correct, int incorrect, String content, WordSet ws, String links) throws IOException {
		
		UnitRenderer.renderBeginEx(
				out, ResourceLocator.getVocbCSSHref(), 
				ws.getName() + " Vocabulary", null, hint, "",
		    "<script type='text/javascript' src='//www.itestyou.com/js/md5.js' ></script>",
		    "<script type='text/javascript' src='//www.itestyou.com/js/vocb.normal.2.js' ></script>");

		if (ui() != null){
			if (ui().origin != Origin.APP){
				UnitRenderer.renderToolbar(ui().isLoggedIn(), out, referrer);
			} else {
				UnitRenderer.renderActiveMobileToolbar(
						out, unitId, ui().sessionId, feedbackCtx, referrer, correct, incorrect);
			}
		}	
				
		out.write("<div class='container'>\n");
		out.write("<div class='vocb'>\n");

		{
  		out.write("<form id='main' action='vocb' method='POST'>\n"); 
  		out.write("<input type='hidden' name='action_id' value='0' />\n");
  		if (ui() != null && ui().origin == Origin.APP){
  			out.write("<input type='hidden' name='app_session' value='" + ui().sessionId + "'>\n");
  		}
  		out.write("<input type='hidden' name='inMemento' value='" + memento + "' />\n");
  		
  		out.write("<div id='vocb_intra'>");
  		out.write(content);
  		
  		if (links != null && links.trim().length() != 0) {
  			out.write(links);
  			out.write("&nbsp;&nbsp;");
  		}
  		
  		out.write("<input type='submit' name='' value='Continue' />");
  		out.write("</div>");
  		
  		out.write("</form>\n");  	
		}

		out.write("</div>\n");
		out.write("</div>\n");

		// ads
		if (ui() != null && ui().origin == Origin.APP && !ui().isPro){
			ActionDispatcher.renderVocbAdMobile(out);
		}
		
		UnitRenderer.renderEnd(out);
	}
	
	public static String md5(String text){
		try {
			byte[] bytesOfMessage = text.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfMessage);			
			return new BigInteger(1, digest).toString(16);
		} catch (Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}
	
	public static String encodeDefinition(String definition){
		return HtmlUtil.escapeHTML(definition).replaceAll("'", "&#39;");
	}
	
}
