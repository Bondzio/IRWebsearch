package solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import test.TagcloudSuggestion;

public class SolrJConnection {

	private HttpSolrServer server;
	
	public static HashMap<String, Double> totalFacets = null;
	
	public static void main(String[] args) {
	//	new SolrJConnection().doStuff();
	}
	
	public SolrJConnection() {
		if(totalFacets == null) totalFacets = getTotalFacets();
	}
	
	public String[] getSuggestions(String query) {
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
			 @SuppressWarnings("deprecation")
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
	
	public ResultsObject getResultsForQuery(String query) {
		return getResultsForQuery(query, 1);
	}
	
	public ResultsObject getResultsForQuery(String query, int page) {
		ResultsObject obj = new ResultsObject();
		
		String[][] results = null;
		

		server = new HttpSolrServer("http://localhost:8983/solr");
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");
		params.set("start", (page-1)*10);
		params.set("q", query);
		
		try {
			QueryResponse response = server.query(params);
			SolrDocumentList queryResults = response.getResults();
			 
			obj.numResults = (int) queryResults.getNumFound();
			
			results = new String[queryResults.size()][4];
			
			obj.tagCloud = getFacetData(response);

			Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
			
			for(int i = 0; i < queryResults.size(); i++) {
				SolrDocument result = queryResults.get(i);
				
				if(highlights.get(result.getFieldValue("id")).containsKey(("title"))) {
					results[i][0] = highlights.get(result.getFieldValue("id")).get("title").get(0);
				} else {
					results[i][0] = (String) result.getFieldValue("title");
				}
				
			//	if(highlights.get(result.getFieldValue("id")).containsKey(("url"))) {
			//		results[i][1] = highlights.get(result.getFieldValue("id")).get("url").get(0);
			//	} else {
					results[i][1] = (String) result.getFieldValue("url");
			//	}
				
				if(highlights.get(result.getFieldValue("id")).containsKey(("content"))) {
					results[i][2] = highlights.get(result.getFieldValue("id")).get("content").get(0);
				} else {
					results[i][2] = (String) result.getFieldValue("content"); 
				}
				
				results[i][3] = (String) result.getFieldValue("url");
				
				if(results[i][0] != null && results[i][0].endsWith(" - Universität Regensburg")) {
					results[i][0] = results[i][0].substring(0, results[i][0].length() - " - Universität Regensburg".length());
				}
				
				// cut http:// or https:// from displayed url:
				results[i][1] = results[i][1].replace("&#x2F;", "/");
				results[i][1] = results[i][1].replace("https://", "");
				results[i][1] = results[i][1].replace("http://", "");
			}
					 
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		
		obj.actualResults = results;
		
		return obj;
	}
	
	private ArrayList<TagcloudSuggestion> getFacetData(QueryResponse response) {		
		HashMap<String, Double> currentFacets = new HashMap<String, Double>();
		
		for(int i = 0; i < response.getFacetFields().size(); i++) {
			for(int j = 0; j < response.getFacetFields().get(i).getValueCount(); j++) {
				currentFacets.put(response.getFacetFields().get(i).getValues().get(j).getName(), 
						((double) response.getFacetFields().get(i).getValues().get(j).getCount()));
			}
		}
			
		ArrayList<TagcloudSuggestion> suggestions = new ArrayList<TagcloudSuggestion>();
		
		for (Entry<String, Double> entry : currentFacets.entrySet()) {
			if(!totalFacets.containsKey(entry.getKey())) continue;
			double value = (entry.getValue() *2)  * (totalFacets.get(entry.getKey())*0.8);
			suggestions.add(new TagcloudSuggestion(entry.getKey(), value));
		}
		
		Collections.sort(suggestions);
		return suggestions;
	}
	
	private HashMap<String, Double> getTotalFacets() {
		HashMap<String, Double> totalFacets = new HashMap<String, Double>();
		
		server = new HttpSolrServer("http://localhost:8983/solr");
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");
		params.set("q", "*");
		
		try {
			QueryResponse response = server.query(params);	
			int totalDocumentCount = response.getResults().size();
			
			for(int i = 0; i < response.getFacetFields().size(); i++) {
				for(int j = 0; j < response.getFacetFields().get(i).getValueCount(); j++) {
					totalFacets.put(response.getFacetFields().get(i).getValues().get(j).getName(), 
							1.0 - ((double) response.getFacetFields().get(i).getValues().get(j).getCount())/totalDocumentCount);
				}
			}
			
			return totalFacets;
			
		} catch(Exception e) {
			return null;
		}
		
	}
	
	public class ResultsObject {
		public int numResults;
		public String[][] actualResults;
		public String[] relatedQueries;
		public String[] filters;
		public ArrayList<TagcloudSuggestion> tagCloud;
	}
	
}
