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
	  //chat messages are contained here.
	  //last added- index of the last message, -1 for empty chat.
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
    
    //response structure is:
    //if client is just sending a message:
    //id of the last message on server #
    //if client is asking for updates:
    //id of the last message on server # new messages for the client (we know what messages client already has
    // with the help of refreshFrom param)
    
    if(sendMessage!=null){
    	chat.add("Anonimous:<br>"+TextCleaner.filter(
    			sendMessage.substring(0,Math.min(1000,sendMessage.length())))+"<br>"
    			);
    	lastAdded++;
    	//adding index of the last message and the # separator
    	out.print(lastAdded+"#");
    }else if(refreshFrom!=null&&refreshFrom!=""){   
    	//serving messages between the most actual one on the server and the last message that client has acquired
    	//--not working for some reason,commented--if(isPosInt(refreshFrom)||refreshFrom.substring(0,2)=="-1"){
    		out.print(lastAdded+"#");
	    	for(int i=lastAdded;i>Integer.parseInt(refreshFrom);i--)
	    		out.print("<div>"+chat.get(i)+"</div>");
	    	out.close();
    	//}
    }
    
	
  }
}
