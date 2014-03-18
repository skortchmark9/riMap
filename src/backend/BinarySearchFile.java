package backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Searchable file which reads from the disk, without reading the file into memory.
 * It also reads the headers of tsv style documents and parses them appropriately.
 * @author skortchm
 *
 */
public class BinarySearchFile implements AutoCloseable {

	RandomAccessFile raf; // the random access file to be searched
	private static Charset UTF8 = Charset.forName("UTF-8"); // the type of encoding.
	HashMap<String, Integer> parsePattern;
	LineMap newLines;
	String sortingCol;
	int bufferLength;
	int tabBufferLength;
	int numCalls;
	long length;
	public enum SearchType {WILDCARD, DEFAULT}

	/** 
	 * Main constructor of the BinarySearchFile.
	 * automatically closes if it fails top open it correctly.
	 * Very generic, this class allows you to specify the sorting principle and
	 * important columns in the binary search file.
	 * @param filePath - the path to the file
	 * @param pt - what kind of file it is, what information we are seeking.
	 * @throws IOException = if we can't read the file for some reason. 
	 */
	BinarySearchFile(String filePath, String sortingCol, String ...importantCols) throws IOException {
		try {
			raf = new RandomAccessFile(filePath, "r");				
		} catch(FileNotFoundException ex) {
			System.out.println("ERROR: Unable to open file '" + filePath + "'");
			System.exit(1);
		}
		bufferLength = Constants.BufferLength;
		tabBufferLength = Constants.TabBufferLength;
		this.sortingCol = sortingCol;
		newLines = new LineMap();
		length = raf.length();
		parsePattern = readHeader(nextNewLine(0, length));
		for(String colID : importantCols) {
			if (!parsePattern.containsKey(colID)) {
				System.out.println("ERROR: file header not formatted correctly");
				System.exit(0);
				break;
			}
		}
	}

	/** Attempts to read the header of an index, actors, or films file.
	 * Assumes the first line starts at 0 and headerEnd is the end of the first line.
	 * @param headerEnd - end of first line.
	 * @return a hashmap which contains the number of tabs for each keyword. 
	 */
	private HashMap<String, Integer> readHeader(long headerEnd) {
		try {
			raf.seek(0);
			byte[] line = new byte[(int) headerEnd];
			raf.read(line);
			String header = string(line);
			String[] headerPieces = Constants.tab.split(header);
			//Divides up the pieces of the header into individual columns. 
			HashMap<String, Integer> results = new HashMap<>(3);
			for(int i = 0; i < headerPieces.length; i++) {
				results.put(headerPieces[i], i);
			}
			return results;
		}  catch (IOException e) {
			return null;
		}
	}

	/**
	 * Cool function designed to minimize the number of binary searches. 
	 * If we are consulting an entry for information about its name for example,
	 * we can save ourselves a search later by also getting any information we 
	 * need from that line. 
	 * @param line - the line containing the info we want to return.
	 * @param xs - keywords, i.e. the fields to be returned. e.g. in Bacon: "name", "id", "film", "starring"...
	 * @return a string array that corresponds to the order of the argument array of keywords passed into it.
	 */
	private String[] getXs(String line, String ...xs) {
		//Searches for the entry we are looking for
		if (line == null) {
			return null;
		}
		//Splits up the linearray for quick searching.
		String[] resultsArray = new String[xs.length];
		String[] lineArray = Constants.tab.split(line);
		for(int i = 0; i < xs.length; i++) {
			String x = xs[i];
			if (parsePattern.containsKey(x)) {
				//We use the parsePattern we must have defined in search to get the data for each column.
				Integer numTabs = parsePattern.get(x);
				if (numTabs != null) {
					//If no field exists for this column, just put an empty string in where the data would go. 
					String s = "";
					if (lineArray.length > numTabs) {
						s = lineArray[numTabs];
					}
					resultsArray[i] = s; //We replace the key with the sought information.
				}
				else {
					System.out.println("ERROR: header file and data not aligned");
					return null;
				}
			}
			else {
				System.out.println("ERROR: usage - you input: " + Arrays.toString(xs));
				return null;
			}
		}
		return resultsArray;
	}

	public String[] getXsByY(String y, String ...xs) {
		return getXs(search(y), xs);
	}

	public List<List<String>> searchMultiples(String searchCode, String ...xs){ 
		return searchMultiples(searchCode, SearchType.DEFAULT, xs); 
	}

