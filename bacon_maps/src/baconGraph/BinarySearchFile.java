package baconGraph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Searchable file which reads from the disk, without reading the file into memory.
 * It also reads the headers of tsv style documents and parses them appropriately.
 * @author skortchm
 *
 */
public class BinarySearchFile implements AutoCloseable {

	RandomAccessFile raf; // the random access file to be searched
	private static final Charset UTF8 = Charset.forName("UTF-8"); // the type of encoding.
	ParseType pt; // what we are looking for in the file; what kind of file it is. 
	HashMap<String, Integer> parsePattern;
	HashMap<Long, Long> nextNewLines;
	HashMap<Long, Long> prevNewLines;

	enum ParseType {
		ACTOR, INDEX, FILM
	}

	/** 
	 * Main constructor of the BinarySearchFile.
	 * automatically closes if it fails top open it correctly. 
	 * @param filePath - the path to the file
	 * @param pt - what kind of file it is, what information we are seeking.
	 * @throws IOException = if we can't read the file for some reason. 
	 */
	BinarySearchFile(String filePath, ParseType pt) throws IOException {
		try {
			raf = new RandomAccessFile(filePath, "r");				
		} catch(FileNotFoundException ex) {
			System.out.println("ERROR: Unable to open file '" + filePath + "'");
			if (raf != null) {
				raf.close();
			}
			System.exit(1);
		}
		this.pt = pt; 
		nextNewLines = new HashMap<>();
		prevNewLines = new HashMap<>();
	}

