package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.jdbc.pool.DataSource;

@SuppressWarnings("serial")
@WebServlet("/RandomPost")
@MultipartConfig
public class RandomPicManager extends ServletWithLogging {
	protected Connection connection;
	protected PreparedStatement messageCountStatement;
	protected PreparedStatement messageRetrieveStatement;
	protected int total = 0;
	public String getTable() {
        return "approved_posts";
    }
	
	public String getImageLinkAddition(){
		return "?" + Math.floor(Math.random()*6000);
	}
	
	public void prepareStatements() throws SQLException{
		messageCountStatement = connection.prepareStatement("SELECT COUNT(index) AS result FROM " + getTable() + " WHERE nickname != 'SYSTEM';");
		messageRetrieveStatement = connection.prepareStatement("SELECT * FROM " + getTable() + " where index = ?;");
	}
	public int countMessages() throws SQLException{
		ResultSet countResult = messageCountStatement.executeQuery();
		int count = 0;
		while (countResult.next()) 
			count = countResult.getInt("result");	
		return count;
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
			  
			total = countMessages(); 
		
		} catch (Exception e) {
			logFatalError(getTable() + "-" + e.getMessage());			
		}
	  } 
	
	//retrieves message from database
	public String retrieveMessage(int index){
		  try{
			  messageRetrieveStatement.setInt(1, index);		
			  ResultSet message = messageRetrieveStatement.executeQuery();
			
			  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  	  
			  if(message.next()){
			      String strTime = timeFormat.format(message.getTimestamp("time"));
			      String pic = message.getString("picture");
			      
			      pic = "<img class='img-responsive' src=" + pic + getImageLinkAddition() + " alt=''>";
			      
			      String contacts = message.getString("contacts");
			      if(!contacts.equals(""))
			    	  contacts = " (" + contacts + ")";
				  
				  return 
					"<div class='well'>" +
						pic + 
					"</div>" +
				  	"<span style='visibility:hidden;'>" +
						"<h3>" + message.getString("nickname") + contacts + "</h3>" +
		                "<p>" + message.getString("message") + "</p>" +
	                "</span>";
	              
			  }
		  } catch (SQLException e) {	
			  logFatalError(getTable() + "-" + e.getMessage());	
			  return e.getMessage();
		  }
		  return "-";
	  }
	
	  @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  
		//encoding stuff. must be written in the beginning of every servlet
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
			
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		try{
			total = countMessages();		
		} catch (Exception e) {	
			logFatalError(getTable() + "-" + e.getMessage());	
		}
		out.print(retrieveMessage((int)Math.floor(Math.random() * total)));		
		
		out.close();
		
	  }
	  
}
