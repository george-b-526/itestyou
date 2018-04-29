package com.vokamis.ity.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ViewHelper {

	/**
	 * WebView does not properly load HTML data with some special characters. 
	 * This method properly escapes HTML and loads the data into WebView. 
	 */
	public static void loadData(WebView wv, String data, String mimeType, String encoding) {
		StringBuilder buf = new StringBuilder();

		int len = data.length();
		for (int i = 0; i < len; i++) {
			char chr = data.charAt(i);
			switch (chr) {
			case '%':
				buf.append("%25");
				break;
			case '\'':
				buf.append("%27");
				break;
			case '#':
				buf.append("%23");
				break;
			default:
				buf.append(chr);
			}
		}
		
		wv.loadData(buf.toString(), mimeType, encoding);
	}
	
	/**
	 * Fill Spinner with items.
	 */

	public static void fillSpinner(Context context, Spinner spinner, String ... items){
		List<String> list = new ArrayList<String>();
		for (String item : items){
			list.add(item);
		}
		fillSpinner(context, spinner, list);
	}
	
	public static void fillSpinner(Context context, Spinner spinner, List<String> items){
		ArrayAdapter <CharSequence> adapter =
		  new ArrayAdapter <CharSequence> (context, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		for (String item : items){
			adapter.add(item);
		}
		
		spinner.setAdapter(adapter);
	}
	
}
