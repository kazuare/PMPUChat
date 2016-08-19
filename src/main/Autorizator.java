package main;

import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import arrays.MagicStringArray;

/** Simple servlet for testing. Generates HTML instead of plain
 *  text as with the HelloWorld servlet.
 */

@WebServlet("/Autorizator")
public class Autorizator extends HttpServlet  {
  @Override
  public void init(){
	  getServletContext().setAttribute("users", new Vector<String>(128));
  } 
  
  @Override
  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
	//encoding stuff. must be written in the beginning of every servlet
	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8");
	
    HttpSession session = request.getSession(true);
    PrintWriter out = response.getWriter();
    
    Vector<String> users = (Vector<String>) getServletContext().getAttribute("users");
    
    String username = TextCleaner.filter(request.getParameter("username"));
	if (users.indexOf(username)==-1 && !username.equals("")){
	    if (username.length() >= 30)
	    	username = username.substring(0, 31);
	    session.setAttribute("username", username);
		users.add(username);
	    out.print('1');   
    }else{
    	out.print('0');
    }
	out.close();
  }
}
