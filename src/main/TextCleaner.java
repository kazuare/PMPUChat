package main;

import javax.servlet.http.*;

public class TextCleaner {

  public static String filter(String input) {
    StringBuilder filtered = new StringBuilder(input.length());
    char c;
    int wordCharsCount = 0;
    int lengthToBreak = 20;
    for(int i=0; i<input.length(); i++) {
      c = input.charAt(i);
      switch(c) {
        case '<': filtered.append("&lt;"); break;
        case '>': filtered.append("&gt;"); break;
        case '"': filtered.append("&quot;"); break;
        case '&': filtered.append("&amp;"); break;
        default: filtered.append(c);
      }
      if(c == ' ' || c == '\r'){
    	  wordCharsCount = 0;
      }else{
    	  wordCharsCount += 1;
      }
      if(wordCharsCount == lengthToBreak){
    	  filtered.append("<wbr>");
    	  wordCharsCount = 0;
      }
    }
    return(filtered.toString());
  }
  public static String prepareForPosting(String input, int maxlen) {
	  String temp = filter(input);
	  return temp.substring(0, Math.min(maxlen, temp.length()));
  }
	    
	  
}
