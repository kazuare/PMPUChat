package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.util.http.fileupload.IOUtils;


public abstract class Manager extends HttpServlet {
	private int lastAdded;
	protected Connection connection;
	private int totalScarfs = 0;
	
	public String getTable() {
        return null;
    }
	public boolean getPermissionToPost(HttpServletRequest request) {
        return true;
    }
	public boolean getPermissionToGet(HttpServletRequest request) {
        return true;
    }
	
	private final int chatLength = 15;
	
	public static boolean isPosInt(String str)
	  {
	      for (char c : str.toCharArray())
	      {
	          if (!Character.isDigit(c)) return false;
	      }
	      return true;
	  }
	
	public void postMessage(String username, String message, String path){
		  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	  	  //need to add long for timezone fixing
	      String strTime = timeFormat.format(new Date().getTime() + 7L * 60L * 60L * 1000L);
	      
	      synchronized (this){
	    	  lastAdded++;
		  	  if(!username.equals("SYSTEM"))
		  		  totalScarfs++;
	      }
	  	  
		  PreparedStatement pstmt;
		  try {
		      connection.setAutoCommit(false);
		      pstmt = connection.prepareStatement(
		    		  "INSERT INTO " + getTable() + "(index,nickname,message,picture,time) VALUES (" 
		    		  + lastAdded + ", "
		    		  + "?, ?, ?, " 
		    		  + "'" + strTime + ":00');");
		      pstmt.setString(1, 
		    		  TextCleaner.filter( username.substring(0, Math.min(100, username.length())))
		    		  );
		      pstmt.setString(2, 
		    		  TextCleaner.filter( message.substring(0, Math.min(1000, message.length())))
		    		  );
		      pstmt.setString(3, 
		    		  path
		    		  );
		      pstmt.executeUpdate();
		
		      connection.commit();
		      pstmt.close();
		
		    } catch(Exception e){
		    	postSystemMessage("DB ERROR");
		    	postSystemMessage(e.getMessage());
		    }
	  	  
	  }
	  public void postSystemMessage(String message){
		  postMessage("SYSTEM", message, "SYSTEM");
	  }
	  public String retrieveMessage(int index){
		  try{
			  if(index > lastAdded)
				  throw new Exception("index is greater than index of the last added!");
			  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + getTable() + " where index = ?;");
			  preparedStatement.setInt(1, index);
		
			  ResultSet message = preparedStatement.executeQuery();
			
			  SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  	  
			  if(message.next()){
		      String strTime = timeFormat.format(message.getTimestamp("time"));
		      String pic = message.getString("picture");
		      if(!pic.equals("SYSTEM")){
		    	  pic = "<br/><img src='" + pic + "'/>";
		      }else{
		    	  pic = "";
		      }
			  return "<p>" + message.getString("nickname") +
				": " + strTime + 
				"</p>" + message.getString("message") + 
				"<br>" + pic;    	  
			  }
		  } catch (Exception e) {
				postSystemMessage(e.getMessage());		
				return e.getMessage();
		  }
		  return "error - no 'next' in message retriever";
	  }
	  @Override
	  public void init(){
		InitialContext cxt;
		DataSource ds;
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
		
		
			  PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(index) AS i FROM " + getTable() + ";");

			  ResultSet initialMessages = preparedStatement.executeQuery();
			
		      while (initialMessages.next()) {
			  	  lastAdded = initialMessages.getInt("i") - 1;	    	  
	          }
		      
		      if(lastAdded == -1){	      
		    	  postSystemMessage("Начало.");
		      }else{
		    	  preparedStatement = connection.prepareStatement("SELECT COUNT(index) AS i FROM " + getTable() + " WHERE nickname != 'SYSTEM';");
		    	  ResultSet scarfsCountResult = preparedStatement.executeQuery();
					
			      while (scarfsCountResult.next()) {
				  	  totalScarfs = scarfsCountResult.getInt("i");	    	  
		          }
		      }
		
		} catch (Exception e) {
			postSystemMessage("DB ERROR");
			postSystemMessage(e.getMessage());		
		}
	  } 
	  
	  @Override
	  public void destroy() {
		  if (connection != null)
			try {
				connection.close();
			} catch (SQLException e) {}
	  }
	  //retrieves message from database
	  @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  if(getPermissionToGet(request)){
			//encoding stuff. must be written in the beginning of every servlet
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			
		    response.setContentType("text/html");
		    PrintWriter out = response.getWriter();
		    
		    String refreshFrom = request.getParameter("refreshFrom");
		    String id = request.getParameter("id");
		    String len = request.getParameter("len");
		    
		    
		    //response structure is:
		    //if client is asking for updates:
		    //id of the last message on server # new messages for the client (we know what messages client already has
		    // with the help of refreshFrom param)
		    
		    if(refreshFrom != null && !refreshFrom.equals("")){   
		    	//serving messages between the most actual one on the server and the last message that client has acquired
		    	//--not working for some reason,commented--if(isPosInt(refreshFrom)||refreshFrom.substring(0,2).equals("-1")){
		    		out.print(lastAdded + "#" + totalScarfs + "%");
		    		int destination = Math.max(Integer.parseInt(refreshFrom),lastAdded - chatLength);
			    	for(int i = lastAdded; i > destination; i--)
			    		out.print("<div class='msg'>" + retrieveMessage(i) + "</div>");
			    	
		    	//}
		    }else if(id != null && !id.equals("") && len != null && !len.equals("")){
		    	int howMuchToDispense = 5;
		    	//if lastId is 20 and len is 10, then start is 10
		    	int start = Integer.parseInt(id) - Integer.parseInt(len);
		    	int destination = Math.max(start - howMuchToDispense, 0);
		    	if(destination == 0){
		    		out.print("0");
		    	}else{
		    		out.print("1");
		    	}
		    	for(int i = start; i >= destination; i--)
		    		out.print("<div class='msg'>" + retrieveMessage(i) + "</div>");
		    }
		    out.close();
	  }
	  }
	  
	  //adds message to the database
	  @Override
	  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	  if(getPermissionToPost(request)){	  	
		    //encoding stuff. must be written in the beginning of every servlet do method
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
		  
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
		    	String path = "/music/photoes/" + (lastAdded+1) + type;
		    	
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
			    if(!contacts.equals("")){
			    	String messageHead = name.substring(0, Math.min(39, name.length()));
			    	messageHead += " (" + contacts.substring(0, Math.min(39, contacts.length())) + ")";
			    	postMessage(messageHead, message, path);
			    }else{
			    	postMessage(name, message, path);
			    }
		    }
		    
		    output.close();
		}
	    }
	
}