	public List<List<String>> searchMultiples(String searchCode, SearchType s, String...xs) {
		try {
			long secondLine = nextNewLine(0, length);
			if (parsePattern == null){
				parsePattern = readHeader(secondLine); //Establishes the parsePattern to read
			}
			//the CSV with. 
			if (parsePattern == null) {
				return null; //If there is a problem we can't read it, or search it. 
			}
			//SecondLine because we want to skip the header for the actual search. 
			//The real recursive engine of search. We'll get there in a second. 
			long wordPosition = search(searchCode.getBytes(UTF8), secondLine, length, s);
			if (wordPosition < 0) {
				return null;
			}
			//wordPosition is the position of the word, but we want the whole line. 
			long rangeStart = prevNewLine(wordPosition, 0);
			long rangeEnd = nextNewLine(wordPosition, length);
			byte[] searchCodeBytes = searchCode.getBytes();

			rangeStart = scanBackward(searchCodeBytes, rangeStart, s);
			rangeEnd = scanForward(searchCodeBytes, rangeEnd, s);
			return readChunk(rangeStart, rangeEnd + 1, xs);
		} catch (IOException e) {
			return null;
		}
	}

	/** 
	 * This function does a small but difficult job. It finds the first
	 * occurrence of the given word (searchCodeBytes) in the file.
	 * The name is a little misleading - it jumps backwards but then scans
	 * forward. The effect is the same as scanning backwards, but faster.
	 * @param searchCodeBytes - the word in question
	 * @param start - an instance of the word in the file.
	 * @param s - the type of search: DEFAULT looks for exact matches, and
	 * WILDCARD finds all words which start with the given word.
	 * @return - the newline holding the first occurrence of the word.
	 * @throws IOException - if there are issues with seek or read.
	 */
	long scanBackward(byte[] searchCodeBytes, long start, SearchType s) throws IOException {
		//The initial newLine is the start.
		long lastNewLine = start;
		//We don't want to accidentally try to read past the top of the file.
		while (start > 0) {
			byte[] arrayToSearch;
			int arraySize;
			if (start < bufferLength) {
				//If there isn't enough space at the top of the file.
				arraySize = (int) (start);
			}
			else {
				arraySize = bufferLength;
			}
			arrayToSearch = new byte[arraySize];
			long startIndex = start - arraySize;
			raf.seek(startIndex);
			//Jumps backwards into the file 
			raf.read(arrayToSearch);
			//Reads from the startIndex to the starting newLine.
			//Indicates whether we've found a MATCHING word in this run.
			boolean foundMatch = false;
			//Indicates whether we've even found a word to compare with.
			boolean foundWord = false;
			//Iterate through the array hunting for newLines.
			for(int i = 0; i < arraySize; i++) {
				int ch = arrayToSearch[i];
				if (ch  == '\n') {
					//TODO make sure this is right for all cases.
					newLines.putNext(startIndex, startIndex + i);
					//Depending on the file, we may need to skip some tabs.
					int tabsToSkip = parsePattern.get(sortingCol);
					int tabsFound = 0;
					int tabLocation = 0;
					for(int j = 0; j < arraySize; j++) {
						if (arrayToSearch[j + i] == '\t') {
							tabsFound++;
						}
						if (tabsFound == tabsToSkip) {
							tabLocation = j;
							break;
						}
					}
					//The convention is to return the exact index of the last tab,
					//so we need to add 1
					int wordStart = i + tabLocation + 1;
					foundWord = true;
					int cmp = compare(searchCodeBytes, arrayToSearch, wordStart);
					if (cmp == 0) {
						lastNewLine = startIndex + i;
						if (s == SearchType.WILDCARD) {
							foundMatch = true;
							break;
						} else if (arrayToSearch[wordStart + searchCodeBytes.length] == '\t' ||
								arrayToSearch[wordStart + searchCodeBytes.length] == '\n') {
							foundMatch = true;
							break;
						}
					}
				}
			}
			if (foundWord && !foundMatch) {
				return lastNewLine;
			} else {
				start = startIndex;
			}
		}
		return start;
	}

