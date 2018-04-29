package com.oy.tv.wdgt.view;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;

import com.oy.shared.hmvc.IActionDispatchEncoder;
import com.oy.shared.hmvc.impl.BaseAction;
import com.oy.shared.hmvc.util.HtmlUtil;
import com.oy.tv.app.BaseViewCtx;
import com.oy.tv.app.view.StepView;
import com.oy.tv.dao.core.TranslationDAO;
import com.oy.tv.model.unit.RenderContext;
import com.oy.tv.model.unit.UnitContext;
import com.oy.tv.model.unit.UnitProcessor;
import com.oy.tv.ns.ResourceLocator;
import com.oy.tv.wdgt.ActionDispatcher;
import com.oy.tv.wdgt.model.UserIdentity;
import com.oy.tv.wdgt.model.UserIdentity.Origin;

public class UnitRenderer {

	private final int MAX_FAIL_BEFORE_NEXT = 2;
	
	private MainView main;
	
	public UnitRenderer(MainView main){
		this.main = main;
	}
	
	BaseViewCtx ctx(){ return main.ctx(); }
	UnitContext uctx (){ return main.uctx; };
	UserIdentity ui(){ return main.ui; }

	BaseAction m_Response(){ return main.m_Response; }
	BaseAction m_Retry(){ return main.m_Retry; }
	BaseAction m_Challenge(){ return main.m_Challenge; }

	public static void renderBegin(Writer out, String relCssHref, String caption, String desc, 
			String hint, String... ex) throws IOException {
		renderBeginEx(out, relCssHref, caption, desc, hint, "", ex);
	}
	
	public static void renderBeginEx(Writer out, String relCssHref, String caption, String desc, 
			String hint, String bodyEx, String... ex) throws IOException {
		out.write("<html><head>\n");		  
		 
		out.write("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\n");
		out.write("<meta name='ROBOTS' content='INDEX,FOLLOW,NOARCHIVE' />\n");
		out.write("<meta name='GOOGLEBOT' content='NOARCHIVE' />\n");
		
		out.write("<meta name='keywords' content='" + caption + ", I Test You, ITestYou' />\n");
		
		if (desc == null) {
			desc = "SAT vocabulary, TOEFL vocabulary, MATH worksheets, Foreign languages, Adaptive learning techniques";
		}
		out.write("<meta name='description' content='" + HtmlUtil.escapeHTML(desc) + "'>");
		
		out.write("<title>" + HtmlUtil.escapeHTML(caption) + " - I Test You</title>\n");
		
		out.write("<link rel='shortcut icon' href='//www.itestyou.com/favicon.ico' />\n");
		out.write("<link type='text/css' href='//www.itestyou.com" + relCssHref + 
				"' rel='stylesheet' />\n");		
		
		if (hint != null && hint.length() != 0){
			out.write("<!-- " + hint + " -->\n");
		}

		for (String _ex : ex){
			out.write(_ex + "\n");
		}
		
		out.write("</head><body style='padding: 0px; margin: 0px;' " + bodyEx + ">\n");			
	}
	
	private boolean isHome(String referrer){
		return 
			referrer != null && 
			(referrer.startsWith("http://www.itestyou.com/") || referrer.startsWith("https://www.itestyou.com/"));
	}	
	
	public void renderUnitToolbar(Writer out, int unitId, String referrer) throws IOException {
		renderUserName(isLoggedin(), out, !isHome(referrer), "header", referrer);
		{          
			if (!isHome(referrer)){ 
				out.write("<div class='menu' style='text-align: right;'>\n");
				out.write("<a class='atext' target='_blank' href='/practice/math/unit-" + unitId + "/test'>Practice</a>");
				out.write("&nbsp;&nbsp;&nbsp;");
				out.write("<a class='atext' target='_blank' href='/cms/week-summary'>My Score</a>");
				out.write("&nbsp;&nbsp;&nbsp;");
				out.write("<a class='atext' target='_blank' href='/cms/math-unit-" + unitId + "/share'>Feedback</a>");
				out.write("&nbsp;&nbsp;");
				out.write("</div>");
			    				
				if (ui() != null && ui().origin == Origin.WEB){
					out.write("<div class='footer'>\n");
					out.write("powered by <a class='atext' target='_blank' href='/'>www.itestyou.com</a>\n");
					out.write("</div>\n");
				}
			}   
		}		
	}
	
