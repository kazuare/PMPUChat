package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;

import javax.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public abstract class ServletWithLogging extends HttpServlet{
	protected boolean fatalError = false;
	public void logFatalError(String error){
		
		String logPath = "/music/errors/errorlog";
		if(!fatalError){
			fatalError = true;
			
			File f = new File(logPath);			
			try {
				f.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			Writer out;
			try {
				out = new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(logPath, true), "UTF8"
						)
					);
				out.append(new Date(new Date().getTime() + 8L * 60L * 60L * 1000L).toString())
				   .append(" - ")
				   .append(error)
				   .append("\r\n");
				
				out.flush();
				out.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

				
		}
		
	}
	
}