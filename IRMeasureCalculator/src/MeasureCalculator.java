import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
		
		File file = new File("C:/users/Jonathan/Desktop/Log/bestCoreTest.txt");

		FileWriter logger = new FileWriter(file);	
		
		
		for(int i = 0; i < 1; i ++) {		
			server = new HttpSolrServer("http://localhost:8983/solr/bestCore");

			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("qt", "/keks");
			//params.set("qf", "content"+17+"^"+ contentBoost +".0 title"+i+"^"+ titleBoost +".0 url^"+ urlBoost +".0 important" + i + "^" + importantBoost + ".0");
			params.set("qf", "content^0.0 content_loose^16.0 title^1.0 url^16.0 important^1.0");
			
			
			double averagePrecisionAtTen = 0;
			double averagePrecisionAtOne = 0;
			double averageMRR = 0;
			double averageMAP = 0;
			double[] elevenPt = new double[] {0,0,0,0,0, 0,0,0,0,0, 0};
			int queryCounterPAt10 = 0;
			int queryCounterPAt1 = 0;
			int queryCounterMRR = 0;
			int queryCounterMAP = 0;
			int queryCounter11pt = 0;
					
			for(int j = 0; j < relevanceData.get(0).size(); j++) {
				if(j == 26) continue;
				ArrayList<String> currentQueries = relevanceData.get(0).get(j);
				ArrayList<String> currentRelevantDocs = relevanceData.get(1).get(j);
				for(int k = 0; k < currentQueries.size(); k++) {
					params.set("q", currentQueries.get(k));
						
					try {
						QueryResponse response = server.query(params);
						SolrDocumentList queryResults = response.getResults();
										
						double precisionAt10 = getPrecisionAt10(queryResults, currentRelevantDocs);
						double precisionAt1 = getPrecisionAt1(queryResults, currentRelevantDocs);
						double map = getMeanAveragePrecision(queryResults, currentRelevantDocs);
						double mrr = getMeanReciprocalRank(queryResults, currentRelevantDocs);
						double[] elevenPointData = get11ptPrecisionRecall(queryResults, currentRelevantDocs);
						logger.write("core"+i+",");
						if(precisionAt10 != -1000.0) {
							averagePrecisionAtTen += precisionAt10;
							queryCounterPAt10++;
							logger.write(precisionAt10+",");
						} else {
							logger.write("NA,");
						}
						if(precisionAt1 != -1000.0) {
							averagePrecisionAtOne += precisionAt1;
							queryCounterPAt1++;
							logger.write(precisionAt1+",");
						} else {
							logger.write("NA,");
						}
						if(map != -1000.0) {
							averageMAP += map;
							queryCounterMAP++;
							logger.write(map+",");
						} else {
							logger.write("NA,");
						}
						if(mrr != -1000.0) {
							averageMRR += mrr;
							queryCounterMRR++;
							logger.write(""+mrr);
						} else {
							logger.write("NA");
						}
						if(elevenPointData != null) {
							for(int z = 0; z < 11; z++) {
								elevenPt[z] += elevenPointData[z];
							}
						queryCounter11pt++;
						}
						logger.write("\n");
										
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
							
			for(int bla = 0; bla < 11; bla++) {
				elevenPt[bla] /= queryCounter11pt;
			}
			
			System.out.println("\tP@1: " + Math.round(averagePrecisionAtTen/queryCounterPAt10 * 100.0) / 100.0);
			System.out.println("\tP@1: " + Math.round(averagePrecisionAtOne/queryCounterPAt1 * 100.0) / 100.0);
			System.out.println("\tMAP: " + Math.round(averageMAP / queryCounterMAP * 100.0) / 100.0);
			System.out.println("\tMRR: " + Math.round(averageMRR / queryCounterMRR * 100.0) / 100.0);
			logger.write("\11pt: " + csvify(elevenPt));
			System.out.println("\n");
							
		}
		logger.write("\n");
		logger.close();
		
		System.out.println("Done");
	}
	
	public static double calculateAveragePrecisionAt10(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData, ModifiableSolrParams params) {
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

	public static double calculateAveragePrecisionAt1(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData, ModifiableSolrParams params) {
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
		if(queryResults.size() == 0) return 0;
		
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
		if(relevantDocs.size() == 0) return 0;
		for(int j = 0; j < relevantDocs.size(); j++) {
			if(queryResults.get(0).getFieldValue("id").equals(relevantDocs.get(j))) {
				return 1.0;
			}
		}
		return 0.0;
	}
	
	public static double calculateMeanAveragePrecision(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData, ModifiableSolrParams params) {
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
		if(queryResults.size() == 0) return 0;
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
		return precision/relevantDocs.size();
	}
	
	public static double calculateMeanReciprocalRank(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData, ModifiableSolrParams params) {
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
		if(queryResults.size() == 0) return 0;
		for(int i = 0; i < queryResults.size(); i++) {
			for(int j = 0; j < relevantDocs.size(); j++) {
				if(queryResults.get(i).getFieldValue("id").equals(relevantDocs.get(j))) {
					return 1.0 / (i+1);
				}
			}
		}
		return 0.0;
	}
	
	public static double[] calculate11ptPrecisionRecall(int index, ArrayList<ArrayList<ArrayList<String>>> relevanceData, ModifiableSolrParams params) throws Exception{
		double[] data = new double[11];
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
		for(int i = 9; i >= 0; i--) {
			if(data[i] <= data[i+1]) data[i] = data[i+1];
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
