package autocorrect;

import java.util.Comparator;

public class Ranker implements Comparator<Suggestion> {

	private CompareMode mode = CompareMode.DEFAULT;
	
	private String currentWord; //We need these to pass to suggestion comparator
	private String previousWord; //for computing bigram probabilities and exact matches.
	
	public enum CompareMode {DEFAULT, SMART};
	
	public Ranker(CompareMode mode) {
		/**The ranker gets currentWord and previousWord changed in the engine
		 * before each ranking, so there's no good reason to need a currentWord
		 *  or previousWord. */
		this.mode = mode;
	}
	
	Ranker(String currentWord, String previousWord) {
		/** Basic constructor, uses defaultRanker*/
		this.currentWord = currentWord;
		this.previousWord = previousWord;
	}
	
	Ranker(CompareMode mode, String previousWord,  String currentWord) {
		/** Constructor which allows you to switch into SmartRank
		 *  @Param compareMode can be 0 (for defaultRanker) or 1 (for SmartRank)*/
		this.mode = mode;
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
		switch (this.mode) {
		case SMART:
			return s1.smartCompareTo(s2, this.previousWord, this.currentWord);
		default:
			return s1.defaultCompareTo(s2, this.previousWord, this.currentWord);	
		}
	}
}
