package autocorrect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.HashMultiset;


public class RadixTree {
	/** Prefix tree which holds whole chunks of words. Most of the intense logic is done here.
	 *  Contains levenshtein, whitespace, and prefix suggestions. Nodes contain lots of information,
	 *  the space load balanced by the relative lack of nodes. (versus a normal trie)  */
	private String key;
	private int freq;
	private RadixTree[] children; //A 26 block array for alphabetical placement and instant access.
	private HashMap<Character, RadixTree> _children = null;
	private HashMultiset<String> bigramNeighbors = null;

	
	/**
	 * Special constructor of the root node. The empty string 
	 * does not affect the positioning of other nodes. 
	 */
	RadixTree() {
		this.key = "";
		this.freq = 0;
	}
	
	
	/**
	 * Most basic constructor - create a new word node from scratch
	 * Adds the previous node the the MultiSet.
	 * @param word - the word to use as the key at this node
	 */
	private RadixTree(String word, String lastWord) {
		this.key = word;
		this.freq = 1;
		addBigramNeighbor(lastWord);
	}

	/**
	 * Makes an intermediate node: creates a new node and 
	 * then places oldNode as a child. Really important. 
	 * Usually should be inserted into the Map where oldNode
	 * (often called currentNode) was.
	 *  
	 * @param word - word to use as a key at this node
	 * @param isWord - true if this word is an end point (full word)
	 * @param oldNode - old node that this node replaces, becomes child of this node
	 */
	private RadixTree(String word, String lastWord, Boolean isWord, RadixTree oldNode) {
		this.key = word;
		_children = new HashMap<>();
		_children.put(oldNode.key.charAt(0), oldNode);
		if (isWord) { //if the intermediate node is a word end point
			this.freq  = 1;
			addBigramNeighbor(lastWord);
		}
	}

	
	/** 
	 * Increment the unigram frequency of this node. 
	 */
	private void incrementFreq() {
		this.freq++;              
	}
	
	/**
	 * Add an occurrence of the parameter word to this node's
	 * set of Bigram's.
	 * @param lastWord - the word to add to the Bigram list.
	 */
	private void addBigramNeighbor(String lastWord) {
		if (bigramNeighbors == null)
			bigramNeighbors = HashMultiset.create();
		bigramNeighbors.add(lastWord);
	}
	
	/**
	 * @return
	 * the unigram frequency of this node
	 */
	public int getFreq() {
		return this.freq;
	}
	
