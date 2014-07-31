package test;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.noggit.CharArr;
import org.noggit.JSONWriter;

import solr.SolrJConnection;


public class AjaxServerTest extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	public AjaxServerTest() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.getRequestURI());
		
		response.setContentType("application/json");
		
		
        PrintWriter out = response.getWriter();
		
        String query = request.getParameter("query");
        
        SolrJConnection connect = new SolrJConnection();
		String[] suggestions = connect.doStuff(query);
		
		String resultString = "[";
		for(int i = 0; i < suggestions.length; i++) {
			resultString += "\"" +suggestions[i] + "\"";
			if(i != suggestions.length-1) resultString += ",";
		}
		resultString += "]";
		System.out.println("Results: "+ resultString);
		
		out.println("{");
		out.println("\"suggestions\":"+resultString);
		out.println("}");
		
		out.close();		
		
	}
		
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Server received something!!");
		response.setContentType("text/html");
		response.getWriter().println("<responseFromServer>hi " + request.getParameter("name") + "</responseFromServer>");
		response.getWriter().close();
	}
	
	
	
}
