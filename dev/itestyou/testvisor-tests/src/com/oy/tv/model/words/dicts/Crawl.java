package com.oy.tv.model.words.dicts;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.oy.tv.util.StringNavigator;
import com.oy.tv.web.WebFlow;

public class Crawl {

	public static void main(String [] args) throws Exception {
		final String HOME = "E:/dev/eclipse-root/testvisor-tests/src/com/oy/tv/model/words/dicts/";
		
		String content; {
  		WebFlow wf = new WebFlow();
  		wf.execute();		
  		wf.get("http://www.dicts.info/vocabulary/index3.php");
  		content = wf.getLastPage();
		}
		
		{
  		Writer sw = new OutputStreamWriter(new FileOutputStream(
  				HOME + "index.php"), "UTF-8");
  		sw.write(content);
  		sw.close();
		}
		
		StringNavigator sn = new StringNavigator(content);
		sn.next("<form name=\"search\"");
		sn.next("<select ");
		while(sn.tryNext("<option ")){
			sn.next("value=\"");
			sn.next("\"");
			String ns = sn.prev();
			sn.next("</option>");
			
			WebFlow wf = new WebFlow();
  		wf.execute();		
  		wf.get("http://www.dicts.info/vocabulary/index3.php?l1=" + ns);
  		content = wf.getLastPage();

  		Writer sw = new OutputStreamWriter(new FileOutputStream(
  				HOME + ns + ".html"), "UTF-8");
  		sw.write(content);
  		sw.close();
  		
			System.out.println(ns);			
		}
	}
	
}
