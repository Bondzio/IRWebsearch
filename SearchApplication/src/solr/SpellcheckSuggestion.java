package solr;

public class SpellcheckSuggestion implements Comparable<SpellcheckSuggestion>{

	public int count;
	public String suggestion;
	@Override
	public int compareTo(SpellcheckSuggestion another) {
		return -Integer.compare(count, another.count);
	}
	
	
}
