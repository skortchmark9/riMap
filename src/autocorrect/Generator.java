package autocorrect;

import java.util.LinkedList;

public class Generator {
	/** Responsible for generating Suggestions for Ranker to Rank. Only to be used by engine.*/
	private Boolean prefix = false;
	private int led = 0;
	private Boolean whitespace = false;

	public enum SuggestType { //Type of generation we want. Levenshtein is easily represented by a non-zero number
		PREFIX, WHITESPACE
	}

	public Generator(int ledNum, SuggestType ... a) {
		this.led = ledNum;
		for(SuggestType s : a) {
			if (s.equals(SuggestType.PREFIX))
				this.prefix = true;
			else if (s.equals(SuggestType.WHITESPACE))
				this.whitespace = true;
		}
	}

	/**
	 * Generates all suggestions for autocorrection - if the previous word is
	 * not the empty string, then it filters out all suggestions whose bigram
	 * probability is 0. 
	 * @param previousWord - the previous word of input. 
	 * @param currentWord - the current word of input.
	 * @param rt - the radixTree. 
	 * @return - the 
	 */
	public LinkedList<Suggestion> generateSuggestions(String previousWord, String currentWord, RadixTree rt) {
		LinkedList<Suggestion> allSuggestions = new LinkedList<>();
		if (this.prefix)
			allSuggestions.addAll(rt.getPrefixSuggestions(currentWord));
		if (this.whitespace)
			allSuggestions.addAll(rt.whitespace(currentWord));

		allSuggestions.addAll(rt.levenshtein(currentWord, this.led));
		if (previousWord.length() > 0) {
			LinkedList<Suggestion> filteredSuggestions = new LinkedList<>();
			for(Suggestion s : allSuggestions) {
				if (s.bigramProbability(previousWord) > 0) {
					filteredSuggestions.add(s);
				}
			}
			return filteredSuggestions;
		} else {
			return allSuggestions;
		}
	}
}
