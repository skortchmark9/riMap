package autocorrect;

public class Constants {
	/** Holds constant information. */
	public final static String parsePattern = "\\s*[^a-z ]"; //used in lineparser for separating out words

	public final static int maxRTD = 5; //Maximum nodes past the given word we will traverse in search of suffixes
	

	public final static int maxLED = 3; //Maximum levenshtein edit distance
	public final static int max = Integer.MAX_VALUE; //For when we don't have LED or rtDistance to assign
	
	
	public final static int ledWeight = 20 ; //Weighting assigned to levenshtein distance in SmartRank
	public final static int rtDistanceWeight = 4; //Weighting assigned to rtDistance in SmartRank
	public final static int unigramWeight = 2; //Weighting assigned to unigram freq in SmartRank
	public final static int bigramWeight = 20; //Weighting assigned to bigram freqin SmartRank
	
	public final static int numResults = 5; //Number of results we want to display
}