package test;

public class RecentSearch implements Comparable<RecentSearch>{

	public String query;
	public long date;
	public int counter;
	
	public RecentSearch(String query, long date) {
		this.query = query;
		this.date = date;
		this.counter = 0;
	};
	
	@Override
	public int compareTo(RecentSearch another) {
		int compareCounter = Integer.compare(counter, another.counter);
		if(compareCounter != 0) return -compareCounter;
		
		int compareAge = Long.compare(date, another.date);
		if(compareAge != 0) return -compareAge;
				
		return query.compareTo(another.query);
	}

}