	BinarySearchFile(String filePath) throws IOException {
		this(filePath, ParseType.INDEX);
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
			int name = -1; //the indices of the keywords we are searching for. 
			int id = -1;
			int starring = -1;
			int film = -1;
			for(int i = 0; i < headerPieces.length; i++) {
				String headerPiece = headerPieces[i];
				//keywords we are looking for. 
				switch (headerPiece) {
				case "id":
					id = i;
					break;
				case "name":
					name = i;
					break;
				case "starring":
					starring = i;
					break;
				case "film":
					film = i;
					break;
				}
			}
			if (id == -1 || name == -1) {
				System.out.println("ERROR: file header not formatted correctly");
				return null;
			}
			else {
				results.put("id", id);
				results.put("name", name);
			}
			switch (pt) {
			case FILM:
				if (starring != -1) {
					results.put("starring", starring);
				}
				else {
					System.out.println("ERROR: file header not formatted correctly");
					return null;
				}
				break;
			case ACTOR:
				if (film != -1) {
					results.put("film", film);
				}
				else {
					System.out.println("ERROR: file header not formatted correctly");
					return null;
				}
				break;
			default:
				results.put("name", 0);
				results.put("id", 1);
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
	public String[] getXsByY(String y, String ...xs) {
		//Searches for the entry we are looking for
		String line = search(y);
		if (line == null) {
			return null;
		}
		//Splits up the linearray for quick searching.
		String[] lineArray = Constants.tab.split(line);
		for(int i = 0; i < xs.length; i++) {
			String x = xs[i];
			if (x.equals("name") || x.equals("id") || x.equals("film") || x.equals("starring")) { //maybe an enum?
				//We use the parsePattern we must have defined in search to get the data for each column.
				Integer numTabs = parsePattern.get(x);
				if (numTabs != null && lineArray.length >= numTabs) {
					xs[i] = lineArray[numTabs]; //We replace the key with the sought information.
				}
				else {
					System.out.println("ERROR: header file and data not aligned");
					return null;
				}
			}
			else {
				System.out.println("ERROR: usage - you input: " + Arrays.toString(xs));
			}
		}
		return xs;
	}

	/** 
	 * Using the keywords defined above, we search for specific pieces of information
	 * through search and then return a string. 
	 * @param x - the piece of data we are searching for in the entry
	 * @param y - the search key
	 * @return - the piece of data corresponding to x. 
	 */
	public String getXByY(String x, String y) {
		if ( x.equals("name") || x.equals("id") || x.equals("film") || x.equals("starring")) { //maybe an enum?
			String line = search(y);
			if (line == null) {
				return null;
			}
			String[] lineArray = Constants.tab.split(line);
			Integer numTabs = parsePattern.get(x);
			if (numTabs != null && lineArray.length >= numTabs)
				return lineArray[numTabs];
			else {
				System.out.println("ERROR: header file and data not aligned");
			}
		}
		else {
			System.out.println("ERROR: you typed: " + x);
		}
		return null;
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
			parsePattern = readHeader(secondLine); //Establishes the parsePattern to read
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
			//wordPosition is the postion of the word, but we want the whole line. 
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
/*	private long search(byte[] word, long top, long bottom) {
		try {
			if (bottom - top < word.length) {
				return -1;
			}
			if (bottom - top == word.length) {
				raf.seek(top);
				byte[] possible = new byte[word.length];
				raf.read(possible);
				return Arrays.equals(word, possible) ? top : -1;
			}
			long mid = (top + bottom) / 2;
			long midNewLine = prevNewLine(mid, top); //perhaps here

			if (midNewLine <= top) {
				midNewLine = nextNewLine(midNewLine, bottom);
			}
			if (midNewLine == bottom) {
				return -1;
			}
			long lastTab = midNewLine;
			if (pt == ParseType.FILM || pt == ParseType.ACTOR) { //these two file types can be rearranged
				lastTab = skipTabs(midNewLine, bottom, parsePattern.get("id"));
			}
			if (lastTab == bottom) {
				System.out.println("lastTab was bottom");
				return -1;
			}
			raf.seek(lastTab + 1);
			byte[] middle = new byte[word.length];
			//compare by bytes - more efficient to search
			int num = raf.read(middle);
			out(string(middle));
			out("CW: " + string(word));
			int cmp = compare(word, middle);
			if (num < word.length || cmp < 0) {//end of file, files of small size.
				return search(word, top, lastTab + 1);
			}
			else if (cmp > 0) {
				if (top == midNewLine) {
					return -1;
				}
				else {
					return search(word, nextNewLine(mid, bottom) + 1, bottom);
				}
			}
			else { //cmp = 0
				///midpoint is "probably a longer form of word, so recur to top.
				//but we have to check to see if we've locked in to hitting the word itself.
				int follow = raf.read(); //either returns byte or -1 (end of file~)
				if(follow == '\t')
					return midNewLine + 1;
				return search(word, top, midNewLine + 1);
			}
		} catch (IOException e) {
			System.out.println("Looks like an infinite loop while searching for: " + string(word));
			return -1;
		}
	}*/

	private long search(byte[] word, long top, long bottom) {
		try {
		if (bottom - top < word.length) {
			return -1;
		}
		long mid = (top + bottom) / 2;
		long followingNewLine = nextNewLine(mid, bottom);
		if (followingNewLine == bottom) {
			followingNewLine = prevNewLine(mid, top);
			if (followingNewLine == top) {
				return -1;
			}
		}
		long lastTab = followingNewLine;
		if (pt == ParseType.FILM || pt == ParseType.ACTOR) { //these two file types can be rearranged
			lastTab = skipTabs(followingNewLine, bottom, parsePattern.get("id"));
		}
		raf.seek(lastTab + 1);
		byte[] testLine = new byte[word.length];
		raf.read(testLine);
		int cmp = compare(word, testLine);
		if (cmp < 0) {
			return search(word, top, followingNewLine);
		}
		else if (cmp > 0) {
			return search(word, followingNewLine, bottom);
		}
		else {
			int follow = raf.read(); //either returns byte or -1 (end of file~)
			if (follow == '\t') {
				return lastTab + 1;
			}
			return search(word, followingNewLine, bottom);
		}
		} catch (IOException e) {
			System.out.println("Looks like an infinite loop while searching for: " + string(word));
			return -1;
		}
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

	/**
	 * Finds the immediate next newline character in the file.
	 * @param start - the starting location of the search.
	 * @param end - the ending location of the search. 
	 * @return - the position of the newline char. 
	 * @throws IOException - in case of problems with raf. 
	 */
	public long nextNewLine(long start, long end) throws IOException {
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
	
	int gCompare(byte[] a, byte[] b) {
		return com.google.common.primitives.SignedBytes.lexicographicalComparator().compare(a, b);
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