	/**
	 * @return
	 * True if this node is a end point (a full word)
	 * The node is considered a word if its unigram frequency is
	 * greater than 1.
	 */
	public boolean isWord() {
		return (this.getFreq() > 0);
	}
	
	
	/**
	 * @return
	 * True if this node's HashMap contains no elements (children)
	 */
	public boolean isEmpty() {
		return (_children.isEmpty());
	}
	
	
	/**
	 * Constructs a radix from the given text file,
	 * inserting the contents of each line. All text
	 * is parsed by parseString before it is inserted.
	 * @param fileName - path to the file to add.
	 */
	public void populateRadixTree(String fileName) {
		String line;
		String lastWord = "";
		
		try {
			FileReader fileReader = new FileReader(fileName); //Attempts to read the given file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			while((line = bufferedReader.readLine()) !=null) { //Loops while bufferedReader can find a next line
				for(String word : Utils.lineParse(line)) { //passes parsing to lineParse
					if (word.length() > 0)	{ //We really don't want any empty strings.
						this.insert(word, lastWord);
						lastWord = word;
					}
				}
			}
			
			bufferedReader.close(); 
		} catch(FileNotFoundException ex) {
			System.out.println("ERROR: File cannot be found at location " + fileName);
			System.exit(1);//GTFO
		} catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
	}

	/**
	 * Quick n Dirty insert for testing
	 * @param word
	 */
	public void insert(String word) {
		insert(word, "");
	}
	
	/**
	 * Inserts a word into the given prefix tree. If the word exists already it will
	 * simply increase its frequency of occurring.
	 * @param word
	 * @param lastWord
	 */
	public void insert(String word, String lastWord) {
		char firstChar = word.charAt(0);
		
		if (_children == null) { //populates children if there are none
			_children = new HashMap<>();
			_children.put(firstChar, new RadixTree(word, lastWord));
		} else if (!_children.containsKey(firstChar)) { //Same idea; if there is no child for firstChar
			_children.put(firstChar, new RadixTree(word, lastWord));
		} else {
			//we know there is a child stored at firstChar
			RadixTree child = _children.get(firstChar);
			if (child.key.equals(word)) { //check if word contained in Tree already
				child.incrementFreq();
				child.addBigramNeighbor(lastWord);
			} else {
				//need to do some manipulation of tree structure:
				int breakPoint = Utils.findBreakPoint(child.key, word); //find index at which 'word' and the key diverge
				String childPrefix = child.key.substring(0, breakPoint);
				String childSuffix = child.key.substring(breakPoint);
				String wordSuffix  = word.substring(breakPoint);
				
				if (child.key.length() < word.length()) {
					if (childSuffix.length() == 0) { //word exists in key.
						child.insert(wordSuffix, lastWord); //recur to insert word as child's key
						return;
					}
				} //in all other situations, make an intermediate node:
				child.key = childSuffix; //set key of child to suffix, maintain all children.
				RadixTree intermediateNode;
				if (childPrefix.equals(word)) //behavior is slightly different if intermediate's key is word end
					intermediateNode = new RadixTree(childPrefix, lastWord, true, child);
				else {
					intermediateNode = new RadixTree(childPrefix, lastWord, false, child);
					if (wordSuffix.length() > 0) //avoid empty strings
						intermediateNode.insert(wordSuffix, lastWord);
				}
				_children.put(firstChar, intermediateNode);
			}
		}
	}	
	
	/**
	 * Returns true if the parameter word is in this node
	 * @param word - the word to search for
	 * @return true if the word can be found in this node, false otherwise.
	 */
	public boolean search(String word) {
		return this.getFreq(word) > 0;
	}
	
	/**
	 * This method is overloaded.
	 * Searches for a word in the tree and
	 * returns its frequency.
	 * 
	 * @param word - word to find frequency of.
	 * @return - frequency of the parameter word in this node
	 */
	public int getFreq(String word) {
		char firstChar = word.charAt(0);
		
		if (_children == null || _children.isEmpty()) return 0;
		
		else {
			RadixTree currentNode = _children.get(firstChar);
			if (currentNode == null)
				return 0;
			else if (currentNode.key.equals(word))
				return currentNode.freq;
			else {
				if (currentNode.key.length() > word.length())
					return 0;
				else
					return (currentNode.getFreq(word.substring(Utils.findBreakPoint(word, currentNode.key), word.length())));
			}
		}
	}
	
	
	/**
	 * Searches the tree for the bigramNeighbors of a given word. 
	 * Used mostly for testing 
	 * @param word - word to find bigramNeighbors of.
	 * @return a map of the bigram neighbors.
	 */
	public HashMap<String, Integer> getBigramNeighbors(String word) {
		char firstChar = word.charAt(0);
		HashMap<String, Integer> defaultMap = new HashMap<>();
		
		if (_children == null || _children.isEmpty()) return defaultMap;
		
		else {
			RadixTree currentNode = _children.get(firstChar);
			if (currentNode == null)
				return defaultMap;
			else if (currentNode.key.equals(word)) {
				//convert hashMultiset to HashMap
				for (String str : currentNode.bigramNeighbors) {
					defaultMap.put(str, currentNode.bigramNeighbors.count(str));
				}
				return defaultMap;
			}
			else {
				if (currentNode.key.length() > word.length())
					return defaultMap;
				else
					return (currentNode.getBigramNeighbors(word.substring(Utils.findBreakPoint(word, currentNode.key), word.length())));
			}
		}
	}



	/**
	 * Searches the tree for every word of a given text. used for testing
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean searchThru(String fileName) {
		String line;
		boolean outcome = true;

		try {
			FileReader fileReader = new FileReader(fileName); //Attempts to read the given file
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) !=null) { //Loops while bufferedReader can find a next line
				for(String word : Utils.lineParse(line)) {
					if (!word.equals("") && !this.search(word)) {
//						System.out.println("word not found: " + word);
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
		
		char charKey = fullWord.charAt(carriedString.length());

		if (_children == null || _children.isEmpty())
			return results;
		else {
			RadixTree followingNode = _children.get(charKey);
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
			if (_children == null || _children.isEmpty())
				return results;
			else {
				for (RadixTree child : _children.values()) { //Adds all children to the list of suggestions
					if (child!=null && child.isWord()) { //Freq checks if they are words.
						results.add(new Suggestion(fullWord + this.key + child.key, child.getFreq(), level, child.bigramNeighbors));
					}
				}
				for (RadixTree child : _children.values()){ //Recursively searches through children.
					if (child != null && child._children != null && !child._children.isEmpty()) {
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
		for(RadixTree child : _children.values())		 //To reach the empty string.
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
		if (_children != null && !_children.isEmpty()) //We don't have the info here to set the rtDistance now. ^
			for(RadixTree rt : _children.values()) //recur on children.
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
		char charKey;
		RadixTree thisNode = this;
		List<Suggestion> results = new LinkedList<>();
		for (int i = 0; i < currentWord.length(); i++) {			
			charKey = currentWord.charAt(i);
			if (thisNode._children != null && !thisNode._children.isEmpty()) {
				RadixTree currentNode = thisNode._children.get(charKey);
				if (currentNode != null) {
					System.out.println("Here is the current node: " + currentNode.toString() + " its freq: " + currentNode.getFreq());
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

