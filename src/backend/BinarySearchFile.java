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
	private static final Charset UTF8 = Charset.forName("UTF-8"); // the type of encoding.
	HashMap<String, Integer> parsePattern;
	HashMap<Long, Long> nextNewLines;
	HashMap<Long, Long> prevNewLines;
	String sortingCol;

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
		nextNewLines = new HashMap<>();
		prevNewLines = new HashMap<>();
		parsePattern = readHeader(nextNewLine(0, raf.length()));
		for(String colID : importantCols) {
			if (!parsePattern.containsKey(colID)) {
				System.out.println("ERROR: file header not formatted correctly");
				System.exit(0);
				break;
			}
		}
		this.sortingCol = sortingCol;
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
	 * @param y - the search key we are using.
	 * @param xs keywords - "name", "id", "film", "starring"
	 * @return a string array that corresponds to the order of the varargs keywords passed into it.
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
				if (numTabs != null && lineArray.length >= numTabs) {
					String s = lineArray[numTabs];
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

	/**
	 * The real search command the above two methods use. Binary searches through the raf.
	 * @param searchCode - the key string we are searching for. 
	 * @return - the line the key is on, or null if it's not found. 
	 */
	String search(String searchCode) {
		try {
			long length;
			length = raf.length();
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
			long wordPosition = search(searchCode.getBytes(UTF8), secondLine, length);
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
	private long search(byte[] word, long top, long bottom) {
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
			long lastTab = getByTabs(followingNewLine, bottom);
			raf.seek(lastTab + 1);
			byte[] testLine = new byte[word.length];
			raf.read(testLine);
			int cmp = compare(word, testLine);
			if (cmp < 0 && !lastSearch) {
				return search(word, top, followingNewLine);
			}
			else if (cmp > 0 && !lastSearch) {
				return search(word, followingNewLine, bottom);
			}
			else {
				int follow = raf.read(); //either returns byte or -1 (end of file~)
				if (follow == '\t' || follow == '\n') { //Could we save time by eliminating this read?
					return lastTab + 1;
				} else {
					return (lastSearch) ? -1 : search(word, followingNewLine, bottom);
				}
			}
		} catch (IOException e) {
			System.out.println("Looks like an infinite loop while searching for: " + string(word));
			return -1;
		}
	}

	public long  getByTabs(long followingNewLine, long bottom) throws IOException {
		long lastTab = skipTabs(followingNewLine, bottom, parsePattern.get(sortingCol));
		return lastTab;
	}

	/** 
	 * Creates a string from a byte[]
	 * @param bytes - the byte[]
	 * @return - the output string. 
	 */
	public static String string(byte[] bytes) {
		return new String(bytes, UTF8);
	}

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
		if ((end - start) < Constants.BufferLength) {
			arraySize = (int) (end - start);
		}
		else {
			arraySize = Constants.BufferLength;
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
		long length = raf.length();
		long secondLine = nextNewLine(0, length);
		long chunkSize = (length - secondLine) / numThreads + 1;
		for(int i = 1; i <= Constants.numThreads; i++) {
			long chunkEnd = (i == Constants.numThreads) ? length : nextNewLine(secondLine + (chunkSize * i), length) + 1;
			long chunkStart = nextNewLine((secondLine + (chunkSize * (i - 1))), chunkEnd) + 1;
			chunks.addAll(readChunk(chunkStart, chunkEnd, xs));
		}
		}
		catch (IOException e) {
			System.err.println("ERROR: readChunks could not read the file.");
		}
		return chunks;
	}
	
	private List<List<String>> readChunk(long start, long end, String...xs) {
		List<List<String>> lines = new LinkedList<>();
		long lineStart = start;
		long lineEnd = 0;
		while (lineStart < end) {
			try {
				lineEnd = nextNewLine(lineStart, end);
				byte[] line = new byte[(int) (lineEnd - lineStart)];
				raf.seek(lineStart);
				raf.read(line);
				lines.add(Arrays.asList(getXs(string(line), xs)));
				lineStart = lineEnd + 1;
			} catch (IOException e) {
				//TODO we should discuss the behavior here - 
				//how do we want to handle it if we can't read a chunk
				return lines;
			}
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
		if (start > end) {
			return end;
		}
		Long cachedNewLine = nextNewLines.get(start);
		if (cachedNewLine != null) {
			return cachedNewLine;
		}
		raf.seek(start);
		byte[] arrayToSearch;
		int arraySize;
		if ((end - start) < Constants.BufferLength) {
			arraySize = (int) (end - start);
		}
		else {
			arraySize = Constants.BufferLength;
		}
		arrayToSearch = new byte[arraySize];
		raf.read(arrayToSearch);
		for(int i = 0; i < arraySize; i++) {
			int ch = arrayToSearch[i];
			if (ch  == '\n') {
				nextNewLines.put(start, start + i);
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
		Long cachedPrevLine = prevNewLines.get(start);
		if (cachedPrevLine != null) {
			return cachedPrevLine;
		}
		byte[] arrayToSearch;
		int arraySize;
		if ((start - beginning) < Constants.BufferLength) {
			arraySize = (int) (start - beginning);
		}
		else {
			arraySize = Constants.BufferLength;
		}
		arrayToSearch = new byte[arraySize];
		raf.seek(start - arraySize);
		raf.read(arrayToSearch);
		for(int i = arraySize - 1; i >= 0; i--) {
			int ch = arrayToSearch[i];
			if (ch  == '\n') {
				prevNewLines.put(start, start - arraySize + i);
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
	static int jCompare(byte[] a, byte[] b) {
		for(int i = 0; i < a.length; i++) {
			if (b.length <= i) {
				return 1;
			}
			if (a[i] != b[i]) {
				//If both bytes are special or standard, we can just subtract them.
				if ((a[i] < 0 && b[i] < 0) || (a[i] > 0 && b[i] > 0)) {
					return a[i] - b[i];
				}
				else {
					//If only one byte is special, we want to HIGHER number to be first.
					return b[i] - a[i];
				}
			}
		}
		if (b.length < a.length) {
			return -1;
		}
		return 0;
	}

	int compare(byte[] a, byte[] b) {
		return jCompare(a, b);
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
