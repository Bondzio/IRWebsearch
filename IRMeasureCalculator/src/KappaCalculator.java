import java.io.FileReader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;


public class KappaCalculator {
	
	private static String[] ratingsPhilipp, ratingsJon;
	
	public static void main(String[] args) throws Exception{
		 CSVReader reader = new CSVReader(new FileReader("Relevanz-phil.csv"), ';');
		 ratingsPhilipp = getRatings(reader.readAll());
		 reader.close();
		 
		 reader = new CSVReader(new FileReader("Relevanz-jon.csv"), ';');
		 ratingsJon= getRatings(reader.readAll());
		 reader.close();
		 
		 System.out.println("the kappa value is: " + calculateKappa(ratingsPhilipp, ratingsJon));
	}
	
	private static String[] getRatings(List<String[]> allData) {
		// Magic number -2: the URLs are in the csv twice (and have nothing to do with the rating values!)
		String[] ratings = new String[allData.size()*(allData.get(0).length-2)];
		
		for(int i = 0; i < allData.size(); i++) {
			// Starts from 1 and goes to -2: same reason as stated above (URLs are in the .csv before and after the ratings table!)
			for(int j = 1; j < allData.get(0).length-1; j++) {
				ratings[i * (allData.get(0).length-2) + j-1] = allData.get(i)[j];
			}
		}
		
		for(String s : ratings) {
			System.out.print(s + ",");
		}
		System.out.println();
		
		return ratings;
	}
	
	private static double calculateKappa(String[] ratings1, String[] ratings2) {
		if(ratings1.length != ratings2.length) {
			System.out.println("Kappa cannot be calculated, different number of ratings!!");
			return -42;
		}
		// Kappa = (totalAgreement - randomAgreement) / ( 1 - randomAgreement)
		double totalAgreement, randomAgreement, pRel, pIrrel;
		
		int sameRatingCount = 0, differentRatingCount = 0, relevantCounter1 = 0, relevantCounter2 = 0;
		
		for(int i = 0; i < ratings1.length; i++) {
			if(ratings1[i].equals("r")) relevantCounter1++;
			if(ratings2[i].equals("r")) relevantCounter2++;
			
			if(ratings1[i].equals(ratings2[i])) sameRatingCount++;
			else differentRatingCount++;
		}
		
		pRel = ((double)(relevantCounter1 + relevantCounter2)) / (ratings1.length + ratings1.length);
		// pIrrel is simply 1 - pRel, but just to add the possibility of making a typo, here goes this line:
		pIrrel = ((double)((ratings1.length - relevantCounter1) + (ratings1.length - relevantCounter2))) / (ratings1.length + ratings1.length);
		
		randomAgreement = pRel*pRel + pIrrel * pIrrel;
		
		totalAgreement = ((double)sameRatingCount) / ratings1.length;
		
		System.out.println("Some fun data:\n"
				+ "Total ratings (topics * docs): " + ratings1.length + "\n"
				+ "Agreement: " + sameRatingCount + " (" + totalAgreement + ")\n"
				+ "Likelihood for 'relevant': " + pRel + "\n"
				+ "Likelihood for 'irrelevant': " + pIrrel);
		
		return (totalAgreement - randomAgreement) / (1 - randomAgreement);
	}
	
	
}
