package main;

import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

/** Simple servlet for testing. Generates HTML instead of plain
 *  text as with the HelloWorld servlet.
 */

@WebServlet("/Chat")
public class Chat extends HttpServlet {
  private Vector<String> chat;
  private int lastAdded;
  public static boolean isPosInt(String str)
  {
      for (char c : str.toCharArray())
      {
          if (!Character.isDigit(c)) return false;
      }
      return true;
  }
  
  @Override
  public void init(){
	  chat=new Vector<String>(100);
	  lastAdded=-1;
  } 
  @Override
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
	  
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    String sendMessage = request.getParameter("sendMessage");
    String refreshFrom = request.getParameter("refreshFrom");
    
    if(sendMessage!=null){
    	chat.add("Anonimous:<br>"+TextCleaner.filter(
    			sendMessage.substring(0,Math.min(1000,sendMessage.length())))+"<br>"
    			);
    	lastAdded++;
    	out.print(lastAdded+"#");
    }else if(refreshFrom!=null&&refreshFrom!=""){   
    	//if(isPosInt(refreshFrom)||refreshFrom.substring(0,2)=="-1"){
    		out.print(lastAdded+"#");
	    	for(int i=lastAdded;i>Integer.parseInt(refreshFrom);i--)
	    		out.print("<div>"+chat.get(i)+"</div>");
	    	out.close();
    	//}
    }
    
	
  }
}
