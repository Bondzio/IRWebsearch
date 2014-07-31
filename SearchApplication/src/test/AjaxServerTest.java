package test;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import solr.SolrJConnection;


public class AjaxServerTest extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	public AjaxServerTest() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.getRequestURI());
		
		if(request.getRequestURI().equals("/IRWebsearch/WebAppTest/suche")) {
			showResults(request, response);
		} else if(request.getRequestURI().equals("/IRWebsearch/WebAppTest")){
			doAutocomplete(request, response);
		}
	}
	
	private void showResults(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String relativeWebPath = "/suche.html";
		String absoluteDiskPath = getServletContext().getRealPath(relativeWebPath);
		
		String htmlPage = readFile(absoluteDiskPath, Charset.forName("utf-8"));
		
		int index = htmlPage.indexOf("<!-- BREAKPOINT: RESULTS -->");
		
		String firstPart = htmlPage.substring(0, index);
		String secondPart = htmlPage.substring(index, htmlPage.length());
		
		PrintWriter out = response.getWriter();
		
		out.write(firstPart);
		out.write(getResults(request.getParameter("q")));
		out.write(secondPart);
	}
	
	private String getResults(String query) {
		String resultString = "<ul>";
        SolrJConnection connect = new SolrJConnection();
        		
		String[][] queryResults = connect.getResultsForQuery(query);
		for(int i = 0; i < queryResults.length; i++) {
			resultString += "<li><h3>" + queryResults[i][0] + "</h3><p>" + queryResults[i][1] +
							"</p><p>" + queryResults[i][2] + "</p></li>";
		}		
		resultString += "</ul>";
		
		resultString = new String(resultString.getBytes(), Charset.forName("ISO-8859-1"));
		return resultString;
	}
	
	private void doAutocomplete(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException{

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
	
	static String readFile(String path, Charset encoding) throws IOException {
		  byte[] encoded = Files.readAllBytes(Paths.get(path));
		  return new String(encoded, encoding);
	}
	
	
}
