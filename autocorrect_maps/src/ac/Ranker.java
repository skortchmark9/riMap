package ac;

import java.util.Comparator;

public class Ranker implements Comparator<Suggestion> {

	public static final int defaultRanker = 0;
	public static final int smartRanker = 1;
	private int compareMode = defaultRanker;
	
	private String currentWord; //We need these to pass to suggestion comparator
	private String previousWord; //for computing bigram probabilities and exact matches.
	
	Ranker(int compareMode) {
		/**The ranker gets currentWord and previousWord changed in the engine
		 * before each ranking, so there's no good reason to need a currentWord
		 *  or previousWord. */
		this.compareMode = compareMode;
	}
	
	Ranker(String currentWord, String previousWord) {
		/** Basic constructor, uses defaultRanker*/
		this.currentWord = currentWord;
		this.previousWord = previousWord;
	}
	
	Ranker(int compareMode, String previousWord,  String currentWord) {
		/** Constructor which allows you to switch into SmartRank
		 *  @Param compareMode can be 0 (for defaultRanker) or 1 (for SmartRank)*/
		this.compareMode = compareMode;
		this.currentWord = currentWord;
		this.previousWord = previousWord;
	}
	
	public void setCurrentWord(String word) {
		/** Used by the engine before every run.*/
		this.currentWord = word;
	}
	
	public void setPreviousWord(String word) {
		this.previousWord = word;
	}

		
	@Override
	public int compare(Suggestion s1, Suggestion s2) {
		/** Compare method which deploys one of two ranking methods*/
		switch (this.compareMode) {
		case smartRanker:
			return s1.smartCompareTo(s2, this.previousWord, this.currentWord);
		default:
			return s1.defaultCompareTo(s2, this.previousWord, this.currentWord);	
		}
	}
}