	private static void renderUserName(boolean isLoggedin, Writer out, boolean renderName, String cssClass, String referrer) throws IOException {
		if (!isLoggedin){   
			String returnto = "";
			if (referrer != null){
				returnto = "?returnto=" + URLEncoder.encode(referrer, "UTF-8");
			}  			
			out.write("<div class='" + cssClass + "'>\n");
			out.write("<a class='atext' target='_top' href='/cms/log-in" + 
					returnto + "'>Login</a>&nbsp;\n");
			out.write("</div>\n"); 
		} else { 
			if (renderName){
				out.write("<div class='" + cssClass + "'>\n");
				out.write("<a class='atext' target='_blank' href='/'>Home</a>&nbsp;\n");
				out.write("</div>\n");
			}
		}
	}

	private static void renderTracking(Writer out) throws IOException {
		out.write("<script type=\"text/javascript\">\n");
		out.write("var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");\n");
		out.write("document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));\n");
		out.write("</script>\n");  
		out.write("<script type=\"text/javascript\">\n");
		out.write("try {\n");
		out.write("var pageTracker = _gat._getTracker(\"UA-360750-5\");\n");
		out.write("pageTracker._trackPageview();\n");
		out.write("} catch(err) {}</script>\n");
	}
	
	public static void renderEnd(Writer out) throws IOException {
		renderTracking(out);
		out.write("</body></html>");
	}

	public static void renderMessage(Writer out, String message) throws IOException {
		renderMessage(out, message, null);
	}
	
	public static void renderMessage(Writer out, String message, String hint) throws IOException {
		renderBegin(out, ResourceLocator.getCSSHref(), "", "Message", hint);  
		
		out.write("<div class='container'>\n");
		out.write("<div class='response'>\n");
		
		out.write("<p align='center' class='warning'>" + HtmlUtil.escapeHTML(message) + "</p>");
		 
		out.write("</div>\n");
		out.write("</div>\n");
  		     
		renderEnd(out);
	}
		
	public void renderChallenge(IActionDispatchEncoder dispatcher, Writer out, 
			String memento, String desc, String hint, String referrer, String ctx) throws IOException {
		StepView.renderNoCacheHeaders(ctx());
		renderBegin(out, ResourceLocator.getCSSHref(), "Math Worksheet Challenge", desc, hint, 
				"<script type='text/javascript' src='//www.itestyou.com/js/md5.js' ></script>",
		    "<script type='text/javascript' src='//www.itestyou.com/js/vocb.normal.2.js' ></script>"		
		);	

		if (ui() != null){
			if (ui().origin != Origin.APP){
				renderToolbar(isLoggedin(), out, referrer);
			} else {
				UnitRenderer.renderMobileToolbar(out, null, ui().sessionId,  referrer, ctx);
			}
		}
				
		out.write("<div class='container'>\n");
		UnitProcessor.render(uctx(), new RenderContext(), out);
		{
  		out.write("<form action='wdgt' method='POST'>\n"); 
  		
  		out.write("<input type='hidden' name='action_id' value='" + m_Response().hashCode() + "'>\n");
  		if (ui() != null && ui().origin == Origin.APP){
  			out.write("<input type='hidden' name='app_session' value='" + ui().sessionId + "'>\n");
  		}
  		out.write("<input type='hidden' name='inMemento' value='" + memento + "'>\n");
  		for (int i=0; i < uctx().choices.size(); i++){
  			if (i != 0){  
  				out.write("&nbsp;");
  			}  
  			out.write("<input class='vote' type='submit' name='v" + i + "' value='" + UnitProcessor.LETTERS[i] + "' />\n");
  		}
  		out.write("&nbsp;");
  		out.write("<input class='vote' type='submit' name='vx' value='&gt;&gt;' />\n");
  		
  		out.write("</form>\n");
		}
		out.write("</div>");
		
		if (ui() != null && ui().origin == Origin.APP && !ui().isPro){
			ActionDispatcher.renderMathAdMobile(out);
		}
		
		renderEnd(out);			 		
	}	
	
	private boolean isLoggedin(){
		return ui() != null && ui().isLoggedIn();
	}

