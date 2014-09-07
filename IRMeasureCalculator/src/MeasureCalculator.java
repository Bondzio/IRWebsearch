import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import au.com.bytecode.opencsv.CSVReader;


public class MeasureCalculator {

	private static HttpSolrServer server;
	
	private static int[] staticCounters = {0,0,0,0,0, 0,0,0,0,0 ,0};
	
	public static void main(String[] args) throws Exception {
		ArrayList<ArrayList<ArrayList<String>>> relevanceData = null;
		
		try {
			relevanceData = getRelevanceData();
		} catch(Exception e) {
			e.printStackTrace();
		}
		for(int z = 1; z < 12; z++) {
			server = new HttpSolrServer("http://localhost:8983/solr/weightChange");
			//System.out.println("The average precision @ 10 for the core " + z + "is: " + (calculateAveragePrecisionAt10(z, relevanceData)));
			//System.out.println("The average precision @ 1 for the core " + z + "is: " + (calculateAveragePrecisionAt1(z, relevanceData)));
			//System.out.println("The mean average precision for the core " + z + "is: " + (calculateMeanAveragePrecision(z, relevanceData)));
			//System.out.println("The mean reciprocal rank for the core " + z + "is: " + (calculateMeanReciprocalRank(z, relevanceData)));
			System.out.println("The 11pt data for the core " + z + "is: " + csvify(calculate11ptPrecisionRecall(z, relevanceData)));
		}
		System.out.println(csvify(staticCounters));
	}
	