	/** 
	 * This function does a small but difficult job. It finds the last
	 * occurrence of the given word (searchCodeBytes) in the file.
	 * The name is a little misleading - it jumps forwards but then scans
	 * backward. The effect is the same as scanning forward, but faster.
	 * @param searchCodeBytes - the word in question
	 * @param start - an instance of the word in the file.
	 * @param s - the type of search: DEFAULT looks for exact matches, and
	 * WILDCARD finds all words which start with the given word.
	 * @return - the newline holding the last occurrence of the word.
	 * @throws IOException - if there are issues with seek or read.
	 */
	long scanForward(byte[] searchCodeBytes, long start, SearchType s) throws IOException {
		long lastNewLine = start;
		byte[] arrayToSearch;
		int arraySize;
		if ((length - start) < bufferLength) {
			arraySize = (int) (length - start);
		}
		else {
			arraySize = bufferLength;
		}
		arrayToSearch = new byte[arraySize];
		long endIndex = start + arraySize;
		raf.seek(start);
		raf.read(arrayToSearch);
		boolean foundInThisRun = false;
		boolean foundAWord = false;
		for(int i = arraySize - 1; i > 0; i--) {
			int ch = arrayToSearch[i];
			endIndex--;
			if (ch  == '\n') {
				//					newLines.putPrev(endIndex, endIndex - i);
				int tabsToSkip = parsePattern.get(sortingCol);
				int tabsFound = 0;
				int tabLocation = 0;
				for(int j = 0; j < arraySize; j++) {
					if (arrayToSearch[j + i] == '\t') {
						tabsFound++;
					}
					if (tabsFound == tabsToSkip) {
						tabLocation = j;
						break;
					}
				}
				int wordStart = i + tabLocation + 1; //Perhaps we need to add 1?
				foundAWord = true;
				int cmp = compare(searchCodeBytes, arrayToSearch, wordStart);
				if (cmp == 0) {
					if (s == SearchType.WILDCARD) {
						foundInThisRun = true;
						break;
					} else if (arrayToSearch[wordStart + searchCodeBytes.length] == '\t' ||
							arrayToSearch[wordStart + searchCodeBytes.length] == '\n') {
						foundInThisRun = true;
						break;
					}
				} else {
					lastNewLine = endIndex;
				}
			}
		}
		if (foundAWord && !foundInThisRun) {
			return endIndex == start ? start : lastNewLine;
		} else {
			return scanForward(searchCodeBytes, endIndex, s);
		}
	}

	String search(String searchCode) {
		return search(searchCode, SearchType.DEFAULT);
	}


	/**
	 * The real search command the above two methods use. Binary searches through the raf.
	 * @param searchCode - the key string we are searching for.
	 * @return - the line the key is on, or null if it's not found. 
	 */
	String search(String searchCode, SearchType s) {
		if (searchCode == null){
			return null;
		}
		try {
			long secondLine = nextNewLine(0, length);
			if (parsePattern == null){
				parsePattern = readHeader(secondLine); //Establishes the parsePattern to read
			}
			//the CSV with. 
			if (parsePattern == null) {
				return null; //If there is a problem we can't read it, or search it. 
			}
			//SecondLine because we want to skip the header for the actual search. 
			//The real recursive engine of search. We'll get there in a second. 
			long wordPosition = search(searchCode.getBytes(UTF8), secondLine, length, s);
			if (wordPosition < 0) {
				return null;
			}
			//wordPosition is the position of the word, but we want the whole line. 
			long lineStart = prevNewLine(wordPosition, 0) + 1;
			long lineEnd = nextNewLine(wordPosition, length);
			int lineLength = (int) (lineEnd - lineStart);
			byte[] line = new byte[lineLength];
			raf.seek(lineStart);
			raf.read(line);
			return string(line);
		}  catch (IOException e) {
			// 			would probably call the logger here. 
			//			System.out.println("ERROR: Could not find" + searchCode);
			return null;
		}
	}

