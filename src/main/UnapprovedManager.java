package main;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ToApprove")
@MultipartConfig
public class UnapprovedManager extends Manager{
	@Override
	public String getTable() {
        return "unapproved";
    }
	//this checks for mod priveleges, actually
	@Override	
	public boolean getPermissionToGet(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute( "mod" ) !=null)
			return true;
		return false;
    }
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if(getPermissionToPost(request)){	  
		String del = request.getParameter("del");
		if( del == null ){
			super.doPost(request, response);
		}else{
			HttpSession session = request.getSession(true);
			if(session.getAttribute( "mod" ) !=null){
				  try{
					  PreparedStatement preparedStatement = connection.prepareStatement("DELETE * FROM " + getTable() + " where index = ?;");
					  preparedStatement.setInt(1, Integer.parseInt(del));
				
					  ResultSet message = preparedStatement.executeQuery();
					
				  } catch (Exception e) { }		
			}				
		}
	}
	}
}
