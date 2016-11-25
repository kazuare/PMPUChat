package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.jdbc.pool.DataSource;

@SuppressWarnings("serial")
public abstract class Manager extends ServletWithLogging {
	protected int lastAdded;
	protected Connection connection;
	protected int totalScarfs = 0;
	protected PreparedStatement insertStatement;
	protected PreparedStatement oldestMessageStatement;
	protected PreparedStatement messageRetrieveStatement;
	protected PreparedStatement postCheckStatement;
	protected PreparedStatement messageDeleteStatement;
	public String getTable() {
        return null;
    }
	//protects pics from browser caching
	public String getImageLinkAddition(){
		return "";
	}
	public boolean getPermissionToPost(HttpServletRequest request) {
        return true;
    }
	public boolean getPermissionToGet(HttpServletRequest request) {
        return true;
    }
	
	public int getChatLength(){
		return 15;
	}
	
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
		InitialContext cxt;
		DataSource ds;
		try {
			  cxt = new InitialContext();
				
			  ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
		
			  if ( ds == null ) {
			     throw new Exception("Data source not found!");
			  }
			  connection = ds.getConnection("PMPU","korovkin");
		
			  prepareStatements();
			  
			  //executed only once
			  PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(index) AS i FROM " + getTable() + ";");
			  ResultSet initialMessages = preparedStatement.executeQuery();
			
		      while (initialMessages.next()) {
			  	  lastAdded = initialMessages.getInt("i") - 1;	    	  
	          }
		      
		      if(lastAdded != -1){	
		    	  //executed only once
		    	  preparedStatement = connection.prepareStatement("SELECT COUNT(index) AS i FROM " + getTable() + " WHERE nickname != 'SYSTEM';");
		    	  ResultSet scarfsCountResult = preparedStatement.executeQuery();
					
			      while (scarfsCountResult.next()) {
				  	  totalScarfs = scarfsCountResult.getInt("i");	    	  
		          }
			      preparedStatement.close();
		      }
		
		} catch (Exception e) {
			logFatalError(getTable() + "-" + e.getMessage());			
		}
	  } 
	public void prepareStatements() throws SQLException{
		insertStatement = connection.prepareStatement(
	    		  "INSERT INTO " + getTable() + "(index,nickname,contacts,message,picture,time) "
		    		  		+ "VALUES (?, ?, ?, ?, ?, ?);"
	    				  );
		oldestMessageStatement = connection.prepareStatement("SELECT MIN(index) AS i FROM  " + getTable() + ";");
		postCheckStatement = connection.prepareStatement("SELECT EXISTS( SELECT 1 FROM " + getTable() + " ) AS bool;");
		messageRetrieveStatement = connection.prepareStatement("SELECT * FROM " + getTable() + " where index = ?;");
		messageDeleteStatement = connection.prepareStatement("DELETE FROM " + getTable() + " WHERE index = ?;");
	}
	public void postMessage(String username, String contacts, String message, String path){
	      synchronized (this){
	    	  lastAdded++;
		  	  if(!"SYSTEM".equals(username))
		  		  totalScarfs++;
	      }
	  	  
		 
		  try {
		      insertStatement.setInt(1, lastAdded);
		      insertStatement.setString(2, TextCleaner.prepareForPosting(username, 100) );
		      insertStatement.setString(3, TextCleaner.prepareForPosting(contacts, 100) );
		      insertStatement.setString(4, TextCleaner.prepareForPosting(message, 1000) );
		      insertStatement.setString(5, path );
		      Timestamp t = new Timestamp(new Date().getTime() + 8L * 60L * 60L * 1000L);
		      insertStatement.setTimestamp(6, t);
		      insertStatement.executeUpdate();
		      
		    } catch(Exception e){
		    	logFatalError(getTable() + " - " + e.getMessage());
		    }
	  	  
	  }
	  public void postSystemMessage(String message){
		  postMessage("SYSTEM", "", message, "SYSTEM");
	  }
	  public boolean thereArePosts(){
		  try{
			  ResultSet v = postCheckStatement.executeQuery();
			  if(v.next())
				  return v.getBoolean("bool");
			  
		  } catch (Exception e) {
			  logFatalError(getTable() + "-" + e.getMessage());		
		  }
		  return false;
	  }
	  public int getTheOldestMessageIndex(){
		  try{
			  ResultSet v = oldestMessageStatement.executeQuery();
			  if(v.next())
				  return v.getInt("i");
			  
		  } catch (Exception e) {
			  logFatalError(getTable() + "-" + e.getMessage());	
		  }
		  return 0;
	  }
	  public String retrieveMessage(int index){
		  try{
			  messageRetrieveStatement.setInt(1, index);		
			  ResultSet message = messageRetrieveStatement.executeQuery();
			
			  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  	  
			  if(message.next()){
			      String strTime = timeFormat.format(message.getTimestamp("time"));
			      String pic = message.getString("picture");
			      if(!"SYSTEM".equals(pic)){
			    	  pic = "<br/><img src='" + pic + getImageLinkAddition() + "'/>";
			      }else{
			    	  pic = "";
			      }  
			      String contacts = message.getString("contacts");
			      if(!contacts.equals(""))
			    	  contacts = " (" + contacts + ")";
				  return "<p>" + message.getString("nickname") + contacts+
					": " + strTime + 
					"</p>" + message.getString("message") + 
					"<br>" + pic;    	  
			  }
		  } catch (Exception e) {	
			  logFatalError(getTable() + "-" + e.getMessage());	
		  }
		  return "error - no 'next' in message retriever";
	  }
	  
	  
	  @Override
	  public void destroy() {
		  if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {
				logFatalError(getTable() + "-" + e.getMessage());	
			}
	  }
	  //retrieves message from database
	  @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  if(getPermissionToGet(request)){
			//encoding stuff. must be written in the beginning of every servlet
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			
		    response.setContentType("text/html");
		    PrintWriter out = response.getWriter();
		    
		    String refreshFrom = request.getParameter("refreshFrom");
		    String id = request.getParameter("id");
		    String len = request.getParameter("len");
		    
		    
		    //response structure is:
		    //if client is asking for updates:
		    //id of the last message on server # new messages for the client (we know what messages client already has
		    // with the help of refreshFrom param)
		    
		    if(refreshFrom != null && !refreshFrom.equals("")){   
		    	//serving messages between the most actual one on the server and the last message that client has acquired
		    	//--not working for some reason,commented--if(isPosInt(refreshFrom)||refreshFrom.substring(0,2).equals("-1")){
		    		out.print(lastAdded + "#" + totalScarfs + "%");
		    		int destination = Math.max(Integer.parseInt(refreshFrom),lastAdded - getChatLength());
			    	for(int i = lastAdded; i > destination; i--)
			    		out.print("<div class='msg'>" + retrieveMessage(i) + "</div>");
			    	
		    	//}
		    }else if(id != null && !id.equals("") && len != null && !len.equals("")){
		    	int howMuchToDispense = 5;
		    	//if lastId is 20 and len is 10, then start is 10
		    	int start = Integer.parseInt(id) - Integer.parseInt(len);
		    	int destination = Math.max(start - howMuchToDispense, 0);
		    	if(destination == 0){
		    		out.print("0");
		    	}else{
		    		out.print("1");
		    	}
		    	for(int i = start; i >= destination; i--)
		    		out.print("<div class='msg'>" + retrieveMessage(i) + "</div>");
		    }
		    out.close();
	  }
	  }
	  
	
}
