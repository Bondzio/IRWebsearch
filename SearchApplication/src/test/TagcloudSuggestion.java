package test;

import java.util.Random;

public class TagcloudSuggestion implements Comparable<TagcloudSuggestion>{

	public static boolean sortRandomly = false;
	
	public double score;
	public String name;
	
	public TagcloudSuggestion(String name, double score) {
		this.name = name;
		this.score = score;
		
		sortRandomly = false;
	}

	@Override
	public int compareTo(TagcloudSuggestion another) {
		if(sortRandomly) {
			if(score == another.score) return 0;
			return new Random().nextBoolean()? 1 : -1;
		}
		else return -Double.compare(score, another.score);
	}
	
}
