package main;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@WebServlet("/Manager")
@MultipartConfig
public class ApprovedManager extends Manager{
	@Override
	public String getTable() {
        return "messages";
    }
	@Override
	public boolean getPermissionToPost(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute( "mod" ) !=null)
			return true;
		return false;
    }
}
