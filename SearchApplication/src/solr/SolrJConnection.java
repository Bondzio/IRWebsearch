package solr;

import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrJConnection {

	private HttpSolrServer server;
	
	public static void main(String[] args) {
	//	new SolrJConnection().doStuff();
	}
	
	public SolrJConnection() {
	}
	
	public String[] doStuff(String query) {
		String[] results = null;
		server = new HttpSolrServer("http://localhost:8983/solr");
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/suggest_phrase");
		params.set("q", query);

		 
		try {
			QueryResponse response = server.query(params); 
			 SpellCheckResponse sr = response.getSpellCheckResponse();
			 if(sr == null) return new String[]{};  
			 List<Suggestion> suggestions = sr.getSuggestions();
			 if(suggestions == null || suggestions.size() == 0) return new String[]{};
			 List<String> realSugg = suggestions.get(0).getSuggestions();
			 results = new String[realSugg.size()];
			 for(int i = 0; i < realSugg.size(); i++) { 
				 results[i] = realSugg.get(i);
			 }	 
			 
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		return results;
	}
	
	public String[][] getResultsForQuery(String query) {
		String[][] results = null;
		

		server = new HttpSolrServer("http://localhost:8983/solr");
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");
		params.set("q", query);
		
		try {
			QueryResponse response = server.query(params);
			SolrDocumentList queryResults = response.getResults();
			 
			results = new String[queryResults.size()][3];
			
			for(int i = 0; i < queryResults.size(); i++) {
				SolrDocument result = queryResults.get(i);
				results[i][0] = (String) result.getFieldValue("title");
				results[i][1] = (String) result.getFieldValue("url");
				results[i][2] = (String) result.getFieldValue("content");
			}
			 
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		
		return results;
	}
	
}