	public static void renderActiveMobileToolbar(Writer out, Integer unitId, String appSession,
			String feedbackCtx, String referer, int correct, int incorect) throws IOException {
		out.write("<div id='tbrMain'>");
		out.write("<div id='tbrOne'><span id='tbrPass'>" + correct + "</span><span id='tbrFail'>" + incorect + "</span></div>");
		out.write("<div id='tbrTwo'><span id='tbrClock'></span></div>");
		
		out.write("<div id='tbrThree'><span id='trbActions'>");
		
		String common = 
			"app_session=" + HtmlUtil.escapeHTML(appSession) + 
			"&inReferer=" + HtmlUtil.escapeHTML(referer);
		
		if (unitId == null){
			out.write(
					"<a href='//www.itestyou.com/view/progress?" + 
					common +
					"'>" +
					"<img border='0' src='/img/tools/my-progress.png' /></a>"
			);			
		} else {
			out.write(
					"<a href='//www.itestyou.com/view/unit/" + unitId + "?" +
					common + 
					"'>" +
					"<img border='0' src='/img/tools/my-progress.png' /></a>"
			);
		}
				
		out.write(
				"&nbsp;&nbsp;<a href='//www.itestyou.com/view/leaderboard?" + 
				common  + 
				"'>" +
				"<img border='0' src='/img/tools/leaders.png' /></a>"
		);
		
		out.write(
				"&nbsp;&nbsp;<a href='//www.itestyou.com/view/shout?" +
				common + 
				"&inCtx=" + HtmlUtil.escapeHTML(feedbackCtx) + 
				"'>" +  
				"<img border='0' src='/img/tools/shout-ex.png' /></a>"
		);
		
		out.write("</span></div>");
		
		out.write("<div style='clear: both;'></div>");
		out.write("</div>");
	}

	public static String getShareToolbarLinks() throws IOException {
		return 
			"<a href='//www.facebook.com/ITestYou' target='_blank'><img border='0' src='/img/tools/fb_btn.png' /></a>" + 
			"&nbsp;&nbsp;<a href='https://twitter.com/#!/ITestYou' target='_blank'><img border='0' src='/img/tools/twit_btn.png' /></a>" +
			"&nbsp;&nbsp;<a href='https://plus.google.com/u/0/b/100299113449260023450/' target='_blank'><img border='0' src='/img/tools/gplus_btn.png' /></a>";
	}
	
	public static void renderMobileToolbar(Writer out, Integer unitId, String appSession, String referer, String ctx) throws IOException {
		out.write("<div align='center' style='margin: 4px; padding: 4px; border-bottom: solid 1px #C0C0C0;'>");
		
		out.write(getShareToolbarLinks());
		
		String common = 
			"app_session=" + HtmlUtil.escapeHTML(appSession) + 
			"&inReferer=" + HtmlUtil.escapeHTML(referer);

		if (unitId == null){
			out.write(
					"&nbsp;&nbsp;<a href='//www.itestyou.com/view/progress?" + 
					common + 
					"'>" +
					"<img border='0' src='/img/tools/my-progress.png' /></a>"
			);			
		} else {
			out.write(
					"&nbsp;&nbsp;<a href='www.itestyou.com/view/unit/" + unitId + "?" +
					common + 
					"'>" +
					"<img border='0' src='/img/tools/my-progress.png' /></a>"
			);
		}
				
		out.write(
				"&nbsp;&nbsp;<a href='//www.itestyou.com/view/leaderboard?" + 
				common +
				"'>" +
				"<img border='0' src='/img/tools/leaders.png' /></a>"
		);
		
		out.write(
				"&nbsp;&nbsp;<a href='//www.itestyou.com/view/shout?" + 
				common +  
				"&inCtx=" + HtmlUtil.escapeHTML(ctx) + 
				"'>" +  
				"<img border='0' src='/img/tools/shout-ex.png' /></a>"
		);
		out.write("</div>");
	}
	
	public static void renderToolbar(boolean isLoggedin, Writer out, String referrer) throws IOException {
		renderUserName(isLoggedin, out, false, "bar", referrer);
	}  
    	
