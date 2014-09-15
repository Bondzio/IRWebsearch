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
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import test.TagcloudSuggestion;

public class SolrJConnection {

	private HttpSolrServer server;
	
	public static HashMap<String, Double> totalFacets = null;
	private static final String STOPWORDS = "aber alle allem allen aller alles als also am an ander andere anderem anderen anderer anderes anderm andern anderr anders auch auf aus bei bin bis bist da damit dann der den des dem die das dass daß derselbe derselben denselben desselben demselben dieselbe dieselben dasselbe dazu dein deine deinem deinen deiner deines denn derer dessen dich dir du dies diese diesem diesen dieser dieses doch dort durch ein eine einem einen einer eines einig einige einigem einigen einiger einiges einmal er ihn ihm es etwas euer eure eurem euren eurer eures für gegen gewesen hab habe haben hat hatte hatten hier hin hinter ich mich mir ihr ihre ihrem ihren ihrer ihres euch im in indem ins ist jede jedem jeden jeder jedes jene jenem jenen jener jenes jetzt kann kein keine keinem keinen keiner keines können könnte machen man manche manchem manchen mancher manches mein meine meinem meinen meiner meines mit muss musste nach nicht nichts noch nun nur ob oder ohne sehr sein seine seinem seinen seiner seines selbst sich sie ihnen sind so solche solchem solchen solcher solches soll sollte sondern sonst über um und uns unse unsem unsen unser unses unter viel vom von vor während war waren warst was weg weil weiter welche welchem welchen welcher welches wenn werde werden wie wieder will wir wird wirst wo wollen wollte würde würden zu zum zur zwar zwischen";
	
	public SolrJConnection() {
		
	}
	
	public void buildSpellcheck() {	
		server = new HttpSolrServer("http://localhost:8983/solr");
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");
		params.set("q", "*");
		params.set("spellcheck.build", "true");
		try {
			server.query(params);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initFacets() {
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
	
	public ResultsObject getResultsForQuery(String query, boolean tagCloud) {
		return getResultsForQuery(query, 1, tagCloud);
	}
	
	public ResultsObject getResultsForQuery(String query, int page, boolean tagCloud) {
		ResultsObject obj = new ResultsObject();
		
		String[][] results = null;
		
		server = new HttpSolrServer("http://localhost:8983/solr");
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks");
		params.set("start", (page-1)*10);
		params.set("q", query);
		if(tagCloud) {
			params.set("facet.limit", -1);
		} else {
			params.set("facet.limit", 0);
		}
		
		
		try {
			QueryResponse response = server.query(params);
			SolrDocumentList queryResults = response.getResults();
			 
			obj.numResults = (int) queryResults.getNumFound();
			if(tagCloud)obj.tagCloud = getFacetData(response, query);
			obj.spellcheck = getSpellcheckingResults(response);
			
			results = new String[queryResults.size()][5];
			
			
			Map<String, Map<String, List<String>>> highlights = response.getHighlighting();
			
			for(int i = 0; i < queryResults.size(); i++) {
				SolrDocument result = queryResults.get(i);

				// RETRIEVE TITLE
				if(highlights.get(result.getFieldValue("id")).containsKey(("title"))) {
					results[i][0] = highlights.get(result.getFieldValue("id")).get("title").get(0);
				} else {
					results[i][0] = (String) result.getFieldValue("title");
				}
				
				// GET URL
				results[i][1] = (String) result.getFieldValue("url");
				
				// GET CONTENT (or the highlights thereof)
				if(highlights.get(result.getFieldValue("id")).containsKey(("content"))) {
					List<String> contentHightlights = highlights.get(result.getFieldValue("id")).get("content");
					results[i][2] = contentHightlights.get(contentHightlights.size()-1);
					
				} else {
					results[i][2] = (String) result.getFieldValue("content"); 
				}				
				results[i][3] = (String) result.getFieldValue("url");
				
				// oddly enough, this does not always work. I suppose it has to do with the "-" and character encoding problems.
				while(results[i][0] != null && results[i][0].endsWith(" - Universität Regensburg")) {
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
	
	private ArrayList<SpellcheckSuggestion> getSpellcheckingResults(QueryResponse response) {
		ArrayList<SpellcheckSuggestion> spellcheckSuggestions = new ArrayList<SpellcheckSuggestion>();
		List<Collation> spellcheckResults = response.getSpellCheckResponse().getCollatedResults();
		
		if(spellcheckResults == null) {
			return spellcheckSuggestions;
		}
		
		for(int i = 0; i < spellcheckResults.size(); i++) {
			SpellcheckSuggestion suggestion = new SpellcheckSuggestion();
			suggestion.count = (int) spellcheckResults.get(i).getNumberOfHits();
			suggestion.suggestion = spellcheckResults.get(i).getCollationQueryString();
			spellcheckSuggestions.add(suggestion);
		}
		
		Collections.sort(spellcheckSuggestions);
		return spellcheckSuggestions;
	}

	private ArrayList<TagcloudSuggestion> getFacetData(QueryResponse response, String query) {		
		HashMap<String, Double> currentFacets = new HashMap<String, Double>();
		if(response.getFacetFields() != null)
		for(int i = 0; i < response.getFacetFields().size(); i++) {
			for(int j = 0; j < response.getFacetFields().get(i).getValueCount(); j++) {
				currentFacets.put(response.getFacetFields().get(i).getValues().get(j).getName(), 
						((double) response.getFacetFields().get(i).getValues().get(j).getCount()));
			}
		}
			
		ArrayList<TagcloudSuggestion> suggestions = new ArrayList<TagcloudSuggestion>();
		
		for (Entry<String, Double> entry : currentFacets.entrySet()) {
			if(!totalFacets.containsKey(entry.getKey())) continue;
			if(query.toLowerCase().contains(entry.getKey())) continue;
			if(STOPWORDS.contains(entry.getKey().toLowerCase())) continue;
			if(entry.getValue() < 4 || entry.getValue() > 800) continue;
			double value = Math.pow(entry.getValue(),1.5);
			double totalWeight = totalFacets.get(entry.getKey());
			
			String tagCloudSuggestionTitle = entry.getKey();
			if(tagCloudSuggestionTitle.endsWith(".") || tagCloudSuggestionTitle.endsWith(",")) {
				tagCloudSuggestionTitle = tagCloudSuggestionTitle.substring(0, tagCloudSuggestionTitle.length()-2);
			}
			
			suggestions.add(new TagcloudSuggestion(tagCloudSuggestionTitle, value*totalWeight));
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
					long currentCount = response.getFacetFields().get(i).getValues().get(j).getCount();
					String currentName = response.getFacetFields().get(i).getValues().get(j).getName();
					if(currentCount < 4 || currentCount > 800) continue;
					try {
						int t = Integer.parseInt(currentName);
						double d = Double.parseDouble(currentName);
					} catch(Exception e) {
						if(currentName.length() <= 4) continue;
						double d = 1.0-Math.pow((Math.log1p((double) Math.abs(currentCount-50.0))/totalDocumentCount),2); 
						if(d > 1 || Double.compare(d,Double.NaN) == 0) d = 1.0;
						totalFacets.put(currentName, d); 				
					}
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
		public ArrayList<SpellcheckSuggestion> spellcheck;
		public ArrayList<TagcloudSuggestion> tagCloud;
	}
	
}
