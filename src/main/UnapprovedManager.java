package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.util.http.fileupload.IOUtils;

@SuppressWarnings("serial")
@WebServlet("/ToApprove")
@MultipartConfig
public class UnapprovedManager extends Manager{
	private int lastAdded;
	protected int linkAdditionNumber = 0;
	protected double servletStartRandomDouble = 0;
	@Override
	public String getImageLinkAddition(){
		return "?" + linkAdditionNumber + servletStartRandomDouble;
	}
	@Override
	public String getTable() {
        return "unapproved_posts";
    }
	//this checks for mod privileges, actually
	@Override	
	public boolean getPermissionToGet(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		if(session.getAttribute( "mod" ) !=null)
			return true;
		return false;
    }
	
	@Override
	public int getChatLength(){
		return 1;
	}
	
	@Override
	  public void init(){
		servletStartRandomDouble= Math.random();
		
		InitialContext cxt;
		DataSource ds;
		try {
			  cxt = new InitialContext();
		
			  ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
		
			  if ( ds == null ) {
			     throw new Exception("Data source not found!");
			  }
			  connection = ds.getConnection("PMPU","korovkin");
			  
			  prepareStatements();
			  
		} catch (Exception e) {
			logFatalError(getTable() + "-" + e.getMessage());		
		}
	  } 
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  if(getPermissionToGet(request)){
			//encoding stuff. must be written in the beginning of every servlet
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			
		    response.setContentType("text/html");
		    PrintWriter out = response.getWriter();
		    
		    if(thereArePosts()){
		    	out.print("<div class='msg'>" + retrieveMessage(getTheOldestMessageIndex()) + "</div>");
		    }else{
		    	out.print("");
		    }
		    out.close();
	  }
	  }
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	if(getPermissionToPost(request)){
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String del = request.getParameter("del");
		if( del == null ){
			response.setContentType("text/html");
			
		    PrintWriter output = response.getWriter();
		    
		    String name = request.getParameter("name"); 
		    String message = request.getParameter("message");
		    String contacts = request.getParameter("contacts"); 
		    Part filePart = request.getPart("file"); 
		    
		    String type = "invalid";
		    
		    if(name.equals("")){
		    	output.print("Не заполнено поле 'имя'.");
		    	output.print("<br>");
			    output.print("<a href='form.jsp'>Попробовать еще раз</a>");
		    }else if (name.toLowerCase().contains("system")){
		    	output.print("Запрещено использовать ключевое слово system в имени.");
		    	output.print("<br>");
			    output.print("<a href='form.jsp'>Попробовать еще раз</a>");
		    }else if (message.equals("")){
		    	output.print("Пожалуйста, черкните пару слов в графе 'Сообщение'!");
		    	output.print("<br>");
			    output.print("<a href='form.jsp'>Попробовать еще раз</a>");
		    }else if (filePart.getSize() > 1024 * 1024 * 16) {
		    	output.print("Фотография слишком велика.");
		    	output.print("<br>");
			    output.print("<a href='form.jsp'>Попробовать еще раз</a>");
			}else{				
				if("image/jpeg".equals(filePart.getContentType())){
					type = ".jpg";
				}else if ("image/png".equals(filePart.getContentType())){
					type = ".png";
				}else{
					output.print("Пожалуйста, приложите фотографию в формате PNG или JPEG.");
			    	output.print("<br>");
				    output.print("<a href='form.jsp'>Попробовать еще раз</a>");
				}				
			}
		    if(!type.equals("invalid") ){
		    	InputStream fileContent = filePart.getInputStream();
		    	lastAdded += 1;
		    	String path = "/music/photoes/unapproved" + (lastAdded) + type;
		    	
		    	new File(path).delete();
		    	
			    OutputStream out = new FileOutputStream(path); 
			    IOUtils.copy(fileContent,out);
			    fileContent.close();
			    out.close();
			    output.print("<html>");
			    output.print("<body>");
			    output.print("Спасибо. Ваш пост будет рассмотрен модератором в ближайшее время!");
			    output.print("<br>");
			    output.print("<a href='form.jsp'>Нажмите, чтобы запостить еще одну вещь</a>");
			    output.print("</body>");
			    output.print("</html>");
			    HttpSession session = request.getSession(true);
			    session.setAttribute("nickname", name);
			    if(!contacts.equals(""))
			    	session.setAttribute("contacts", contacts);
			    postMessage(name, contacts, message, path);
		    }
		    
		    output.close();
		}else{
			HttpSession session = request.getSession(true);
			if(session.getAttribute( "mod" ) !=null){
				  linkAdditionNumber++;
				  if(linkAdditionNumber>999999999)
					  linkAdditionNumber = 0;
				  try{
					  int index = getTheOldestMessageIndex();
					  PreparedStatement preparedStatement;
					  if(del.equals("accept")){
						  messageRetrieveStatement.setInt(1, index);
					
						  ResultSet message = messageRetrieveStatement.executeQuery();
						 	  
						  if(message.next()){
						      String pic = message.getString("picture");
						      if(pic.equals("SYSTEM"))
						    	  pic = "";
						      
							  request.setAttribute("nickname", message.getString("nickname"));
							  request.setAttribute("contacts", message.getString("contacts"));
							  request.setAttribute("time", message.getTimestamp("time"));
							  request.setAttribute("message", message.getString("message"));
							  request.setAttribute("picture", pic);
						  }
						      
					  }
						  
					  messageDeleteStatement.setInt(1, index);				
					  messageDeleteStatement.executeUpdate();
					  
					  if(del.equals("accept")){
						  RequestDispatcher rd = request.getRequestDispatcher("/Manager");
						  rd.forward(request,response);
					  }
					  
				  } catch (Exception e) {
					  logFatalError(getTable() + "-" + e.getMessage());	
				  }		
			}				
		}
	}
	}
}
