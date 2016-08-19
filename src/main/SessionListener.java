package main;

import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener{
	@Override
	  public void sessionCreated(HttpSessionEvent arg0) { }

	  @Override
	  public void sessionDestroyed(HttpSessionEvent arg0) {
		  HttpSession session = arg0.getSession();		
		  Vector<String> users = (Vector<String>) session.getServletContext().getAttribute("users");
		  users.remove(session.getAttribute("username"));	
	  }
}
