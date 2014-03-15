package autocorrect;

import com.google.common.collect.HashMultiset;

public class Suggestion {
	
	private String word; //The word
	private int freq; //frequency of occurrence in corpus. //GOOD. More is better
	private int led;	//Levenshtein distance			   //BAD. Less is better
	private int rtDistance; //Distance from departure node //BAD. Usually, less is better.
	private HashMultiset<String> neighbors;			   //for a given neighbor: GOOD. More is better.
	
	Suggestion(String word) {
		//used only for testing (equality purposes)
		this.word = word;
	}

	//We need all these constructors because they get called differently by each suggester.
	// prefixes cant see levenshteins and vice versa. Its basically montagues and capulets.
	Suggestion(String word, int freq, HashMultiset<String> neighbors) {
		this.word = word;
		this.freq = freq;
		this.rtDistance = Constants.max; //We take these into account. 
		this.led = Constants.max;		//In our suggestions.
		this.neighbors = neighbors;
	}
	
	Suggestion(String word, int freq, int rtDistance, HashMultiset<String> neighbors) {
		this.word = word;
		this.freq = freq;
		this.rtDistance = rtDistance;
		this.led = Constants.max;
		this.neighbors = neighbors;
	}
	
	Suggestion(String word, int freq, int rtDistance, int led, HashMultiset<String> neighbors) {
		this.word = word;
		this.freq = freq;
		this.rtDistance = rtDistance;
		this.led = led;
		this.neighbors = neighbors;
	}
	
	public int getLED() {
		return this.led;
	}
	
	public String getWord() {
		return this.word;
	}
	
	private int smartEvaluate(String previousWord) {
		/** Evaluates suggestion as per README*/
		int val = this.freq * Constants.unigramWeight;
		if (this.rtDistance != Constants.max) //unassigned
			val += this.rtDistance * Constants.rtDistanceWeight;
		if (this.led != Constants.max)
			if (this.led == 0)
				return Constants.max;
			else
				val = val - (this.led * Constants.ledWeight);
			val += this.bigramProbability(previousWord) * Constants.bigramWeight;
		return val;
	}
	
	
	public String secondWord() {
		/** Used by rt.whitespace for filtering whitespace suggestions */
		String[] parts = this.word.trim().split("\\s+");
		return parts[parts.length - 1];
	}

	
	public int bigramProbability(String previousWord) {
		Integer val = this.neighbors.count(previousWord);
		return (val == null) ? 0 : val;
	}
	
	public int defaultCompareTo(Suggestion s, String previousWord,  String currentWord) {
		/** Default comparator used in defaultRank. Selects first for exact matches, 
		 * then checks for bigram probability, then for unigram probability, and
		 * finally performs lexicographic comparison. The returned integer is:
		 * negative if this is more valued than specified, 0 if equal, and 
		 * positive if this is less valued than specified.*/
		if (this.word.equals(s.word)) {
			return 0;
		}
		if (this.word.equals(currentWord)) {
			return -1;
		}
		if (s.word.equals(currentWord)) {
			return 1;
		}
		if (this.bigramProbability(previousWord) > s.bigramProbability(previousWord)) {
			return -1;
		}
		if (this.bigramProbability(previousWord) < s.bigramProbability(previousWord)) {
			return 1;
		}
		if (this.freq > s.freq) {
			return -1;
		}
		if (this.freq < s.freq) {
			return 1;
		}
		return (this.word.compareTo(s.word));
	}

	public int smartCompareTo(Suggestion s, String previousWord,  String currentWord) {
		/**Smart comparator for smartCompare. See README for details*/
		if (this.word.equals(s.word))
			return 0;
		if (this.word.equals(currentWord)) {
			return -1;
		}
		if (s.word.equals(currentWord)) {
			return 1;
		}
		if (this.smartEvaluate(previousWord) > s.smartEvaluate(previousWord)) //be aware a little counterintuitive.
			return -1;														  //Words with higher scores are lesser
		if (this.smartEvaluate(previousWord) < s.smartEvaluate(previousWord)) //in an ascending sort.
			return 1;
		return (this.word.compareTo(s.word));
	}

		
	
	@Override
	public String toString() {
		return this.word;
	}
	
	@Override
	public boolean equals(Object o) {
		/** A pretty soft equals - just checks if suggestions have the same string.*/
		if (o == this) return true;
		if (!(o instanceof Suggestion)) return false;
		
		Suggestion s = (Suggestion) o;
		return (this.word.trim().equals(s.word.trim()));
	}
}
