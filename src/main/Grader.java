package main;

import java.io.*;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

/** Simple servlet for testing. Generates HTML instead of plain
 *  text as with the HelloWorld servlet.
 */

@WebServlet("/Grader")
public class Grader extends HttpServlet {
  private int vasyan2002Score;  
  @Override
  public void init(){
	//Your average Vasyan:
	  vasyan2002Score=0;
  } 
  
  @Override
  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
	//encoding stuff. must be written in the beginning of every servlet
	request.setCharacterEncoding("UTF-8");
	response.setCharacterEncoding("UTF-8");
	
    String sendMessage = request.getParameter("sendMessage");
        
    //if the answer is found, vasyan's score increases!
    if(sendMessage!=null){
    	if(sendMessage.toLowerCase().indexOf("free your mind")!=-1){
    		vasyan2002Score++;
    	}
    	if(sendMessage.toLowerCase().indexOf("pizuya's cell")!=-1){
    		vasyan2002Score++;
    	}
    }
    RequestDispatcher rd =request.getRequestDispatcher("/Chat");              
    rd.forward(request, response);    
	
  }
}