	public void renderResponse(
			IActionDispatchEncoder dispatcher, Writer out, String memento, 
			String hint, String referrer, String ctx, int unitId, boolean correct, int passCount, 
			int failCount, boolean hasUnitVariety, String lang) 
	throws IOException {

		// plan next action
		int action;
		boolean hasRepeat = false;
		if (correct || failCount >= MAX_FAIL_BEFORE_NEXT){
			action = m_Challenge().hashCode();
			hasRepeat = true;
		} else {
			action = m_Retry().hashCode();
		}

		// compute rendering attributes
		String cls;
		String caption = null;
		String text = null;
		String img;
		if (correct){
			cls = "correct";
			img = "<img style='margin-bottom: 24px;' src='//www.itestyou.com/img/passx.png' " + 
					"title='" + getCorrectCaption(lang)+ "'/>";
			text = getNewChallengeCaption(lang);
			
			caption = getCheer(passCount, lang);
			
			if (caption == null){
				caption = getCorrectCaption(lang);
			} else {
				img += "<img style='margin-bottom: 24px;' src='//www.itestyou.com/img/starx.png' " + 
						"title='" + caption + "'/>";
				caption = getCorrectCaption(lang) + " " + caption;
			}
		} else {       
			cls = "incorrect";
			img = "<img style='margin-bottom: 24px;' src='//www.itestyou.com/img/failx.png' " + 
					"title='" + getInCorrectCaption(lang) + "'/>";
			
			if (failCount >= MAX_FAIL_BEFORE_NEXT){
				caption = getInCorrectAgainCaption(lang);
				text = getNewChallengeCaption(lang);  
			} else {
				caption = getInCorrectCaption(lang);
				text = getTryAgainCaption(lang);
			}								
		}  			  

		// images only for app
		if (ui().origin != Origin.APP){
			img = "";
		}
		
		// reset the fail counter or it will keep triggering the next challenge
		if (failCount >= MAX_FAIL_BEFORE_NEXT){
			failCount = 0;
		}

		// start render
		StepView.renderNoCacheHeaders(ctx());
		renderBegin(out, ResourceLocator.getCSSHref(), "", "Math Worksheet Answer", hint);
		
		// render toolbar if needed  
		if (ui() != null){
			if (ui().origin != Origin.APP){
				renderUnitToolbar(out, unitId, referrer);
			} else {
				UnitRenderer.renderMobileToolbar(out, null, ui().sessionId,  referrer, ctx);				
			}
		}
		
		out.write("<div class='container'>\n");
		out.write("<div class='response'>\n");
		
		{
			out.write("<form action='wdgt' method='GET'>\n"); 
			out.write("<input type='hidden' name='action_id' value='" + action + "'>\n");
			if (ui() != null && ui().origin == Origin.APP){
				out.write("<input type='hidden' name='app_session' value='" + ui().sessionId + "'>\n");
				if (ui().sessionExpired){
					out.write("<input type='hidden' name='expire_token' value='true'>\n");
				}
			}
			out.write("<input type='hidden' name='inMemento' value='" + memento + "'>\n");

			out.write(img + "<p class='" + cls + "'>" + caption + "</p>\n");	  
			out.write("<br /><br />");
			
			if (hasUnitVariety){
				if (hasRepeat){
					out.write("<input type='submit' value='" + getRepeatCaption(lang) + "' name='inRepeat'/>&nbsp;\n&nbsp;");
				}
				out.write("<input type='submit' value='" + text + "'/>\n");
			} else {
				out.write("<input type='submit' value='" + getRepeatCaption(lang) + "' name='inRepeat'/>&nbsp;\n&nbsp;");
			}
			
			out.write("</div>\n");
			out.write("</form>\n");
		}

		out.write("</div>\n");
		out.write("</div>\n");

		// ads
		if (ui() != null && ui().origin == Origin.APP && !ui().isPro){
			ActionDispatcher.renderMathAdMobile(out);
		}
		
		renderEnd(out);
	}

	private String getRepeatCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Повторить";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "重复";
		return "Repeat";
	}
	
	private String getTryAgainCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Попробовать еще раз";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "再试一次";
		return "Try Again";
	}
	
	private String getNewChallengeCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Новое задание";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "新挑战";
		return "New Challenge";
	}
	
	private String getCorrectCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Правильно!";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "正确！";
		return "Correct!";
	}
	
	private String getInCorrectCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Неправильно.";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "不正确。";
		return "Incorrect.";
	}
	
	private String getInCorrectAgainCaption(String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) return "Опять неправильно. Попытки закончились.";
		if (TranslationDAO.LANG_ZH.equals(lang)) return "还是不正确。不能再试了。";
		return "Incorrect again. No more tries.";
	}
	
	private String getCheer(int passCount, String lang) {
		if (TranslationDAO.LANG_RU.equals(lang)) {
			if (passCount == 3) return "Отлично! 3 подряд!";
			if (passCount == 5) return "Ура! 3 подряд!";
			if (passCount == 10) return "Отлично! 10 подряд!";
			if (passCount > 15) return "Гений! Ни одной ошибки!";
		}
		
		if (TranslationDAO.LANG_ZH.equals(lang)) {
			if (passCount == 3) return "真棒！连对3题！";
			if (passCount == 5) return "太棒了！连对5题！";
			if (passCount == 10) return "棒极了！连对10题！";
			if (passCount > 15) return "天才！一个错误也没犯！";
		}
		
		if (passCount == 3) return "Great! 3 in a row!";
		if (passCount == 5) return "Awesome! 5 in a row!";
		if (passCount == 10) return "Amazing! 10 in a row!";
		if (passCount > 15) return "Genius! Not a single mistake!";

		return "";
	}
}