	public static double calculateAveragePrecisionAt10(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks"+index);
		
		double averagePrecisionAtTen = 0;
		int queryCounter = 0;
		
		for(int i = 0; i < 20; i++) {
			if(i == 26) continue;
			ArrayList<String> currentQueries = relevanceData.get(0).get(i);
			ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(i);
			for(int j = 0; j < currentQueries.size(); j++) {
				params.set("q", currentQueries.get(j));
				
				try {
					QueryResponse response = server.query(params);
					SolrDocumentList queryResults = response.getResults();
					
				//	System.out.println("Core: " + z + "query: " + i + ", precision: " + getPrecisionAt10(queryResults, currentRelevantDocs));
					
					double precisionAt10 = getPrecisionAt10(queryResults, currentRelevantDocs);
					if(precisionAt10 == -1000.0) continue;
					
					averagePrecisionAtTen += precisionAt10;
					queryCounter++;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return averagePrecisionAtTen/queryCounter;
	}

	public static double calculateAveragePrecisionAt1(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks"+index);
		
		double averagePrecisionAt1 = 0;
		int queryCounter = 0;
		
		for(int i = 0; i < relevanceData.get(0).size(); i++) {
			if(i == 26) continue;
			
			ArrayList<String> currentQueries = relevanceData.get(0).get(i);
			ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(i);
			for(int j = 0; j < currentQueries.size(); j++) {
				params.set("q", currentQueries.get(j));
				
				try {
					QueryResponse response = server.query(params);
					SolrDocumentList queryResults = response.getResults();
					
				//	System.out.println("Core: " + z + "query: " + i + ", precision: " + getPrecisionAt10(queryResults, currentRelevantDocs));
					
					double precisionAt1 = getPrecisionAt1(queryResults, currentRelevantDocs);
					if(precisionAt1 == -1000.0) continue;
					
					averagePrecisionAt1 += precisionAt1;
					queryCounter++;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return averagePrecisionAt1/queryCounter;
	}
	
	public static double getPrecisionAt10(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		int max = queryResults.size() > 10? 10 : queryResults.size();

		if(relevantDocs.size() == 0) return -1000.0;
		if(queryResults.size() == 0) return -1000.0;
		
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
	
	public static double getPrecisionAt1(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		if(queryResults.size() == 0) return -1000.0;
		if(relevantDocs.size() == 0) return -1000.0;
		for(int j = 0; j < relevantDocs.size(); j++) {
			if(queryResults.get(0).getFieldValue("id").equals(relevantDocs.get(j))) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	public static double calculateMeanAveragePrecision(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks"+index);
		params.set("rows", 150);
		
		double averageMAP = 0;
		int queryCounter = 0;
		
		for(int i = 0; i < relevanceData.get(0).size(); i++) {
			if(i == 26) continue;
			
			ArrayList<String> currentQueries = relevanceData.get(0).get(i);
			ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(i);
			for(int j = 0; j < currentQueries.size(); j++) {
				params.set("q", currentQueries.get(j));
				
				try {
					QueryResponse response = server.query(params);
					SolrDocumentList queryResults = response.getResults();
					
				//	System.out.println("Core: " + z + "query: " + i + ", precision: " + getPrecisionAt10(queryResults, currentRelevantDocs));
					
					double meanPos = getMeanAveragePrecision(queryResults, currentRelevantDocs);
					if(meanPos == -1000.0) continue;
										
					averageMAP += meanPos;
					queryCounter++;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return averageMAP/queryCounter;
	}
	
	public static double getMeanAveragePrecision(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		if(relevantDocs.size() == 0) return -1000.0;
		if(queryResults.size() == 0) return -1000.0;
		int relevantDocCounter = 0;
		double precision = 0;					
		for(int i = 0; i < queryResults.size(); i++) {
			for(int j = 0; j < relevantDocs.size(); j++) {
				if(queryResults.get(i).getFieldValue("id").equals(relevantDocs.get(j))) {
					relevantDocCounter++;
					double currentPrecision = ((double) relevantDocCounter)/(i+1);
					precision += currentPrecision;
					break;
				}
			}
			if(relevantDocCounter == relevantDocs.size()) return precision/relevantDocCounter;
		}
		if(relevantDocCounter == 0) return 0.0;
		return precision/relevantDocCounter;
	}
	
	public static double calculateMeanReciprocalRank(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks"+index);
		params.set("rows", 150);
		
		double averageMRR = 0;
		int queryCounter = 0;
		
		for(int i = 0; i < relevanceData.get(0).size(); i++) {
			if(i == 26) continue;
			
			ArrayList<String> currentQueries = relevanceData.get(0).get(i);
			ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(i);
			for(int j = 0; j < currentQueries.size(); j++) {
				params.set("q", currentQueries.get(j));
				
				try {
					QueryResponse response = server.query(params);
					SolrDocumentList queryResults = response.getResults();
					
				//	System.out.println("Core: " + z + "query: " + i + ", precision: " + getPrecisionAt10(queryResults, currentRelevantDocs));
					
					double mrr = getMeanReciprocalRank(queryResults, currentRelevantDocs);
					if(mrr == -1000.0) continue;
										
					averageMRR += mrr;
					queryCounter++;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		return averageMRR/queryCounter;
	
	}
	
	public static double getMeanReciprocalRank(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		if(relevantDocs.size() == 0) return -1000.0;
		if(queryResults.size() == 0) return -1000.0;
		for(int i = 0; i < queryResults.size(); i++) {
			for(int j = 0; j < relevantDocs.size(); j++) {
				if(queryResults.get(i).getFieldValue("id").equals(relevantDocs.get(j))) {
					return 1.0 / (i+1);
				}
			}
		}
		return 0.0;
	}
	
	public static double[] calculate11ptPrecisionRecall(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData) throws Exception{
		double[] data = new double[11];
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("qt", "/keks"+index);
		params.set("rows", 150);
		
		int counter = 0;
		
		for(int i = 0; i < relevanceData.get(0).size(); i++) {
			if( i == 26 ) continue;
			
			for(int j = 0; j < relevanceData.get(0).get(i).size(); j++) {
				params.set("q", relevanceData.get(0).get(i).get(j));
				SolrDocumentList results = server.query(params).getResults();
				
				double[] currentData = get11ptPrecisionRecall(results, relevanceData.get(1).get(i));
					
				if(currentData == null) continue; 
				counter++;
				for(int k = 0; k < currentData.length; k++) {
					data[k] += currentData[k];
				}
			}
			
		}
		
		for(int i = 0; i < data.length; i++) {
			data[i] /= counter;
		}
		
		return data;
	}
	
	public static double[] get11ptPrecisionRecall(SolrDocumentList queryResults, ArrayList<String> relevantDocs) {
		double[] data = new double[] {0,0,0,0,0,0,0,0,0,0,0};
		
		if(relevantDocs.size() == 0) return null;
		
		int relevantDocCounter = 0;
		
		for(int i = 0; i < queryResults.size(); i++) {
			for(int j = 0; j < relevantDocs.size(); j++) {
				if(queryResults.get(i).getFieldValue("id").equals(relevantDocs.get(j))) {
					relevantDocCounter++;
					data[(int)(((double)relevantDocCounter)/relevantDocs.size()*10)] = ((double) relevantDocCounter) / (i+1); 
					
					staticCounters[((int)(((double)relevantDocCounter)/relevantDocs.size()*10))]++;
					
				}
			}
		}
		for(int i = 1; i < data.length; i++) {
			if(data[i] == 0) data[i] = data[i-1];
		}
		
		return data;
	}
	
	/**
	 * "CSV -  i - fy"
	 * @return
	 */
	public static String csvify(double[] data) {
		String s = "";
		for(int i = 0; i < data.length; i++) {
			s += data[i];
			if(i != data.length - 1) {
				s += ",";
			}
		}
		return s;
	}
	
	public static String csvify(int[] data) {
		String s = "";
		for(int i = 0; i < data.length; i++) {
			s += data[i];
			if(i != data.length - 1) {
				s += ",";
			}
		}
		return s;
	}
	public static ArrayList<ArrayList<ArrayList<String>>> getRelevanceData() throws Exception {
		ArrayList<ArrayList<ArrayList<String>>> relevanceData = new ArrayList<ArrayList<ArrayList<String>>>();
		
		ArrayList<ArrayList<String>> queriesArrayList = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> relevantDocsForTopics = new ArrayList<ArrayList<String>>();
		
		
		 CSVReader reader = new CSVReader(new FileReader("Relevanz-phil.csv"), ';');
		 CSVReader reader2 = new CSVReader(new FileReader("Relevanz-jon.csv"), ';');
		    
		 List<String[]> allDataPhilipp = reader.readAll();
		 List<String[]> allDataJon = reader2.readAll();
		 
		    
		 for(int i = 0; i < allDataPhilipp.get(0).length-2; i++) {
		  	ArrayList<String> queries = getQueriesForIndex(i);
		   	ArrayList<String> relevantDocs = new ArrayList<String>();
		    	
		   	for(int j = 0; j < allDataPhilipp.size(); j++) {
		   		if(allDataPhilipp.get(j)[i+1].equals("r") || allDataJon.get(j)[i+1].equals("r")) {
		   			relevantDocs.add(allDataPhilipp.get(j)[0]);
		   		}
		   	}
		    	
		   	queriesArrayList.add(queries);
		   	relevantDocsForTopics.add(relevantDocs);
		 }
		
		 relevanceData.add(queriesArrayList);
		 relevanceData.add(relevantDocsForTopics);
		
		 reader.close();
		 reader2.close();
		 
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
		case 10:
			queries.add("stellenangebote informationswissenschaft");
			queries.add("offene stellen informationswissenschaft"); 
			queries.add("stellen informationswissenschaft");
			break;
		case 11:
			queries.add("software");
			queries.add("software kostenlos");
			break;
		case 12:
			queries.add("barrierefreiheit");
			queries.add("behinderung");
			queries.add("rollstuhl");
			break;
		case 13:
			queries.add("essen");
			queries.add("mensa");
			break;
		case 14:
			queries.add("lageplan");
			queries.add("karte");
			queries.add("übersichtskarte");
			break;
		case 15:
			queries.add("stipendium");
			queries.add("stipendien");
			queries.add("förderung");
			break;
		case 16:
			queries.add("preis");
			queries.add("preise");
			queries.add("auszeichnungen");
			break;
		case 17:
			queries.add("study abroad");
			queries.add("international");
			break;
		case 18:
			queries.add("shk antrag");
			queries.add("shk information");
			break;
		case 19:
			queries.add("spind");
			queries.add("spindvergabe");
			break;
		case 20:
			queries.add("grips");
			queries.add("elearning"); 
			break;
		case 21:
			queries.add("flexnow");
			break;
		case 22:
			queries.add("florian meier mail");
			break;
		case 23:
			queries.add("informationswissenschaft fachschaft");
			queries.add("sim");
			break;
		case 24:
			queries.add("prüfungsamt öffnungszeit");
			queries.add("prüfungsamt");
			break;
		case 25:
			queries.add("hisqis");
			queries.add("immatrikulationsbescheinigung");
			break;
		case 27:
			queries.add("kurse");
			queries.add("kursangebot");
			queries.add("lsf");
			break;
		case 28:
			queries.add("josef hilz");
			break;
		case 29:
			queries.add("formatvorlage informationswissenschaft");
			queries.add("formatvorlage slk");
			break;
		}
		
		return queries;		
	}
	
}
