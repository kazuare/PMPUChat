package main;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.jdbc.pool.DataSource;

@SuppressWarnings("serial")
@WebServlet("/GetPosts")
public class GetPosts extends ServletWithLogging{	
	
	PreparedStatement getterStatement;
	
	public boolean getPermissionToGet(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		
		return session.getAttribute( "mod" ) != null;
    }
	
	@Override
	public void init(){
		InitialContext cxt;
		DataSource ds;
		Connection connection;
		try {
			  cxt = new InitialContext();
		
			  ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
		
			  if ( ds == null ) {
			     throw new Exception("Data source not found!");
			  }
			  connection = ds.getConnection("PMPU","korovkin");
		
		
			  getterStatement = connection.prepareStatement("SELECT * FROM approved_posts where nickname = ?;");

			  
			
			  
		     
		      
		} catch (Exception e) {
			logFatalError("XML" + "-" + e.getMessage());	
		}    
	}
	public String getImageLinkAddition(){
		return "?" + Math.floor(Math.random()*6000);
	}
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if(getPermissionToGet(request)){
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
  	  	  boolean thereArePosts = false;
		
	      PrintWriter out = response.getWriter();
	      
    	  
		  out.println("<html>");
		  out.println("<head>");
		  out.println("<style>");
		  out.println(
			"div{"+
			    "width:90%;"+
			    "margin: 1em;"+
			    "background-color:#d5d9e5;"+
		    "}"
		  );
		  out.println("</style>");
		  out.println("</head>");
		  out.println("<body>");
	      try {
	    	  getterStatement.setString(1, (String)request.getParameter("target"));
	    	  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
	    	  ResultSet messages = getterStatement.executeQuery();
	    	  while(messages.next()){
	    		  thereArePosts = true;
	    		  out.println("<div>");	    		  
	    		  String strTime = timeFormat.format(messages.getTimestamp("time"));
			      String pic = messages.getString("picture");
			      if(!"SYSTEM".equals(pic)){
			    	  pic = "<br/><img src='" + pic + getImageLinkAddition() + "'/>";
			      }else{
			    	  pic = "";
			      }  
			      String contacts = messages.getString("contacts");
			      if(!contacts.equals(""))
			    	  contacts = " (" + contacts + ")";
				  out.println("<p>" + messages.getString("nickname") + contacts+
					": " + strTime + 
					"</p>" + messages.getString("message") + 
					"<br>" + pic);   
	    		  out.println("</div>");
	    	  }
	      } catch (SQLException e) {
	    	  logFatalError("GetPosts" + "-" + e.getMessage());
	      }
	      if(!thereArePosts)
	    	  out.println("Пользователь " + (String)request.getParameter("target") + " ничего не писал.");
		  out.println("</body>");
		  out.println("</html>");
	      out.flush();
	}
	}
}