	/** 
	 * Where the math for search goes down. Basically JJs implementation.
	 * uses byte[]s instead of strings for efficiency. 
	 * @param word - the word we are searching for, as a byte array. 
	 * @param top - the top of the range we are searching in.
	 * @param bottom - the bottom of the range we are searching in.
	 * @return - the position of the byte[] we are searching for. Returns -1 if not found. 
	 */
	private long search(byte[] word, long top, long bottom, SearchType s) {
		try {
			if (bottom - top < word.length) {
				return -1;
			}
			long mid = (top + bottom) / 2;
			long followingNewLine = nextNewLine(mid, bottom);
			boolean lastSearch = false;
			if (followingNewLine == bottom) {
				followingNewLine = prevNewLine(mid, top);
				if (followingNewLine == top) { //We've reached the top of the file, so we've got one more shot to check.
					lastSearch = true;
				}
			}
			long lastTab = skipTabs(followingNewLine, bottom, parsePattern.get(sortingCol));
			raf.seek(lastTab + 1);
			//XXX: changing to only do one read, with an extra byte
			byte[] tempField = new byte[word.length + 1]; //get one extra byte for use in the else clause below
			raf.read(tempField);
			int follow = tempField[tempField.length-1]; //returns the extra byte
			int cmp = compare(word, tempField);
			if (cmp < 0 && !lastSearch) {
				return search(word, top, followingNewLine, s);
			}
			else if (cmp > 0 && !lastSearch) {
				return search(word, followingNewLine, bottom, s);
			}
			else {
				//int follow = raf.read(); //either returns byte or -1 (end of file~)
				if (s == SearchType.WILDCARD) {
					return lastTab + 1;
				}	else {
					if (follow == '\t' || follow == '\n') { //Could we save time by eliminating this read?
						return lastTab + 1;
					} else {
						return (lastSearch) ? -1 : search(word, top, followingNewLine, s);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Looks like an infinite loop while searching for: " + string(word));
			return -1;
		}
	}

	public long  getByTabs(long newLine, long bottom) throws IOException {
		return skipTabs(newLine, bottom, parsePattern.get(sortingCol));
	}

	/** 
	 * Creates a string from a byte[] using the UTF-8 character set.
	 * 
	 * @param bytes - the byte array
	 * @return 
	 * the string created from the bytes. 
	 */
	public static String string(byte[] bytes) {
		return new String(bytes, UTF8);
	}


	/**
	 * Wrapper for System.out print stream
	 * @param s  - string to print to standard out
	 */
	public static void out(String s) {
		System.out.println(s);
	}

	/** Skips some tabs. Assumes a start is a new line and end is a new line also.
	 * 
	 * @param start - the starting position of a line in the file
	 * @param end - the ending position of a line in the file. 
	 * @param numTabs - the number of tabs we have to skip. 
	 * @return - a position in the line that is represents the last tab. 
	 * @throws IOException - in case something goes wrong with the raf. 
	 */
	private long skipTabs(long start, long end, int numTabs) throws IOException {
		int tabsFound = 0;
		byte[] arrayToSearch;
		int arraySize;
		if ((end - start) < tabBufferLength) {
			arraySize = (int) (end - start);
		}
		else {
			arraySize = tabBufferLength;
		}
		arrayToSearch = new byte[arraySize];
		raf.seek(start);
		raf.read(arrayToSearch);
		for (int i = 0; i < arraySize; i++) {
			int ch = arrayToSearch[i];
			if (ch  == '\t') {
				tabsFound++;
			}
			//We return the last tab index to keep with the convention of next/prevNewLine.
			if (tabsFound == numTabs) {
				return start + i;
			}
		}
		//if we haven't found it yet.
		if (start + arraySize < end) {
			return skipTabs(start + arraySize, end, numTabs - tabsFound);
		}
		else {
			return end;
		}
	}	

	public List<List<String>> readChunks(String...xs) {
		return readChunks(Constants.numThreads, xs);
	}

	private List<List<String>> readChunks(int numThreads, String...xs) {
		List<List<String>> chunks = new LinkedList<>();
		try {
			long secondLine = nextNewLine(0, length);
			long chunkSize = (length - secondLine) / numThreads + 1;
			for(int i = 1; i <= Constants.numThreads; i++) {
				long chunkEnd = (i == Constants.numThreads) ? length : nextNewLine(secondLine + (chunkSize * i), length);
				long chunkStart = nextNewLine((secondLine + (chunkSize * (i - 1))), chunkEnd);
				chunks.addAll(readChunk(chunkStart, chunkEnd, xs));
				System.out.println("Finished chunk: " + (i - 1) + " of: " + Constants.numThreads);
			}
		}
		catch (IOException e) {
			System.err.println("ERROR: readChunks could not read the file.");
		}
		System.out.println("Finished Reading");
		return chunks;
	}

	private List<List<String>> readChunk(long start, long end, String...xs) {
		List<List<String>> lines = new LinkedList<>();
		try {
			start++;
			byte[] chunk = new byte[(int) (end - start)];
			raf.seek(start);
			raf.read(chunk);
			int lastNewLine = -1;
			for(int i = 0; i < chunk.length; i++) {
				if (chunk[i] == '\n') {
					lines.add(Arrays.asList(getXs(string(Arrays.copyOfRange(chunk, lastNewLine + 1, i)), xs)));
					lastNewLine = i;
				}
			}
		} catch (IOException e) {
			return lines;
		}
		return lines;
	}

	/**
	 * Finds the immediate next newline character in the file.
	 * @param start - the starting location of the search.
	 * @param end - the ending location of the search. 
	 * @return - the position of the newline char. 
	 * @throws IOException - in case of problems with raf. 
	 */
	public long nextNewLine(long start, long end) throws IOException {
		/*if (Constants.ADJUST_LINE_BUFFER % numCalls== 0) {
			int suggestedLength = newLines.suggestLineLength(length);
			Util.out("SUGGESTED LENGTH OF BYTE BUFFER", suggestedLength, "IMPLEMENTING");
			bufferLength = suggestedLength;
		}*/
		if (start > end) {
			return end;
		}
		Long cachedNewLine = newLines.getNextNewLine(start);
		if (cachedNewLine != null) {
			return cachedNewLine;
		}
		raf.seek(start);
		byte[] arrayToSearch;
		int arraySize;
		if ((end - start) < bufferLength) {
			arraySize = (int) (end - start);
		}
		else {
			arraySize = bufferLength;
		}
		arrayToSearch = new byte[arraySize];
		raf.read(arrayToSearch);
		for(int i = 0; i < arraySize; i++) {
			int ch = arrayToSearch[i];
			if (ch  == '\n') {
				newLines.putNext(start, start + i);
				//				numCalls++;
				return start + i;
			}
		}
		if (start + arraySize < end) {
			return nextNewLine(start + arraySize, end);
		}
		else {
			return end;
		}
	}

	/** 
	 * Same thing really.
	 * @param start - starting location of search. 
	 * @param beginning - position above start in file, the upper bound. 
	 * @return - the position of the newline. 
	 * @throws IOException
	 */
	public long prevNewLine(long start, long beginning) throws IOException {
		/*		if (Constants.ADJUST_LINE_BUFFER % numCalls == 0) {
			int suggestedLength = newLines.suggestLineLength(length);
			Util.out("SUGGESTED LENGTH OF BYTE BUFFER", suggestedLength, "IMPLEMENTING");
			bufferLength = suggestedLength;
		}*/
		Long cachedPrevLine = newLines.getPrevNewLine(start);
		if (cachedPrevLine != null) {
			return cachedPrevLine;
		}
		byte[] arrayToSearch;
		int arraySize;
		if ((start - beginning) < bufferLength) {
			arraySize = (int) (start - beginning);
		}
		else {
			arraySize = bufferLength;
		}
		arrayToSearch = new byte[arraySize];
		raf.seek(start - arraySize);
		raf.read(arrayToSearch);
		for(int i = arraySize - 1; i >= 0; i--) {
			int ch = arrayToSearch[i];
			if (ch  == '\n') {
				newLines.putPrev(start, start - arraySize + i);
				//				numCalls++;
				return start - arraySize + i;
			}
		}
		if (start - arraySize > beginning) {
			return prevNewLine(start - arraySize, beginning);
		}
		else {
			return beginning;
		}
	}

	/** 
	 * from class, lexicographically compares two byte[]s.
	 * The index file is weirdly sorted so that words with
	 * special characters are pushed to the end. Not lexicographical
	 * which was advertised, so that's confusing. Therefore, we've
	 * implemented a comparator that has the same deficiency.
	 * In UTF8 special characters are represented by negative bytes.
	 * If both the byte[] have special characters, the one with the
	 * more negative bytes is less. However, if only one byte[] has
	 * a special character, the positive number will represent the
	 * standard character and should be less.
	 * @param a 
	 * @param b
	 * @return
	 */
	static int jCompare(byte[] a, byte[] b, int bIndex) {
		for(int i = 0; i < a.length; i++) {
			int relativeIndex = i + bIndex;
			if (b.length <= i) {
				return 1;
			}
			if (a[i] != b[relativeIndex]) {
				//If both bytes are special or standard, we can just subtract them.
				if ((a[i] < 0 && b[relativeIndex] < 0) || (a[i] > 0 && b[relativeIndex] > 0)) {
					return a[i] - b[relativeIndex];
				}
				else {
					//If only one byte is special, we want to HIGHER number to be first.
					return b[i] - a[relativeIndex];
				}
			}
		}
		return 0;
	}
	
	int compare(byte[] a, byte[] b, int bIndex) {
		return jCompare(a, b, bIndex);
	}

	int compare(byte[] a, byte[] b) {
		return jCompare(a, b, 0);
	}

	@Override
	//Attempts to close the file. If it can't, closes the file. 
	public void close() {
		try {
			if (raf != null) {
				raf.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Closing...");
			System.exit(1);
		}
	}
}
