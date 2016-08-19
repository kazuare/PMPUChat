package main;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import arrays.MagicStringArray;

/** Simple servlet for testing. Generates HTML instead of plain
 *  text as with the HelloWorld servlet.
 */

@WebServlet("/Chat")
public class Chat extends HttpServlet {
  private MagicStringArray chat;
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
	  //last added- index of the last message, -1 for the empty chat.
	  
	  //to change chat implementation back to vector (if it works ok, dont do it), 
	  //change MagicStringArray to Vector and delete condition at chat printing
	  
	  //if changing chatlength, change the same property in room.jsp
	  
	  int chatLength=100;
	  chat=new MagicStringArray(chatLength);
	  lastAdded=-1;
  } 
  
  @Override
  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
	//encoding stuff. must be written in the beginning of every servlet
	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8");
	
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
    
    HttpSession session = request.getSession(true);
    String username=session.getAttribute("username").toString();
    
    if(sendMessage!=null){
    	SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    	//need to add long for timezone fixing
        String strTime = timeFormat.format(new Date().getTime() + 7L * 60L * 60L * 1000L);
    	chat.add(username + ": " + strTime + "<br>" + TextCleaner.filter(
    			sendMessage.substring(0, Math.min(1000, sendMessage.length()))) + "<br>"
    			);
    	lastAdded++;
    	//adding index of the last message and the # separator
    	out.print(lastAdded + "#");
    }else if(refreshFrom != null && refreshFrom != ""){   
    	//serving messages between the most actual one on the server and the last message that client has acquired
    	//--not working for some reason,commented--if(isPosInt(refreshFrom)||refreshFrom.substring(0,2)=="-1"){
    		out.print(lastAdded + "#");
	    	for(int i = lastAdded; i > Integer.parseInt(refreshFrom) && i >= chat.getFirstIndex(); i--)
	    		out.print("<div class='msg'>" + chat.get(i) + "</div>");
	    	out.close();
    	//}
    }
    
	
  }
}
