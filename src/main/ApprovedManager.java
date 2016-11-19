package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/Manager")
@MultipartConfig
public class ApprovedManager extends Manager{
	@Override
	public String getTable() {
        return "approved_posts";
    }
	@Override
	public String getImageLinkAddition(){
		return "?" + Math.floor(Math.random()*6000);
	}
	@Override
	public boolean getPermissionToPost(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute( "mod" ) !=null)
			return true;
		return false;
    }
	public void postApprovedMessage(String username, String contacts, String message, String path, String time){
		  super.lastAdded += 1;
		  super.totalScarfs += 1;
	  	  
		  PreparedStatement pstmt;
		  try {
		      //connection.setAutoCommit(false);
		      pstmt = connection.prepareStatement(
		    		  "INSERT INTO " + getTable() + "(index,nickname,contacts,message,picture,time) VALUES (" 
		    		  + lastAdded + ", "
		    		  + "?, ?, ?, ?, '" + time + ":00');");
		      pstmt.setString(1, username );
		      pstmt.setString(2, contacts );
		      pstmt.setString(3, message  );
		      pstmt.setString(4, path );
		      pstmt.executeUpdate();
	
		      pstmt.close();
		    } catch(Exception e){
		    	postSystemMessage("DB ERROR");
		    	postSystemMessage(e.getMessage());
		    }
	  	  
	  }
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if(getPermissionToPost(request)){
		try{
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			
			String name = (String) request.getAttribute("nickname");
			String contacts = (String) request.getAttribute("contacts");
			String message = (String) request.getAttribute("message");
			String file = (String) request.getAttribute("picture");
			String time = (String) request.getAttribute("time");
			
			String path = file.replace("un", "");
			path = path.substring(0, path.indexOf("approved")+"approved".length()) + Integer.toString(lastAdded+1) + path.substring(path.indexOf('.'));
			Path source = Paths.get(file);
			
			Files.deleteIfExists(Paths.get(path));
			Files.move(source, source.resolveSibling(path));
			postApprovedMessage(name, contacts, message, path, time);
		}catch(Exception e){
			postSystemMessage(e.toString());
		}
	}
	}
}
