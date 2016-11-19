package main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.jdbc.pool.DataSource;

@WebServlet("/LoadXML")
public class LoadXML extends HttpServlet{	
	
	public boolean getPermissionToGet(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute( "mod" ) !=null)
			return true;
		return false;
    }
	
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if(getPermissionToGet(request)){
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		InitialContext cxt;
		DataSource ds;
		Connection connection;
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
		
		
			  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM approved_posts;");

			  ResultSet messages = preparedStatement.executeQuery();
			
			  
			  String fileType = "text/plain";
		      // You must tell the browser the file type you are going to send
		      // for example application/pdf, text/plain, text/html, image/jpg
		      response.setContentType(fileType);
		      response.setHeader("Content-disposition","attachment; filename=data.xml");
		        
		      // This should send the file to browser
		      PrintWriter out = response.getWriter();
			  
		      out.write("<?xml version=\"1.0\"?>\n");
		      out.write("<posts>\n");
		      out.write("<approved_posts>\n");
		      while (messages.next()) {
		    	  out.write("<post>\n");
			    	  out.write("<index>\n");
			    	  out.write(messages.getInt("index")+ "\n");
			    	  out.write("</index>\n");
		    	  
			    	  out.write("<sender>\n");
			    	  out.write(messages.getString("nickname").replace("<wbr>", "") + "\n");
			    	  out.write("</sender>\n");
			    	  
			    	  out.write("<contacts>\n");
			    	  out.write(messages.getString("contacts").replace("<wbr>", "") + "\n");
			    	  out.write("</contacts>\n");
			    	  
			    	  out.write("<message>\n");
			    	  out.write(messages.getString("message").replace("<wbr>", "") + "\n");
			    	  out.write("</message>\n");

			    	  out.write("<time>\n");
			    	  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
			    	  out.write(format.format(messages.getTimestamp("time")) + "\n");			    	  
			    	  out.write("</time>\n");

			    	  out.write("<image_link>\n");
			    	  out.write(messages.getString("picture")+ "\n");
			    	  out.write("</image_link>\n");
		    	  out.write("</post>\n");
	          }   
		      out.write("</approved_posts>\n");
		      
		      preparedStatement = connection.prepareStatement("SELECT * FROM unapproved_posts;");

			  messages = preparedStatement.executeQuery();
			
		      out.write("<unapproved_posts>\n");
		      while (messages.next()) {
		    	  out.write("<post>\n");
		    	  out.write("<index>\n");
		    	  out.write(messages.getInt("index")+ "\n");
		    	  out.write("</index>\n");
	    	  
		    	  out.write("<sender>\n");
		    	  out.write(messages.getString("nickname").replace("<wbr>", "") + "\n");
		    	  out.write("</sender>\n");
		    	  
		    	  out.write("<contacts>\n");
		    	  out.write(messages.getString("contacts").replace("<wbr>", "") + "\n");
		    	  out.write("</contacts>\n");
		    	  
		    	  out.write("<message>\n");
		    	  out.write(messages.getString("message").replace("<wbr>", "") + "\n");
		    	  out.write("</message>\n");

		    	  out.write("<time>\n");
		    	  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
		    	  out.write(format.format(messages.getTimestamp("time")) + "\n");			    	  
		    	  out.write("</time>\n");

		    	  out.write("<image_link>\n");
		    	  out.write(messages.getString("picture")+ "\n");
		    	  out.write("</image_link>\n");
		    	  out.write("</post>\n");
	          }   
		      out.write("</unapproved_posts>\n");
		      out.write("</posts>");
		      
		      out.flush();
		      
		} catch (Exception e) {}    
	}
	}
}
