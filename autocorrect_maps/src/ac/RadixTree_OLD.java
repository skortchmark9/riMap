package ac;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class RadixTree {
	/** Prefix tree which holds whole chunks of words. Most of the intense logic is done here.
	 *  Contains levenshtein, whitespace, and prefix suggestions. Nodes contain lots of information,
	 *  the space load balanced by the relative lack of nodes. (versus a normal trie)  */
	private String key;
	private int freq;
	private RadixTree[] children; //A 26 block array for alphabetical placement and instant access.
	private HashMap<String, Integer> bigramNeighbors;

	RadixTree() {
		/**Special constructor of the root node. The empty string 
		does not affect the positioning of other nodes. */
		this.key = "";
		this.freq = 0;
	}

	//private RadixTree(String word, String lastWord) {
	private RadixTree(String word) {
		/**Most basic constructor - create a new word node from scratch */
		this.key = word;
		this.freq = 1;
		this.children = null;
		if (this.bigramNeighbors == null) //Increments the number of bigram neighbors on insertion
			this.bigramNeighbors = new HashMap<>();
		Integer val = this.bigramNeighbors.get(lastWord);
		this.bigramNeighbors.put(lastWord, (val == null) ? 1 : val + 1);
	}
	
	//private RadixTree(String word,)

	private RadixTree(String word, String lastWord, Boolean isWord, RadixTree oldNode) {
		/**Makes an intermediate node: creates a new node and 
		//then places oldNode as a child. Really important. 
		 * Usually should be inserted into the array where oldNode
		 * (often called currentNode) was. */
		this.key = word;
		this.children = new RadixTree[26];
		this.children[oldNode.key.charAt(0) - 97] = oldNode;
		if (isWord) { //if the intermediate node is a word endpoint
			this.freq  = 1; 
			this.addBigramNeighbor(lastWord);
		}
	}

	private void incrementFreq() {
		/** If a word exists that is already in the tree, we only want to increase
		 *  the frequency of that node, not make a new node.*/
		this.freq++;              
	}

	public int getFreq() {
		return this.freq;
	}

	private void addBigramNeighbor(String lastWord) {
		/** Adds a bigram neighbor to a the current node.*/
		if (this.bigramNeighbors == null)
			this.bigramNeighbors = new HashMap<>();
		Integer val = this.bigramNeighbors.get(lastWord);
		this.bigramNeighbors.put(lastWord, (val == null) ? 1 : val + 1);
	}

	public boolean isWord() {
		return (this.getFreq() > 0);
	}

	public boolean isEmpty() {
		return (this.children == null);
	}

	public void populateRadixTree(String fileName) {
		/** Constructs a radix from the given text file,
		 *  inserting the contents of each line. All text
		 *  is parsed by parseString before it is inserted.*/
		String line;
		String lastWord = "";


		try {
			FileReader fileReader = new FileReader(fileName); //Attempts to read the given file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) !=null) { //Loops while bufferedReader can find a next line
				for(String word : Utils.lineParse(line)) { //passes parsing to lineParse
					if (word.length() > 0)	{ //We rrreeeallly don't want any empty strings.
						this.insert(word, lastWord);
						lastWord = word;
					}
										else if (word.length() == 1 && (word.equals("a") || word.equals("i"))) {
						this.insert(word, lastWord);
						lastWord = word;
					} //This section is commented out because I believe it would break tests.
				}		 //However, I think this is a good way to get around the problems with conjunctions
			}			 //and other non-words sneaking into the trie.
			bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
			System.out.println("ERROR: Unable to open file '" + fileName + "'");
			System.exit(1);
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
	}
	public void insert(String word) {
		/** Quick, dirty, and really risky way of inserting a word.
		 * Should only be used for testing purposes.*/
		insert(word, "");
	}

	public void insert(String word, String lastWord) {
		/** Inserts a word into the given prefix tree. If the word exists already it will
		 *  simply increase its frequency of occurring. */
		int wordIndex = word.charAt(0) - 97; //The place in the array where the focus will be
		if (this.children == null) { //populates children if there are none
			this.children = new RadixTree[26];
			this.children[wordIndex] = new RadixTree(word, lastWord);
		}
		else if (this.children[wordIndex] == null) { //Same idea
			this.children[wordIndex] = new RadixTree(word, lastWord);
		}
		else if (this.children[wordIndex].key.equals(word)) {
			this.children[wordIndex].incrementFreq();
			this.children[wordIndex].addBigramNeighbor(lastWord);
			//If the word is already in the tree.
		}
		else {
			RadixTree currentNode = this.children[wordIndex]; //the node in the position we want to insert into
			int breakpoint = Utils.findBreakPoint(currentNode.key, word); // the index at which 'word' and the key diverge.
			String currentNodePrefix = currentNode.key.substring(0, breakpoint); //first half of node
			String currentNodeSuffix = currentNode.key.substring(breakpoint, currentNode.key.length()); //second half of node
			String currentWordSuffix = word.substring(breakpoint, word.length()); //second half of word

			if (currentNode.key.length() < word.length()) {
				if (currentNodeSuffix.length() == 0) { //the word exists in the key. for example: word = cats, key = cat, CNS = 0; CWS = "s";
					currentNode.insert(currentWordSuffix, lastWord); 	//recursive insert word into currentKey
					return;
				}
			} //In all other situations, we need to make an intermediate node
			currentNode.key = currentNodeSuffix; // we set the key of the current node to its suffix, while maintaining its children
			// we make a new node with currentNode as a child and the prefix becoming the key of the intermediate node
			RadixTree intermediateNode;
			if (currentNodePrefix.equals(word)) //behavior is slightly different if intermediate node would be a word end.
				intermediateNode = new RadixTree(currentNodePrefix, lastWord, true, currentNode);
			else {
				intermediateNode = new RadixTree(currentNodePrefix, lastWord, false, currentNode);
				if (currentWordSuffix.length() > 0) //REEEEALLY don't want empty strings.
					intermediateNode.insert(currentWordSuffix, lastWord);
			}
			this.children[wordIndex] = intermediateNode;
		}
	}	

	public boolean search(String word) {
		/**tells you if a word is in the given node.
		 *  Can't use the isWord method because we need to input a word. */
		return this.getFreq(word) > 0;
	}

	public int getFreq(String word) {
		/** Note this method is overloaded. Searches for a word in the tree and returns its frequency. */
		int wordIndex = word.charAt(0) - 97;   //Traverses prefix tree looking for the 
		if (this.children == null) 
			return 0;
		else {
			RadixTree currentNode = this.children[wordIndex];

			if (currentNode == null)
				return 0;
			else if (currentNode.key.equals(word))
				return currentNode.getFreq();
			else {
				if (currentNode.key.length() > word.length())
					return 0;
				else
					return (currentNode.getFreq(word.substring(Utils.findBreakPoint(word, currentNode.key), word.length())));
			}
		}
	}

	public HashMap<String, Integer> getBigramNeighbors(String word) {
		/**Searches the tree for the bigramNeighbors of a given word. Used mostly for testing */
		int wordIndex = word.charAt(0) - 97;
		HashMap<String, Integer> defaultMap = new HashMap<>();
		if (this.children == null) 
			return defaultMap;
		else {
			RadixTree currentNode = this.children[wordIndex];
			if (currentNode == null)
				return defaultMap;
			else if (currentNode.key.equals(word))
				return currentNode.bigramNeighbors;
			else {
				if (currentNode.key.length() > word.length())
					return defaultMap;
				else
					return (currentNode.getBigramNeighbors(word.substring(Utils.findBreakPoint(word, currentNode.key), word.length())));
			}
		}
	}



	public boolean searchThru(String fileName) {
		/**Searches the tree for every word of a given text. used for testing*/
		String line;
		boolean outcome = true;

		try {
			FileReader fileReader = new FileReader(fileName); //Attempts to read the given file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) !=null) { //Loops while bufferedReader can find a next line
				for(String word : Utils.lineParse(line)) {
					if (!word.equals("") && !this.search(word)) {
						System.out.println("word not found: " + word);
						outcome = false;

					}
				}
			}
			bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
		return outcome;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////PREFIX MATCHING ////////////////////////////////////////////////////////////////////////

	public List<Suggestion> getPrefixSuggestions(String word) {
		/** This method allows users to specify a maxDistance
		 *  at which they want the search to end. Initialized
		 *  with the empty string as an accumulator.*/
		return this.getPrefixSuggestions("", word, Constants.maxRTD);
	}
	public List<Suggestion> getPrefixSuggestions(String word, int maxDistance) {
		/** This method allows users to specify a maxDistance
		 *  at which they want the search to end. Initialized
		 *  with the empty string as an accumulator.*/
		return this.getPrefixSuggestions("", word, maxDistance);
	}


	private List<Suggestion> getPrefixSuggestions(String carriedWord, String fullWord, int maxDistance) {
		/** This is the real recursive engine of getSuggestions.
		 *  It follows the tree down to the point at which all
		 *  child nodes will be suffixes of the 'fullWord' and
		 *  then calls findAllSuffixes. */
		String carriedString = carriedWord + this.key;
		LinkedList<Suggestion> results = new LinkedList<>();
		if (carriedString.length() == fullWord.length())
			return results;
		int wordIndex = fullWord.charAt(carriedString.length()) - 97;

		if (this.children == null)
			return results;
		else {
			RadixTree followingNode = this.children[wordIndex];
			int fwLength = fullWord.length();
			String potentialWord;

			if (followingNode == null)  //If there is no following node, there are no suffixes in the tree
				return results;
			else
				potentialWord = carriedString + followingNode.key; //If there is a following node, we want it!

			if ((potentialWord).equals(fullWord)) { //If the node matches the full word, then we're done.
				//			System.out.println("branch2: " +  (potentialWord));
				results = followingNode.findAllSuffixes(carriedString, 1, maxDistance); //We find all suffixes after it
				if (followingNode.isWord()) //And if it is itself a word, we'll add it first to our list.
					results.addFirst(new Suggestion(potentialWord, followingNode.getFreq(), 0, followingNode.bigramNeighbors));
				return results;
			}
			else {
				if ((potentialWord).length() > fwLength) { //It might be that we've traversed the whole tree but the word itself doesn't exist in the tree.
					//However, suffixes might exist. If 'potentialWord' is a suffix of fw, then their breakpoint will be where fw ends.
					// If that is the case, we'll want potentialWord, and also its children.
					//			System.out.println("branch3: " + this.toString() + " carriedString: " + carriedString + " Current Node: " + followingNode.toString());
					if (Utils.findBreakPoint(potentialWord, fullWord) == fwLength) { //
						results.addAll(followingNode.findAllSuffixes(fullWord, 1, maxDistance));
						results.addFirst(new Suggestion(potentialWord, followingNode.getFreq(), 0, followingNode.bigramNeighbors));
					}
					return results;
				}
				else { //This is the most likely case: the 'potentialword' is shorter than the 'fullWord', so we'll keep searching the tree.
					//			System.out.println("branch4: " + followingNode.toString());
					return (followingNode.getPrefixSuggestions(carriedString, fullWord, maxDistance));
				}
			}
		}
	}

	private LinkedList<Suggestion> findAllSuffixes(String fullWord, int level, int maxDistance) {
		/** This is a part of the prefix matching segment. Once another method has navigated 
		 *  to the end node of a given word, this function will find all suffixes that follow it, 
		 *  up to a maxDistance of Constants.numSuffixes.*/
		LinkedList<Suggestion> results = new LinkedList<>();
		if (level > maxDistance)
			return results;
		else {
			if (this.children == null)
				return results;
			else {
				for (RadixTree child : this.children) { //Adds all children to the list of suggestions
					if (child!=null && child.isWord()) { //Freq checks if they are words.
						results.add(new Suggestion(fullWord + this.key + child.key, child.getFreq(), level, child.bigramNeighbors));
					}
				}
				for (RadixTree child : this.children){ //Recursively searches through children.
					if (child != null && child.children != null) {
						results.addAll(child.findAllSuffixes(fullWord + this.key, level + 1, maxDistance));
					}
				}
				return results;
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	//////////////////////////////////////////////// LEVENSHTUFF //////////////////////////////////////////////////////////////////////////

	public List<Suggestion> levenshtein(String rootWord, int maxLED) {
		/** Creates a list of suggestions with their levenshtein
		 *  distances. Uses a dynamic matrix approach, passing
		 *  along a row for each node which represents the 
		 *  levenshtein distances of the words before them.
		 *  This function serves as the initializer/interface
		 *  for nodeLD and cld. */
		List<Suggestion> results = new ArrayList<>();
		int[] initRow = new int[rootWord.length() + 1];
		for (int i = 0; i <= rootWord.length(); i++) //Initializes the array 1,2....n
			initRow[i] = i;							 //This represents the amount of edits required
		for(RadixTree child : this.children)		 //To reach the empty string.
			if(child != null)
				results.addAll(child.nodeLD(rootWord, "", initRow, maxLED)); //Passes along the rest of the
		return results; 											 //work to nodeLD.
	}


	private List<Suggestion> nodeLD(String rootWord, String wordFragment, int[] previousRow, int maxLED) {
		/** Another intermediate method. This method parses multiple character nodes into
		 *  more manageable pieces for cld to compute. It also handles the maximum
		 *  levenshtein distances we want to allow.  */
		List<Suggestion> results = new LinkedList<>();
		int[] currentRow = previousRow; //A little confusing: we need to pass the previous
		//row, to cld. However, we want this to update after every run of cld, which is why we
		//don't use the 'previousRow' parameter directly. 
		for(int i = 1; i <= this.key.length(); i++)
			currentRow = this.cld(rootWord, currentRow, wordFragment + this.key.substring(0, i));

		int led = currentRow[rootWord.length()]; //These levenshtein matrices hold the final
		if (led <= maxLED)			 		 //LED value in their bottom right corner.
			if (this.isWord())				 //Ours is 1D,so  it's just the rightmost slot.
				results.add(new Suggestion(wordFragment + this.key, this.getFreq(), Constants.max, led, this.bigramNeighbors));
		if (this.children != null) //We don't have the info here to set the rtDistance now. ^
			for(RadixTree rt : this.children) //recur on children.
				if (rt != null)
					results.addAll(rt.nodeLD(rootWord, wordFragment + this.key, currentRow, maxLED));

		return results;
	}

	private int[] cld(String rootWord, int[] previousRow, String wordFragment) {
		/** The computation gets done here. Hence, Compute Levenshtein Distance = cld.
		 *  Finds the minimum distance based on analysis of the previous row and
		 *  the slots filled in already.*/
		//		System.out.println("this word: " + wordFragment);
		//		System.out.println("this previous row: " + Arrays.toString(previousRow));
		int[] currentRow = new int[rootWord.length() + 1];
		currentRow[0] = previousRow[0] + 1; //Remember - the first column is deletion to the empty string.
		for (int j = 0; j < rootWord.length(); j++) {
			currentRow[j + 1] = Utils.minimum(
					previousRow[j + 1] + 1,    //cost of deletion
					currentRow[j] + 1, //cost of insertion
					previousRow[j] +   //cost of replacement
					((//(j) < wordFragment.length() && 
							rootWord.charAt(j) == wordFragment.charAt(wordFragment.length() - 1)) ? 0 : 1));
		}
		return currentRow;
	}	


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////// WHITESPACE ////////////////////////////////////////////////////////////////////////////

	private List<Suggestion> possibleWhiteSpaceBreaks(String currentWord) {
		/** Returns all possible suggestions in which the first word is definitely a word.*/
		int wordIndex;
		RadixTree thisNode = this;
		List<Suggestion> results = new LinkedList<>();
		for (int i = 0; i < currentWord.length(); i++) {			
			wordIndex = currentWord.charAt(i) - 97;
			if (thisNode.children != null) {
				RadixTree currentNode = thisNode.children[wordIndex];
				if (currentNode != null) {
					//					System.out.println("Here is the current node: " + currentNode.toString() + " its freq: " + currentNode.getFreq());
					if (currentNode.isWord()) {
						int breakpoint = i + currentNode.key.length();
						if (breakpoint < currentWord.length())
							results.add(new Suggestion(currentWord.substring(0, breakpoint) + " " + currentWord.substring(breakpoint, currentWord.length()), 
									currentNode.freq, currentNode.bigramNeighbors));
					}
					thisNode = currentNode;
				}
			}
		}
		return results;
	}
	
	public List<Suggestion> whitespace(String currentWord) {
		/** Filters out all possible suggestions in which the second word is NOT a word.*/
		List<Suggestion> results = new LinkedList<>();
		for(Suggestion s : possibleWhiteSpaceBreaks(currentWord)) {
			if (this.search(s.secondWord()))
				results.add(s);
		}
		return results;
	}





	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return this.key;
	}
}

