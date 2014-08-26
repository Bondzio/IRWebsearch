import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import au.com.bytecode.opencsv.CSVReader;


public class MainClass {

	private static HttpSolrServer server;
	
	private static String[][] relevantDocsForQueries = {
		{"https://elearning.uni-regensburg.de"}
	};
	
	public static void main(String[] args) {
		ArrayList<ArrayList<ArrayList<String>>> relevanceData = null;
		
		try {
			relevanceData = getRelevanceData();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		server = new HttpSolrServer("http://localhost:8983/solr/test1");

		
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");

		for(int i = 0; i < relevanceData.get(0).size(); i++) {
			
			ArrayList<String> currentQueries = relevanceData.get(0).get(i);
			ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(i);
			
			for(int j = 0; j < currentQueries.size(); j++) {
				params.set("q", currentQueries.get(j));
				
				try {
					QueryResponse response = server.query(params);
					SolrDocumentList queryResults = response.getResults();
					
					System.out.println("The precision @ 10 for the query " + currentQueries.get(j) + " is: " + getPrecisionAt10(queryResults, currentRelevantDocs));
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

	
	}
	
	public static double getPrecisionAt10(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		int max = queryResults.size() > 10? 10 : queryResults.size();

		if(relevantDocs.size() == 0) return -1000.0;
		
		if(max == 0) return 0;
		int hits = 0;
		
		for(int i = 0; i < max; i++) {
			for(int j = 0; j < relevantDocs.size(); j++) {
				if(queryResults.get(i).getFieldValue("id").equals(relevantDocs.get(j))) {
					hits++;
				}
			}
		}
		
		
		return hits/(double)max;
	}
	
	
	public static ArrayList<ArrayList<ArrayList<String>>> getRelevanceData() throws Exception {
		ArrayList<ArrayList<ArrayList<String>>> relevanceData = new ArrayList<ArrayList<ArrayList<String>>>();
		
		ArrayList<ArrayList<String>> queriesArrayList = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> relevantDocsForTopics = new ArrayList<ArrayList<String>>();
		
		
		 CSVReader reader = new CSVReader(new FileReader("Relevanz-phi.csv"), ';');
		    
		 List<String[]> allData = reader.readAll();
		    
		 for(int i = 0; i < 30; i++) {
		  	ArrayList<String> queries = getQueriesForIndex(i);
		   	ArrayList<String> relevantDocs = new ArrayList<String>();
		    	
		   	for(int j = 1; j < allData.size(); j++) {
		   		if(allData.get(j)[i].equals("r")) {
		   			relevantDocs.add(allData.get(j)[0]);
		   		}
		   	}
		    	
		   	queriesArrayList.add(queries);
		   	relevantDocsForTopics.add(relevantDocs);
		 }
		
		 relevanceData.add(queriesArrayList);
		 relevanceData.add(relevantDocsForTopics);
		
		 reader.close();
		 
		 return relevanceData;
	}
	
	private static ArrayList<String> getQueriesForIndex(int i) {
		ArrayList<String> queries = new ArrayList<String>();
		
		switch(i) {
		case 0:
			queries.add("immatrikulation");
			queries.add("immatrikulation zeit");
			break;
		case 1:
			queries.add("immatrikulation");
			queries.add("immatrikulation ort");
			break;
		case 2:
			queries.add("informationswissenschaft");
			queries.add("informationswissenschaft lehrplan");
			queries.add("was ist informationswissenschaft");
			break;
		case 3:
			queries.add("einführung");
			queries.add("einführungsveranstaltung");
			queries.add("einführung informationswissenschaft");
			break;
		case 4:
			queries.add("informationswissenschaft nc");
			queries.add("informationswissenschaft zulassung");
			break;
		case 5:
			queries.add("regensburg vorteile");
			queries.add("gründe für regensburg");
			break;
		case 6:
			queries.add("regensburg wohnen");
			queries.add("wohnen in regensburg");
			break;
		case 7:
			queries.add("bafög");
			queries.add("bafög antrag");
			break;
		case 8:
			queries.add("ausland");
			queries.add("austausch");
			queries.add("erasmus");
			break;
		case 9:
			queries.add("freizeit");
			queries.add("freizeitangebote");
			break;
		}
		
		return queries;		
	}
	
}
