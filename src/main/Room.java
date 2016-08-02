package main;

import java.io.*;
import java.util.Scanner;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

/** Simple servlet for testing. Generates HTML instead of plain
 *  text as with the HelloWorld servlet.
 */

@WebServlet("/main")
public class Room extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    
    String URL="/music/Free.mp3";
    
    out.println
      ("<!DOCTYPE html>\n" +
       "<html>\n" +
       "<head><title>A Test Servlet</title></head>\n" +
       "<body bgcolor=\"#fdf5e6\">\n" +
       "<h1>PMPU MUSIC GUESSING GAME</h1>\n" +
       "<audio src='"+URL+"' controls ></audio>"+
       "<p>MP3 test.</p>\n" +
       "<form method='post' action='main'>"+  
       "<textarea name='answer' cols='40' rows='2'></textarea>"+  
       "<input type='submit' id='btn2' name='btn2'/>"+  
       "</form>"+
       "</body></html>");
  }
  public void doPost(HttpServletRequest request,
          HttpServletResponse response)
	throws ServletException, IOException {	  
	    Scanner s = null;
	    try {
	        s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    String attempt=s.hasNext() ? s.next() : "";
	    attempt=attempt.substring(attempt.indexOf("answer=")+"answer=".length());
	    attempt=attempt.substring(0,attempt.indexOf('&'));
	    attempt=attempt.replace('+',' ');
	    attempt=attempt.toLowerCase();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String URL="/music/Free.mp3";
		if(attempt.equals("")){		
		}else if(attempt.equals("free your mind")){
			out.println("Correct!");
		}else{
			out.println("something is wrong, here:"+attempt+"\\");
		}
    }
}
