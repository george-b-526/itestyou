package com.vokamis.ity.util;

public class StringNavigator {
 
	public boolean hasNext;
	public String prev;
	public String next;
	
	public StringNavigator(){
		
	}
	
	public StringNavigator(String value){
		next = value;
	}
	
	private static boolean hasNext(StringNavigator current, String separator){		
		if (current.next == null){
			return false;
		}
		
		int idx = current.next.indexOf(separator);
		if (idx == -1){
			return false;
		}
		
		current.prev = current.next.substring(0, idx);
		current.next = current.next.substring(idx + separator.length());
		return true;
	}

	private static void next(StringNavigator current, String separator){
		if (!hasNext(current, separator)){
			throw new RuntimeException("Failed for get next " + separator + " for " + current);
		}
	}
	
	public boolean hasNext(String separator){
		if (next == null){
			return false;
		}
		
		return next.indexOf(separator) != -1;
	}
	
	public boolean tryNext(String separator){
		return hasNext(this, separator);
	}
	
	public StringNavigator next(String separator){
		next(this, separator);
		return this;
	}

	
	//
	// nav = new Nav("[[foo[[bar]]]]");
	// String e = nav.extract("[[", "]]");
	// nav.nextInDepth("[[", "]]");	// returns the very last ]], 
	// 
	public boolean extract(String open, String close){
		StringBuffer sb = new StringBuffer();
		 
		StringNavigator sn = new StringNavigator(next);
		boolean start = sn.hasNext(open);
		if (!start){	 	
			return false;
		} else {
			sn.next(open);
			while(true){
				boolean end = sn.hasNext(close);
				if (!end){
					return false;
				}
				sn.next(close);
				sb.append(sn.prev);				
				 
				StringNavigator rev = new StringNavigator(sn.prev);
				boolean revStart = rev.hasNext(open);
				if (revStart){
					sb.append(close);
				} else {
					break;
				}
			}
		} 
		
		prev = sb.toString();
		next = sn.next;
		
		return true;
	}
	
	public String toString(){
		return prev + "\n" + next;
	}
	
}
