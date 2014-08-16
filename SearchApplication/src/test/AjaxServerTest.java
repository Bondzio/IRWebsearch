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
import solr.SolrJConnection.ResultsObject;


public class AjaxServerTest extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	private SolrJConnection connect;
	
	public AjaxServerTest() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		connect = new SolrJConnection();
        
		if(request.getRequestURI().equals("/IRWebsearch/WebAppTest/suche")) {
			showResults(request, response);
		} else if(request.getRequestURI().equals("/IRWebsearch/WebAppTest")){
			doAutocomplete(request, response);
		}
	}
	// Breakpoints: FILTERS    RESULTS    OTHER TOPICS    PAGES
	private void showResults(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String relativeWebPath = "/suche.html";
		String absoluteDiskPath = getServletContext().getRealPath(relativeWebPath);
		
		String htmlPage = readFile(absoluteDiskPath, Charset.forName("utf-8"));
		PrintWriter out = response.getWriter();
		
		String page = request.getParameter("page");
		String query = request.getParameter("q");
		
		ResultsObject obj;
		
		if(page != null) {
			obj = connect.getResultsForQuery(query, Integer.parseInt(page));
		} else {
			obj = connect.getResultsForQuery(query);	
		}
		
		// Write Everything up to (including) filters
		int index = htmlPage.indexOf("<!-- BREAKPOINT: FILTERS -->");
		int nextIndex = htmlPage.indexOf("<!-- BREAKPOINT: RESULTS -->");
		out.write(htmlPage.substring(0, index));
		out.write(writeFilters(obj.filters));
		
		// Write Everything up to (including) results
		out.write(htmlPage.substring(index, nextIndex));
		out.write(writeResults(obj.actualResults));
		index = nextIndex;
		nextIndex = htmlPage.indexOf("<!-- BREAKPOINT: OTHER TOPICS -->");

		// Write Everything up to (including) related queries
		out.write(htmlPage.substring(index, nextIndex));
		out.write(writeRelatedQueries(obj.relatedQueries));
		index = nextIndex;
		nextIndex = htmlPage.indexOf("<!-- BREAKPOINT: PAGES -->");
		
		// Write Pages
		out.write(htmlPage.substring(index, nextIndex));
		if(page != null) {
			out.write(writePages(obj.numResults, query, Integer.parseInt(page)));
		} else {
			out.write(writePages(obj.numResults, query, 1));
		}
		out.write(htmlPage.substring(nextIndex, htmlPage.length()-1));
		
	}
	
	private String writeFilters(String[] filters) {
		return "";
	}
	
	private String writeResults(String[][] results) {
		return getFormattedResults(results);
	}
	
	private String writeRelatedQueries(String[] relatedQueries) {
		return "";
	}
	
	private String writePages(int numResults, String query, int active) {
		String pagination = "";

		int startNumber;
		
		if(active <= 6) startNumber = 1;
		else startNumber = active - 5;
		
		int pageNumbersShown = numResults/10;
		if(pageNumbersShown > 10) pageNumbersShown = startNumber + 9;
		if(pageNumbersShown > numResults/10) pageNumbersShown = numResults/10 + 1;
		
		if(active != 1) {
			pagination += "<a class='active' href='suche?q=" + query + "&page=" + (active-1) + "'>&lt;</a>";	
		}
		
		for(int i = startNumber; i <= pageNumbersShown; i++) {
			if(i == active) {
				pagination += "<a class='active' href='suche?q=" + query + "&page=" + i + "'>" + i + "</a>";				 
			} else {
				pagination += "<a href='suche?q=" + query + "&page=" + i + "'>" + i + "</a>";
			}
		}
		
		
		if(active != numResults/10 + 1) {
			pagination += "<a class='active' href='suche?q=" + query + "&page=" + (active+1) + "'>&gt;</a>";	
		}
		
		return pagination;
	}
	
	private String getFormattedResults(String[][] queryResults) {
		String resultString = "<ul class='result-list'>";
        			
		for(int i = 0; i < queryResults.length; i++) {
			// so url doesn't need 2 lines
			queryResults[i][1] = queryResults[i][1].replaceAll("<strong>", "");
			queryResults[i][1] = queryResults[i][1].replaceAll("</strong>", "");
			if(queryResults[i][1].length() > 95){
			
				queryResults[i][1] = queryResults[i][1].substring(0, 90) + "...";
			}
			
			resultString += "<li><h3 class='result-title'><a href='" + queryResults[i][3] +  "'>" + queryResults[i][0] +
					"</a></h3><p class='result-url'>" + queryResults[i][1] + 
					"</p><p class='result-snippet'>" + queryResults[i][2] + "</p></li>";
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
		String[] suggestions = connect.getSuggestions(query);
		
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
