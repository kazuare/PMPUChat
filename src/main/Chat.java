package main;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import org.apache.tomcat.jdbc.pool.DataSource;

import arrays.MagicStringArray;


@WebServlet("/Chat")
public class Chat extends HttpServlet {
  private MagicStringArray chat;
  private int lastAdded;
  private Connection connection;
  public static boolean isPosInt(String str)
  {
      for (char c : str.toCharArray())
      {
          if (!Character.isDigit(c)) return false;
      }
      return true;
  }
  
  public void postMessage(String username, String message){
	  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  	  //need to add long for timezone fixing
      String strTime = timeFormat.format(new Date().getTime() + 7L * 60L * 60L * 1000L);
	  chat.add("<p>" + username + ": " + strTime + "</p>" + TextCleaner.filter(
  			message.substring(0, Math.min(1000, message.length()))) + "<br>"
  			);
  	  lastAdded++;
  	  
	  PreparedStatement pstmt;
	  try {
	      connection.setAutoCommit(false);
	      pstmt = connection.prepareStatement(
	    		  "INSERT INTO messages(index,nickname,message,time) VALUES (" 
	    		  + lastAdded + ", '"
	    		  + username + "', '" 
	    		  + TextCleaner.filter( message.substring(0, Math.min(1000, message.length()))) + "', '" 
	    		  + strTime + ":00');");
	      pstmt.executeUpdate();
	
	      connection.commit();
	      pstmt.close();
	
	    } catch(Exception e){
	    	postSystemMessage("DB ERROR");
	    	postSystemMessage(e.getMessage());
	    }
  	  
  }
  public void postSystemMessage(String message){
	  postMessage("SYSTEM", message);
  }
  public String retrieveMessage(int index){
	  try{
		  if(index > lastAdded)
			  throw new Exception("index is greater than index of the last added!");
		  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages where index = ?;");
		  preparedStatement.setInt(1, index);
	
		  ResultSet message = preparedStatement.executeQuery();
		
		  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  	  
		  if(message.next()){
	      String strTime = timeFormat.format(message.getTimestamp("time"));
		  return "<p>" + message.getString("nickname") +
			": " + strTime + 
			"</p>" + message.getString("message") + "<br>";    	  
		  }
	  } catch (Exception e) {
			postSystemMessage(e.getMessage());		
			return e.getMessage();
	  }
	  return "error - no 'next' in message retriever";
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
	
	InitialContext cxt;
	DataSource ds;
	try {
		  cxt = new InitialContext();
	
		  if ( cxt == null ) {
		     throw new Exception("Uh oh -- no context!");
		  }
	
		  ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
	
		  if ( ds == null ) {
		     throw new Exception("Data source not found!");
		  }
		  connection = ds.getConnection("PMPU","korovkin");
	
	
		  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM messages;");

		  ResultSet initialMessages = preparedStatement.executeQuery();
		
		  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  	  
		  
	      while (initialMessages.next()) {
	    	  String strTime = timeFormat.format(initialMessages.getTimestamp("time"));
			  chat.add("<p>" + initialMessages.getString("nickname") +
					  ": " + strTime + 
					  "</p>" + initialMessages.getString("message") + "<br>"
		  			);
		  	  lastAdded++;	    	  
          }
	      
	      if(lastAdded == -1)	      
	    	  postSystemMessage("CHAT START");
	
	} catch (Exception e) {
		postSystemMessage("DB ERROR");
		postSystemMessage(e.getMessage());		
	}
  } 
  
  @Override
  public void destroy() {
	  if (connection != null)
		try {
			connection.close();
		} catch (SQLException e) {}
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
    	if(sendMessage != "")
	    	postMessage(username, TextCleaner.filter(
	    			sendMessage.substring(0, Math.min(1000, sendMessage.length()))
	    			));
    	//adding index of the last message and the # separator
    	out.print(lastAdded + "#");
    }else if(refreshFrom != null && refreshFrom != ""){   
    	//serving messages between the most actual one on the server and the last message that client has acquired
    	//--not working for some reason,commented--if(isPosInt(refreshFrom)||refreshFrom.substring(0,2)=="-1"){
    		out.print(lastAdded + "#");
	    	for(int i = lastAdded; i > Integer.parseInt(refreshFrom) && i >= chat.getFirstIndex(); i--)
	    		out.print("<div class='msg'>" + retrieveMessage(i) + "</div>");
	    		//out.print("<div class='msg'>" + chat.get(i) + "</div>");
	    	out.close();
    	//}
    }
    
	
  }
}
