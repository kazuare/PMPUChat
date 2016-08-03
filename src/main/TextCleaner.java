package main;

import javax.servlet.http.*;

public class TextCleaner {

  public static String filter(String input) {
    if (!hasSpecialChars(input)) {
      return(input);
    }
    StringBuilder filtered = new StringBuilder(input.length());
    char c;
    for(int i=0; i<input.length(); i++) {
      c = input.charAt(i);
      switch(c) {
        case '<': filtered.append("&lt;"); break;
        case '>': filtered.append("&gt;"); break;
        case '"': filtered.append("&quot;"); break;
        case '&': filtered.append("&amp;"); break;
        default: filtered.append(c);
      }
    }
    return(filtered.toString());
  }

  private static boolean hasSpecialChars(String input) {
    if ((input != null) && (input.length()!=0)) 
      for(int i=0; i<input.length(); i++) 
        if(input.charAt(i)=='<'||
        	input.charAt(i)=='>'||
        	input.charAt(i)=='"'||
        	input.charAt(i)=='&')
        	return true;
     return false;
  }
  
}
