package main;

import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import javafx.util.Pair;


@SuppressWarnings("serial")
@WebServlet("/Autorizator")
public class Autorizator extends ServletWithLogging  {
  private Vector<Pair<String,String>> mods;

  @Override
  public void init(){
	  
	mods = new Vector<Pair<String,String>>(128);
	  
	BufferedReader br;
	try {
		br = new BufferedReader(new FileReader("/music/passwords/mods.txt"));
	
		String line = null;
		while ((line = br.readLine()) != null) {
			int breakIndex = line.indexOf(' ');
			mods.add(
					new Pair<String, String>(
							line.substring(0, breakIndex),
							line.substring(breakIndex + 1)
							)
					);
		}
	} catch (IOException e) {
		logFatalError("Autorizator - " + e.getMessage());
	} 
	
	  
  } 
  
  @Override
  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
	//encoding stuff. must be written in the beginning of every servlet
	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8");
	
    HttpSession session = request.getSession(true);

    String login = request.getParameter("login");
    String password = request.getParameter("password");
    
    Pair<String,String> submittedPair = new Pair<String,String>(login,password);
    
    
    if(mods.contains(submittedPair)){
    	session.setAttribute("mod", "yes");
    }
    String nextJSP = "/game/room.jsp";
    response.sendRedirect(nextJSP);
  }
}
