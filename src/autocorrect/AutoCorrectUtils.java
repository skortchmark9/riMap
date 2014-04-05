package autocorrect;

public class AutoCorrectUtils {
	/** Contains some useful generic stuff for testing as well as the parser we use everywhere.. */
	
	public static int computeLevenshteinDistance(String str,String rootWord) {
		/** Reference implementation of levenshtein distance which uses a 2D array.
		 *  Used in levenshtein testing.*/
		int[][] distance = new int[str.length() + 1][rootWord.length() + 1];
 
		for (int i = 0; i <= str.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= rootWord.length(); j++)
			distance[0][j] = j;
 
		for (int i = 1; i <= str.length(); i++)
			for (int j = 1; j <= rootWord.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]+ 
						((str.charAt(i - 1) == rootWord.charAt(j - 1)) ? 0 : 1));
		
		return distance[str.length()][rootWord.length()];
	}

	public static int minimum(int a, int b, int c) {
		return Math.min(c, Math.min(a, b));
	}
	
	public static String[] lineParse(String line) {
		/** replaceAll functions as a delete here: Constants.parsePattern specifies the characters
		 *  we don't want to keep -- chars to watch out for: ! ' " / ? . , - 0-9, & (
		 *  the array it spits out isn't always perfect about whitespace - so we need to check to
		 *  make sure we don't insert the #*!U)*# empty string.
		 */
		return line.toLowerCase().replaceAll(AutoCorrectConstants.parsePattern, " ").trim().split("\\s+");
	}
	
	public static String arrayParse(String[] array) {
		/** pair it with lineParse. Turns its input back into one string.*/
		String currentString = "";
		for(String s : array)
			currentString = currentString + s + " ";
		return currentString.trim();
	}
	
	static int findBreakPoint(String string1, String string2) {
		/** This method finds the index at which two strings diverge.
		 *   cat| <- here it is!
		 *   catastrophe */
		String shorterString = string1;
		String longerString = string1;
		if (string1.length() < string2.length()) {
			longerString = string2;
		}
		else 
			shorterString = string2;
		int breakpoint = shorterString.length(); //default is when one is a prefix of the other.
		for(int i = 0; i < shorterString.length(); i++) {
			if (longerString.charAt(i) != shorterString.charAt(i)) {
				breakpoint = i;
				break;
			}
		}
		return breakpoint;
	}
}